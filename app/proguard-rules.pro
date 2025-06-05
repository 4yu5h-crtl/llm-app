# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep native methods for LLM integration
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Bluetooth classes
-keep class android.bluetooth.** { *; }

# Keep camera classes
-keep class androidx.camera.** { *; }

# Keep sensor classes
-keep class android.hardware.** { *; }

# Keep speech recognition classes
-keep class android.speech.** { *; }

# Keep OkHttp classes
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data classes
-keep class com.smartbot.ui.viewmodel.** { *; }
-keep class com.smartbot.ai.** { *; }
-keep class com.smartbot.comm.** { *; }
-keep class com.smartbot.sensors.** { *; }
-keep class com.smartbot.vision.** { *; }
-keep class com.smartbot.voice.** { *; }