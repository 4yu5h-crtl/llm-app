/*
 * SmartBot ESP32 Robot Firmware
 * 
 * This firmware enables the ESP32 to communicate with the SmartBot Android app
 * and control a basic robot platform with motors, sensors, and LEDs.
 * 
 * Hardware Requirements:
 * - ESP32 development board
 * - L298N motor driver
 * - 2x DC motors
 * - HC-SR04 ultrasonic sensor
 * - RGB LED (or WS2812 NeoPixel)
 * - Battery pack
 * 
 * Communication:
 * - Bluetooth Classic (SPP)
 * - WiFi (HTTP server)
 */

#include "BluetoothSerial.h"
#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>

// Pin definitions
#define MOTOR_LEFT_PWM    18
#define MOTOR_LEFT_DIR1   19
#define MOTOR_LEFT_DIR2   21
#define MOTOR_RIGHT_PWM   22
#define MOTOR_RIGHT_DIR1  23
#define MOTOR_RIGHT_DIR2  25

#define ULTRASONIC_TRIG   26
#define ULTRASONIC_ECHO   27

#define LED_RED           32
#define LED_GREEN         33
#define LED_BLUE          14

#define BATTERY_PIN       35

// Communication
BluetoothSerial SerialBT;
WebServer server(80);

// Robot state
struct RobotState {
  int leftMotorSpeed = 0;
  int rightMotorSpeed = 0;
  bool isMoving = false;
  int batteryLevel = 100;
  float ultrasonicDistance = 0.0;
  String lastCommand = "";
  unsigned long lastCommandTime = 0;
} robotState;

// WiFi credentials (AP mode)
const char* ssid = "SmartBot_Robot";
const char* password = "smartbot123";

void setup() {
  Serial.begin(115200);
  
  // Initialize pins
  setupMotors();
  setupSensors();
  setupLEDs();
  
  // Initialize communication
  setupBluetooth();
  setupWiFi();
  
  // Set initial LED color (blue = ready)
  setLED(0, 0, 255);
  
  Serial.println("SmartBot Robot initialized!");
}

void loop() {
  // Handle Bluetooth communication
  handleBluetooth();
  
  // Handle WiFi communication
  server.handleClient();
  
  // Update sensors
  updateSensors();
  
  // Auto-stop motors after timeout
  checkMotorTimeout();
  
  delay(50);
}

void setupMotors() {
  pinMode(MOTOR_LEFT_PWM, OUTPUT);
  pinMode(MOTOR_LEFT_DIR1, OUTPUT);
  pinMode(MOTOR_LEFT_DIR2, OUTPUT);
  pinMode(MOTOR_RIGHT_PWM, OUTPUT);
  pinMode(MOTOR_RIGHT_DIR1, OUTPUT);
  pinMode(MOTOR_RIGHT_DIR2, OUTPUT);
  
  // Initialize motors to stopped
  stopMotors();
}

void setupSensors() {
  pinMode(ULTRASONIC_TRIG, OUTPUT);
  pinMode(ULTRASONIC_ECHO, INPUT);
  pinMode(BATTERY_PIN, INPUT);
}

void setupLEDs() {
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);
}

void setupBluetooth() {
  SerialBT.begin("SmartBot_ESP32");
  Serial.println("Bluetooth initialized. Device name: SmartBot_ESP32");
}

void setupWiFi() {
  // Create WiFi Access Point
  WiFi.softAP(ssid, password);
  IPAddress IP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(IP);
  
  // Setup HTTP endpoints
  server.on("/", handleRoot);
  server.on("/ping", handlePing);
  server.on("/move", HTTP_POST, handleMove);
  server.on("/motor", HTTP_POST, handleMotor);
  server.on("/led", HTTP_POST, handleLED);
  server.on("/sensors", handleSensors);
  server.on("/status", handleStatus);
  server.on("/command", HTTP_POST, handleCommand);
  
  server.begin();
  Serial.println("HTTP server started");
}

void handleBluetooth() {
  if (SerialBT.available()) {
    String command = SerialBT.readStringUntil('\n');
    command.trim();
    processCommand(command);
  }
}

void processCommand(String command) {
  robotState.lastCommand = command;
  robotState.lastCommandTime = millis();
  
  Serial.println("Received command: " + command);
  
  if (command.startsWith("MOVE:")) {
    handleMoveCommand(command);
  } else if (command.startsWith("MOTOR:")) {
    handleMotorCommand(command);
  } else if (command.startsWith("LED:")) {
    handleLEDCommand(command);
  } else if (command.startsWith("SENSOR:")) {
    handleSensorCommand(command);
  } else if (command == "STOP") {
    stopMotors();
  } else if (command == "DANCE") {
    danceMode();
  } else if (command == "SPIN") {
    spinMode();
  } else if (command == "PATROL") {
    patrolMode();
  }
  
  // Send acknowledgment
  SerialBT.println("OK:" + command);
}

