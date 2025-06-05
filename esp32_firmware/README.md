# SmartBot ESP32 Robot Firmware

This firmware enables the ESP32 to communicate with the SmartBot Android app and control a basic robot platform.

## Hardware Requirements

### Core Components
- ESP32 development board (ESP32-WROOM-32 or similar)
- L298N motor driver module
- 2x DC geared motors (6V recommended)
- Robot chassis or platform
- Battery pack (7.4V Li-Po or 6x AA batteries)
- Jumper wires and breadboard

### Sensors (Optional)
- HC-SR04 ultrasonic distance sensor
- MPU6050 gyroscope/accelerometer (future expansion)
- Light sensor (LDR)

### Output Devices
- RGB LED (common cathode) or WS2812 NeoPixel
- Buzzer (future expansion)

## Wiring Diagram

### Motor Driver (L298N)
```
ESP32 Pin    L298N Pin    Function
GPIO 18   -> ENA         Left motor PWM
GPIO 19   -> IN1         Left motor direction 1
GPIO 21   -> IN2         Left motor direction 2
GPIO 22   -> ENB         Right motor PWM
GPIO 23   -> IN3         Right motor direction 1
GPIO 25   -> IN4         Right motor direction 2
VIN       -> VCC         Power supply
GND       -> GND         Ground
```

### Ultrasonic Sensor (HC-SR04)
```
ESP32 Pin    HC-SR04 Pin
GPIO 26   -> Trig
GPIO 27   -> Echo
3.3V      -> VCC
GND       -> GND
```

### RGB LED
```
ESP32 Pin    LED Pin
GPIO 32   -> Red
GPIO 33   -> Green
GPIO 14   -> Blue
GND       -> Common Cathode
```

### Battery Monitoring
```
ESP32 Pin    Connection
GPIO 35   -> Battery voltage divider (optional)
```

## Software Setup

### Arduino IDE Setup
1. Install Arduino IDE (1.8.19 or newer)
2. Add ESP32 board support:
   - Go to File > Preferences
   - Add this URL to Additional Board Manager URLs:
     `https://dl.espressif.com/dl/package_esp32_index.json`
   - Go to Tools > Board > Board Manager
   - Search for "ESP32" and install "ESP32 by Espressif Systems"

### Required Libraries
Install these libraries through Arduino IDE Library Manager:
- `ArduinoJson` by Benoit Blanchon (version 6.x)
- `ESP32 Arduino Core` (included with board package)

### Upload Instructions
1. Connect ESP32 to computer via USB
2. Select board: "ESP32 Dev Module" or your specific ESP32 board
3. Select correct COM port
4. Click Upload

## Communication Protocols

### Bluetooth Commands
The robot accepts commands via Bluetooth Serial in the following format:

#### Movement Commands
- `MOVE:F:100` - Move forward at 100% speed
- `MOVE:B:50` - Move backward at 50% speed
- `MOVE:L:75` - Turn left at 75% speed
- `MOVE:R:75` - Turn right at 75% speed
- `MOVE:S:0` - Stop movement

#### Motor Control
- `MOTOR:50:-30` - Left motor 50%, right motor -30%

#### LED Control
- `LED:255:0:128` - Set RGB LED (red:255, green:0, blue:128)

#### Sensor Requests
- `SENSOR:ALL` - Request all sensor data
- `SENSOR:ULTRASONIC` - Request ultrasonic distance

#### Special Commands
- `STOP` - Emergency stop
- `DANCE` - Execute dance routine
- `SPIN` - Spin in place
- `PATROL` - Execute patrol pattern

### WiFi HTTP API
The robot creates a WiFi access point and serves HTTP endpoints:

#### Access Point Details
- SSID: `SmartBot_Robot`
- Password: `smartbot123`
- IP Address: `192.168.4.1`

#### HTTP Endpoints

**GET /ping**
- Response: `pong`
- Purpose: Connection test

**POST /move**
```json
{
  "command": "forward",
  "speed": 100
}
```

**POST /motor**
```json
{
  "left_motor": 50,
  "right_motor": -30
}
```

**POST /led**
```json
{
  "red": 255,
  "green": 0,
  "blue": 128
}
```

**GET /sensors**
```json
{
  "ultrasonic_distance": 25.4,
  "battery_level": 85,
  "timestamp": 12345
}
```

**GET /status**
```json
{
  "is_moving": true,
  "left_motor_speed": 50,
  "right_motor_speed": 50,
  "battery_level": 85,
  "last_command": "MOVE:F:50",
  "uptime": 123456
}
```

## Safety Features

### Motor Timeout
- Motors automatically stop after 5 seconds without commands
- Prevents runaway robot behavior

### Speed Limiting
- Motor speeds are constrained to -100% to +100%
- Prevents damage from excessive speeds

### Battery Monitoring
- Low battery detection (if voltage divider connected)
- Automatic shutdown at critical levels

## Troubleshooting

### Common Issues

**Robot doesn't move:**
- Check motor driver connections
- Verify battery voltage (should be > 6V)
- Ensure motors are connected to driver outputs
- Check if emergency stop was triggered

**Bluetooth connection fails:**
- Verify ESP32 is powered and running
- Check if device name "SmartBot_ESP32" appears in Bluetooth scan
- Ensure Android app has Bluetooth permissions

**WiFi connection issues:**
- Look for "SmartBot_Robot" network
- Use password "smartbot123"
- Connect to IP address 192.168.4.1

**Erratic movement:**
- Check motor driver power supply
- Verify all ground connections
- Ensure motor driver can handle motor current

### LED Status Indicators
- **Blue**: Robot ready/idle
- **Green**: Moving forward
- **Yellow**: Moving backward
- **Blue**: Turning left
- **Magenta**: Turning right
- **Red**: Stopped/error

## Customization

### Changing WiFi Credentials
Modify these lines in the code:
```cpp
const char* ssid = "SmartBot_Robot";
const char* password = "smartbot123";
```

### Adjusting Motor Speeds
Modify the PWM mapping in `setMotorSpeeds()`:
```cpp
int leftPWM = map(abs(robotState.leftMotorSpeed), 0, 100, 0, 255);
```

### Adding New Commands
1. Add command parsing in `processCommand()`
2. Implement command handler function
3. Add corresponding HTTP endpoint if needed

### Sensor Integration
Add new sensors by:
1. Defining pins in the pin definitions section
2. Adding initialization in `setupSensors()`
3. Adding reading logic in `updateSensors()`
4. Exposing data through HTTP/Bluetooth APIs

## Performance Notes

### Memory Usage
- Current firmware uses approximately 60% of ESP32 flash
- RAM usage is minimal with room for expansion

### Communication Range
- Bluetooth: ~10 meters line of sight
- WiFi: ~30 meters depending on environment

### Battery Life
- Typical operation: 2-4 hours depending on battery capacity
- Standby mode: 8-12 hours

## Future Enhancements

### Planned Features
- IMU integration for better movement control
- Camera module support
- Voice command processing
- Autonomous navigation
- Multiple robot coordination

### Hardware Expansions
- Servo motors for arm/gripper
- Additional sensors (temperature, humidity)
- Speaker for audio feedback
- Display module for status

## Support

For technical issues:
1. Check wiring connections
2. Verify power supply voltage
3. Monitor serial output for error messages
4. Test individual components separately

## License

This firmware is part of the SmartBot project and is licensed under the MIT License.