# 🤖 SmartBot - Educational Robot with Local LLM Integration

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android%205.0%2B-green.svg)](https://developer.android.com)
[![ESP32](https://img.shields.io/badge/Hardware-ESP32-blue.svg)](https://www.espressif.com/en/products/socs/esp32)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg)](https://developer.android.com/jetpack/compose)
[![ML Kit](https://img.shields.io/badge/AI-ML%20Kit-red.svg)](https://developers.google.com/ml-kit)

> **🎓 Transform your smartphone into an intelligent educational robot platform**

SmartBot is a comprehensive Android application that combines local AI processing, smartphone sensors, and ESP32-based robotics to create engaging educational experiences. Perfect for STEM education, maker projects, and interactive learning.

## 📱 Demo & Screenshots

| Home Dashboard | AI Chat Interface | Sensor Monitoring | Robot Control |
|:---:|:---:|:---:|:---:|
| ![Home](docs/screenshots/home.png) | ![Chat](docs/screenshots/chat.png) | ![Sensors](docs/screenshots/sensors.png) | ![Robot](docs/screenshots/robot.png) |

*Screenshots coming soon - app is ready for testing!*

## 🌟 Key Features

### 🧠 **Local AI Integration**
- **🦙 TinyLLaMA, Phi-2, Mistral** model support
- **🔒 Privacy-focused** on-device processing
- **📚 Educational conversations** with context awareness
- **📈 Adaptive difficulty** from beginner to advanced
- **🎯 Subject-specific modes** (Math, Science, Programming, etc.)

### 🎤 **Voice Interaction**
- **🎙️ Speech-to-text** recognition with Android SpeechRecognizer
- **🔊 Text-to-speech** synthesis with multiple languages
- **🗣️ Educational phrases** library for quick responses
- **⚡ Real-time** voice command processing

### 📱 **Smart Sensor Integration**
- **📊 Motion detection** (accelerometer, gyroscope, magnetometer)
- **🌡️ Environmental sensing** (light, proximity sensors)
- **📈 Educational analytics** and live demonstrations
- **📋 Calibration tools** for accurate measurements

### 🔗 **Dual Communication**
- **📶 Bluetooth Classic** for reliable robot connection
- **📡 WiFi HTTP** for high-bandwidth control and monitoring
- **⚡ Real-time control** with low latency
- **📊 Status monitoring** with live feedback

### 👁️ **Computer Vision**
- **🎯 Object detection** powered by Google ML Kit
- **😊 Face recognition** with emotion analysis
- **📝 Text recognition** (OCR) for educational content
- **🎓 Educational descriptions** for detected objects

### 🎨 **Modern UI/UX**
- **🎨 Material Design 3** interface
- **🚀 Jetpack Compose** declarative UI
- **📱 Responsive design** for all Android devices
- **♿ Accessibility** features included

## 🚀 Quick Start

### 📋 Prerequisites

**For Android Development:**
- Android Studio Arctic Fox (2020.3.1) or newer
- Android SDK 21+ (Android 5.0 Lollipop)
- Kotlin 1.9.10 or newer
- 4GB+ RAM device (for LLM models)

**For ESP32 Robot:**
- ESP32 development board
- L298N motor driver
- 2x DC geared motors
- HC-SR04 ultrasonic sensor
- RGB LED or NeoPixel
- 7.4V battery pack

### 🔧 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/4yu5h-crtl/SmartBot-Educational-Robot.git
   cd SmartBot-Educational-Robot
   ```

2. **Open in Android Studio**
   ```bash
   # Open Android Studio and import the project
   # Sync Gradle files
   # Build and run on your Android device
   ```

3. **Set up ESP32 Robot** (Optional)
   ```bash
   # Flash the firmware from esp32_firmware/smartbot_robot.ino
   # See esp32_firmware/README.md for detailed setup
   ```

### 🎮 Usage

1. **Launch the app** on your Android device
2. **Grant permissions** (Camera, Microphone, Bluetooth, Location)
3. **Connect to robot** via Bluetooth or WiFi (optional)
4. **Choose learning mode** (Math, Science, Programming, etc.)
5. **Start exploring** with voice commands and interactive features!

## 🏗️ Architecture

### 📱 Android App Structure
```
app/src/main/java/com/smartbot/
├── 🧠 ai/                    # AI & LLM integration
│   ├── LLMManager.kt         # Local model management
│   └── ConversationManager.kt # Educational conversations
├── 🎤 voice/                 # Speech processing
│   ├── SpeechRecognitionManager.kt
│   └── TextToSpeechManager.kt
├── 📊 sensors/               # Device sensors
│   └── SensorManager.kt      # Motion & environmental sensors
├── 🔗 comm/                  # Robot communication
│   ├── BluetoothManager.kt   # Bluetooth connectivity
│   └── WiFiManager.kt        # WiFi HTTP communication
├── 👁️ vision/                # Computer vision
│   ├── CameraManager.kt      # Camera operations
│   └── ObjectDetectionManager.kt # ML Kit integration
└── 🎨 ui/                    # User interface
    ├── screens/              # Jetpack Compose screens
    ├── theme/                # Material Design theme
    └── viewmodel/            # MVVM ViewModels
```

### 🤖 ESP32 Robot Features
- **Motor control** with PWM speed control
- **Sensor integration** (ultrasonic, battery monitoring)
- **LED feedback** for status indication
- **Dual communication** (Bluetooth + WiFi)
- **Safety features** (motor timeout, speed limiting)

## 🎓 Educational Applications

### 📚 Learning Domains
- **🔢 Mathematics**: Interactive problem solving and calculations
- **🔬 Science**: Physics demonstrations and experiments
- **💻 Programming**: Code concepts and algorithm visualization
- **🤖 Robotics**: Mechanics, sensors, and control systems
- **🌍 General Knowledge**: Interactive Q&A and exploration

### 🎯 Teaching Methods
- **💬 Interactive Dialogue**: AI-powered educational conversations
- **🤲 Hands-on Learning**: Physical robot demonstrations
- **👀 Visual Learning**: Object recognition and analysis
- **🏃 Kinesthetic Learning**: Motion-based activities
- **📈 Adaptive Difficulty**: Personalized learning progression

## 🛠️ Development

### 🏃‍♂️ Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### 🔧 Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### 📝 Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use [ktlint](https://ktlint.github.io/) for formatting
- Add KDoc comments for public APIs
- Write unit tests for business logic

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### 🎯 Areas for Contribution
- 📱 **Android Development**: UI/UX improvements, new features
- 🤖 **Hardware Integration**: ESP32 firmware, sensor integration
- 🧠 **AI/ML**: Educational content, model optimization
- 📚 **Educational Content**: Learning modules, curriculum
- 📖 **Documentation**: Guides, tutorials, translations

### 🚀 Quick Contribution Steps
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📊 Project Status

### ✅ Completed Features
- [x] Android app with Jetpack Compose UI
- [x] Local LLM integration framework
- [x] Voice recognition and synthesis
- [x] Sensor data collection and visualization
- [x] Bluetooth and WiFi communication
- [x] Computer vision with ML Kit
- [x] ESP32 robot firmware
- [x] Educational conversation system

### 🚧 In Progress
- [ ] LLM model optimization for mobile
- [ ] Advanced computer vision features
- [ ] Enhanced educational content
- [ ] Multi-language support

### 🔮 Future Plans
- [ ] AR/VR integration
- [ ] Cloud synchronization
- [ ] Multi-robot coordination
- [ ] Professional educator tools

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **[llama.cpp](https://github.com/ggerganov/llama.cpp)** for local LLM capabilities
- **[Google ML Kit](https://developers.google.com/ml-kit)** for computer vision
- **[Android Open Source Project](https://source.android.com/)** for the platform
- **[ESP32 Community](https://www.espressif.com/)** for hardware support
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** for modern UI

## 📞 Support & Contact

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/4yu5h-crtl/SmartBot-Educational-Robot/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/4yu5h-crtl/SmartBot-Educational-Robot/discussions)
- 📧 **Email**: ayush.chintalwar1234@gmail.com
- 🌐 **Website**: [Ayush Chintalwar](https://ayush-chintalwar.netlify.app/)

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=4yu5h-crtl/SmartBot-Educational-Robot&type=Date)](https://star-history.com/#4yu5h-crtl/SmartBot-Educational-Robot&Date)

---

<div align="center">

**Made with ❤️ for education and learning**

[⭐ Star this repo](https://github.com/4yu5h-crtl/SmartBot-Educational-Robot) • [🐛 Report Bug](https://github.com/4yu5h-crtl/SmartBot-Educational-Robot/issues) • [💡 Request Feature](https://github.com/4yu5h-crtl/SmartBot-Educational-Robot/discussions)

</div>