void handleMoveCommand(String command) {
  // Format: MOVE:F:100 (direction:speed)
  int firstColon = command.indexOf(':');
  int secondColon = command.indexOf(':', firstColon + 1);
  
  if (firstColon != -1 && secondColon != -1) {
    String direction = command.substring(firstColon + 1, secondColon);
    int speed = command.substring(secondColon + 1).toInt();
    
    speed = constrain(speed, 0, 100);
    
    if (direction == "F") {
      moveForward(speed);
    } else if (direction == "B") {
      moveBackward(speed);
    } else if (direction == "L") {
      turnLeft(speed);
    } else if (direction == "R") {
      turnRight(speed);
    } else if (direction == "S") {
      stopMotors();
    }
  }
}

void handleMotorCommand(String command) {
  // Format: MOTOR:50:-30 (left:right)
  int firstColon = command.indexOf(':');
  int secondColon = command.indexOf(':', firstColon + 1);
  
  if (firstColon != -1 && secondColon != -1) {
    int leftSpeed = command.substring(firstColon + 1, secondColon).toInt();
    int rightSpeed = command.substring(secondColon + 1).toInt();
    
    setMotorSpeeds(leftSpeed, rightSpeed);
  }
}

void handleLEDCommand(String command) {
  // Format: LED:255:0:128 (red:green:blue)
  int firstColon = command.indexOf(':');
  int secondColon = command.indexOf(':', firstColon + 1);
  int thirdColon = command.indexOf(':', secondColon + 1);
  
  if (firstColon != -1 && secondColon != -1 && thirdColon != -1) {
    int red = command.substring(firstColon + 1, secondColon).toInt();
    int green = command.substring(secondColon + 1, thirdColon).toInt();
    int blue = command.substring(thirdColon + 1).toInt();
    
    setLED(red, green, blue);
  }
}

void handleSensorCommand(String command) {
  if (command == "SENSOR:ALL") {
    String sensorData = "SENSOR:ULTRASONIC:" + String(robotState.ultrasonicDistance) + 
                       ",BATTERY:" + String(robotState.batteryLevel);
    SerialBT.println(sensorData);
  }
}

void moveForward(int speed) {
  setMotorSpeeds(speed, speed);
  setLED(0, 255, 0); // Green for forward
}

void moveBackward(int speed) {
  setMotorSpeeds(-speed, -speed);
  setLED(255, 255, 0); // Yellow for backward
}

void turnLeft(int speed) {
  setMotorSpeeds(-speed, speed);
  setLED(0, 0, 255); // Blue for left
}

void turnRight(int speed) {
  setMotorSpeeds(speed, -speed);
  setLED(255, 0, 255); // Magenta for right
}

void stopMotors() {
  setMotorSpeeds(0, 0);
  setLED(255, 0, 0); // Red for stop
}

void setMotorSpeeds(int leftSpeed, int rightSpeed) {
  robotState.leftMotorSpeed = constrain(leftSpeed, -100, 100);
  robotState.rightMotorSpeed = constrain(rightSpeed, -100, 100);
  robotState.isMoving = (leftSpeed != 0 || rightSpeed != 0);
  
  // Left motor
  int leftPWM = map(abs(robotState.leftMotorSpeed), 0, 100, 0, 255);
  analogWrite(MOTOR_LEFT_PWM, leftPWM);
  
  if (robotState.leftMotorSpeed > 0) {
    digitalWrite(MOTOR_LEFT_DIR1, HIGH);
    digitalWrite(MOTOR_LEFT_DIR2, LOW);
  } else if (robotState.leftMotorSpeed < 0) {
    digitalWrite(MOTOR_LEFT_DIR1, LOW);
    digitalWrite(MOTOR_LEFT_DIR2, HIGH);
  } else {
    digitalWrite(MOTOR_LEFT_DIR1, LOW);
    digitalWrite(MOTOR_LEFT_DIR2, LOW);
  }
  
  // Right motor
  int rightPWM = map(abs(robotState.rightMotorSpeed), 0, 100, 0, 255);
  analogWrite(MOTOR_RIGHT_PWM, rightPWM);
  
  if (robotState.rightMotorSpeed > 0) {
    digitalWrite(MOTOR_RIGHT_DIR1, HIGH);
    digitalWrite(MOTOR_RIGHT_DIR2, LOW);
  } else if (robotState.rightMotorSpeed < 0) {
    digitalWrite(MOTOR_RIGHT_DIR1, LOW);
    digitalWrite(MOTOR_RIGHT_DIR2, HIGH);
  } else {
    digitalWrite(MOTOR_RIGHT_DIR1, LOW);
    digitalWrite(MOTOR_RIGHT_DIR2, LOW);
  }
}

