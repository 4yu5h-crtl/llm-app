# 📱 SmartBot Installation Guide

This guide will help you set up SmartBot on your Android device and configure the ESP32 robot platform.

## 📋 System Requirements

### Android Device
- **OS**: Android 5.0 (API level 21) or higher
- **RAM**: 4GB+ recommended (for LLM models)
- **Storage**: 2GB+ free space
- **Hardware**: 
  - Bluetooth 4.0+
  - WiFi capability
  - Camera (for vision features)
  - Microphone (for voice features)
  - Accelerometer and Gyroscope

### Development Environment
- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **JDK**: 8 or higher
- **Gradle**: 7.0+
- **Kotlin**: 1.9.10+

## 🚀 Quick Installation

### Option 1: Install APK (Recommended for Users)

1. **Download the latest APK** from the [Releases page](https://github.com/4yu5h-crtl/SmartBot-Educational-Robot/releases)
2. **Enable Unknown Sources** in Android Settings > Security
3. **Install the APK** by tapping on the downloaded file
4. **Grant permissions** when prompted (Camera, Microphone, Bluetooth, Location)
5. **Launch SmartBot** and start exploring!

### Option 2: Build from Source (For Developers)

1. **Clone the repository**
   ```bash
   git clone https://github.com/4yu5h-crtl/SmartBot-Educational-Robot.git
   cd SmartBot-Educational-Robot
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory and select it
   - Wait for Gradle sync to complete

3. **Configure the project**
   - Ensure Android SDK is properly configured
   - Install any missing SDK components
   - Sync project with Gradle files

4. **Build and run**
   - Connect your Android device via USB
   - Enable Developer Options and USB Debugging
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## 🤖 ESP32 Robot Setup (Optional)

The robot hardware is optional but provides the full SmartBot experience.

### Hardware Requirements

| Component | Specification | Quantity |
|-----------|---------------|----------|
| ESP32 Development Board | ESP32-WROOM-32 or similar | 1 |
| Motor Driver | L298N Dual H-Bridge | 1 |
| DC Motors | 6V Geared Motors | 2 |
| Ultrasonic Sensor | HC-SR04 | 1 |
| RGB LED | Common Cathode or WS2812 | 1 |
| Battery Pack | 7.4V Li-Po or 6x AA | 1 |
| Robot Chassis | Any suitable platform | 1 |
| Jumper Wires | Male-to-Male, Male-to-Female | 20+ |
| Breadboard | Half-size or full-size | 1 |

### Software Setup

1. **Install Arduino IDE**
   - Download from [arduino.cc](https://www.arduino.cc/en/software)
   - Install version 1.8.19 or newer

2. **Add ESP32 Board Support**
   - Open Arduino IDE
   - Go to File > Preferences
   - Add this URL to "Additional Board Manager URLs":
     ```
     https://dl.espressif.com/dl/package_esp32_index.json
     ```
   - Go to Tools > Board > Board Manager
   - Search for "ESP32" and install "ESP32 by Espressif Systems"

3. **Install Required Libraries**
   - Go to Sketch > Include Library > Manage Libraries
   - Install "ArduinoJson" by Benoit Blanchon (version 6.x)

4. **Flash the Firmware**
   - Open `esp32_firmware/smartbot_robot.ino` in Arduino IDE
   - Select your ESP32 board from Tools > Board
   - Select the correct COM port from Tools > Port
   - Click Upload

### Hardware Assembly

Detailed wiring instructions are available in [esp32_firmware/README.md](../esp32_firmware/README.md).

**Quick Wiring Summary:**
```
ESP32 Pin → Component
GPIO 18   → L298N ENA (Left Motor PWM)
GPIO 19   → L298N IN1 (Left Motor Dir 1)
GPIO 21   → L298N IN2 (Left Motor Dir 2)
GPIO 22   → L298N ENB (Right Motor PWM)
GPIO 23   → L298N IN3 (Right Motor Dir 1)
GPIO 25   → L298N IN4 (Right Motor Dir 2)
GPIO 26   → HC-SR04 Trig
GPIO 27   → HC-SR04 Echo
GPIO 32   → RGB LED Red
GPIO 33   → RGB LED Green
GPIO 14   → RGB LED Blue
```

## 🔧 Configuration

### First Launch Setup

1. **Grant Permissions**
   - Camera: For computer vision features
   - Microphone: For voice recognition
   - Bluetooth: For robot communication
   - Location: Required for Bluetooth scanning on Android 6+

2. **Connect to Robot** (if available)
   - Go to Robot Control screen
   - Tap "Connect" button
   - Select your ESP32 device from the list
   - Wait for connection confirmation

3. **Choose Learning Mode**
   - Navigate to Chat screen
   - Select your preferred educational mode
   - Set appropriate difficulty level

### Advanced Configuration

#### LLM Model Setup
1. **Download Models** (optional)
   - Models can be downloaded from Hugging Face
   - Supported formats: GGUF quantized models
   - Place in `Android/data/com.smartbot/files/models/`

2. **Model Configuration**
   - TinyLLaMA: ~669MB, good for basic conversations
   - Phi-2: ~1.5GB, better reasoning capabilities
   - Mistral 7B: ~4GB, advanced conversations (requires 6GB+ RAM)

#### Network Configuration
- **WiFi Setup**: ESP32 creates "SmartBot_Robot" network (password: smartbot123)
- **Bluetooth**: Automatic pairing with "SmartBot_ESP32" device

## 🔍 Troubleshooting

### Common Issues

#### App Won't Install
- **Solution**: Enable "Install from Unknown Sources" in Android settings
- **Alternative**: Use `adb install` command if building from source

#### Bluetooth Connection Fails
- **Check**: Bluetooth is enabled on both devices
- **Verify**: Location permission is granted (required for Bluetooth scanning)
- **Try**: Restart Bluetooth on Android device
- **Reset**: Clear Bluetooth cache in Android settings

#### Robot Doesn't Respond
- **Power**: Ensure ESP32 and motors have adequate power supply
- **Connections**: Verify all wiring connections are secure
- **Firmware**: Confirm firmware is properly flashed
- **Range**: Stay within 10 meters for Bluetooth, 30 meters for WiFi

#### Voice Recognition Not Working
- **Permissions**: Ensure microphone permission is granted
- **Network**: Some features require internet for speech recognition
- **Noise**: Try in a quieter environment
- **Language**: Check that device language is supported

#### Camera Features Not Working
- **Permissions**: Ensure camera permission is granted
- **Lighting**: Ensure adequate lighting for object detection
- **Focus**: Tap to focus camera on objects
- **Performance**: Close other camera apps

### Performance Optimization

#### For Low-End Devices
- Disable computer vision features if not needed
- Use smaller LLM models (TinyLLaMA)
- Reduce sensor sampling rate
- Close background apps

#### For Better Battery Life
- Lower screen brightness
- Disable unnecessary sensors
- Use WiFi instead of Bluetooth when possible
- Enable battery optimization for the app

## 📚 Next Steps

After successful installation:

1. **Explore the Tutorial** - Built-in guided tour of features
2. **Try Voice Commands** - "Hello SmartBot, teach me about physics"
3. **Test Robot Control** - Use directional controls and preset commands
4. **Experiment with Sensors** - Move your device and watch sensor data
5. **Use Computer Vision** - Point camera at objects for educational descriptions

## 🆘 Getting Help

If you encounter issues:

1. **Check Documentation** - Review this guide and the main README
2. **Search Issues** - Look through [GitHub Issues](https://github.com/4yu5h-crtl/SmartBot-Educational-Robot/issues)
3. **Create Issue** - Report bugs or request help
4. **Join Discussions** - Ask questions in [GitHub Discussions](https://github.com/4yu5h-crtl/SmartBot-Educational-Robot/discussions)

## 🔄 Updates

SmartBot will notify you of available updates. To update:

1. **APK Users**: Download and install the latest APK
2. **Source Users**: Pull latest changes and rebuild
3. **ESP32 Firmware**: Flash updated firmware when available

---

**Happy Learning with SmartBot! 🎓🤖**