void setLED(int red, int green, int blue) {
  analogWrite(LED_RED, red);
  analogWrite(LED_GREEN, green);
  analogWrite(LED_BLUE, blue);
}

void updateSensors() {
  // Update ultrasonic sensor
  digitalWrite(ULTRASONIC_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(ULTRASONIC_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(ULTRASONIC_TRIG, LOW);
  
  long duration = pulseIn(ULTRASONIC_ECHO, HIGH);
  robotState.ultrasonicDistance = duration * 0.034 / 2;
  
  // Update battery level
  int batteryReading = analogRead(BATTERY_PIN);
  robotState.batteryLevel = map(batteryReading, 0, 4095, 0, 100);
}

void checkMotorTimeout() {
  // Auto-stop motors after 5 seconds of no commands
  if (robotState.isMoving && (millis() - robotState.lastCommandTime > 5000)) {
    stopMotors();
    Serial.println("Motor timeout - stopping");
  }
}

void danceMode() {
  for (int i = 0; i < 4; i++) {
    moveForward(70);
    delay(500);
    moveBackward(70);
    delay(500);
    turnLeft(70);
    delay(300);
    turnRight(70);
    delay(300);
  }
  stopMotors();
}

void spinMode() {
  turnRight(80);
  delay(2000);
  stopMotors();
}

void patrolMode() {
  for (int i = 0; i < 3; i++) {
    moveForward(60);
    delay(1000);
    turnRight(60);
    delay(500);
  }
  stopMotors();
}

// HTTP Handlers
void handleRoot() {
  String html = "<html><body><h1>SmartBot Robot</h1>";
  html += "<p>Status: " + String(robotState.isMoving ? "Moving" : "Idle") + "</p>";
  html += "<p>Battery: " + String(robotState.batteryLevel) + "%</p>";
  html += "<p>Distance: " + String(robotState.ultrasonicDistance) + " cm</p>";
  html += "</body></html>";
  server.send(200, "text/html", html);
}

void handlePing() {
  server.send(200, "text/plain", "pong");
}

void handleMove() {
  if (server.hasArg("plain")) {
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, server.arg("plain"));
    
    String command = doc["command"];
    int speed = doc["speed"] | 100;
    
    String moveCommand = "MOVE:" + command.substring(0, 1).toUpperCase() + ":" + String(speed);
    processCommand(moveCommand);
    
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  } else {
    server.send(400, "application/json", "{\"error\":\"no data\"}");
  }
}

void handleMotor() {
  if (server.hasArg("plain")) {
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, server.arg("plain"));
    
    int leftSpeed = doc["left_motor"];
    int rightSpeed = doc["right_motor"];
    
    String motorCommand = "MOTOR:" + String(leftSpeed) + ":" + String(rightSpeed);
    processCommand(motorCommand);
    
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  } else {
    server.send(400, "application/json", "{\"error\":\"no data\"}");
  }
}

void handleLED() {
  if (server.hasArg("plain")) {
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, server.arg("plain"));
    
    int red = doc["red"];
    int green = doc["green"];
    int blue = doc["blue"];
    
    String ledCommand = "LED:" + String(red) + ":" + String(green) + ":" + String(blue);
    processCommand(ledCommand);
    
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  } else {
    server.send(400, "application/json", "{\"error\":\"no data\"}");
  }
}

void handleSensors() {
  DynamicJsonDocument doc(1024);
  doc["ultrasonic_distance"] = robotState.ultrasonicDistance;
  doc["battery_level"] = robotState.batteryLevel;
  doc["timestamp"] = millis();
  
  String response;
  serializeJson(doc, response);
  server.send(200, "application/json", response);
}

void handleStatus() {
  DynamicJsonDocument doc(1024);
  doc["is_moving"] = robotState.isMoving;
  doc["left_motor_speed"] = robotState.leftMotorSpeed;
  doc["right_motor_speed"] = robotState.rightMotorSpeed;
  doc["battery_level"] = robotState.batteryLevel;
  doc["last_command"] = robotState.lastCommand;
  doc["uptime"] = millis();
  
  String response;
  serializeJson(doc, response);
  server.send(200, "application/json", response);
}

void handleCommand() {
  if (server.hasArg("plain")) {
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, server.arg("plain"));
    
    String command = doc["command"];
    processCommand(command);
    
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  } else {
    server.send(400, "application/json", "{\"error\":\"no data\"}");
  }
}