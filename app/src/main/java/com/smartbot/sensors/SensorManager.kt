package com.smartbot.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager as AndroidSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages device sensors for educational and robotic applications
 */
class SensorManager(private val context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager
    
    // Sensors
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var magnetometer: Sensor? = null
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var gravitySensor: Sensor? = null
    private var linearAcceleration: Sensor? = null
    private var rotationVector: Sensor? = null
    
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()
    
    private val _sensorStatus = MutableStateFlow(SensorStatus())
    val sensorStatus: StateFlow<SensorStatus> = _sensorStatus.asStateFlow()
    
    data class SensorData(
        // Accelerometer (m/s²)
        val accelerometerX: Float = 0f,
        val accelerometerY: Float = 0f,
        val accelerometerZ: Float = 0f,
        
        // Gyroscope (rad/s)
        val gyroscopeX: Float = 0f,
        val gyroscopeY: Float = 0f,
        val gyroscopeZ: Float = 0f,
        
        // Magnetometer (μT)
        val magnetometerX: Float = 0f,
        val magnetometerY: Float = 0f,
        val magnetometerZ: Float = 0f,
        
        // Light sensor (lux)
        val lightLevel: Float = 0f,
        
        // Proximity sensor (cm)
        val proximity: Float = 0f,
        
        // Gravity (m/s²)
        val gravityX: Float = 0f,
        val gravityY: Float = 0f,
        val gravityZ: Float = 0f,
        
        // Linear acceleration (m/s²)
        val linearAccelX: Float = 0f,
        val linearAccelY: Float = 0f,
        val linearAccelZ: Float = 0f,
        
        // Rotation vector
        val rotationX: Float = 0f,
        val rotationY: Float = 0f,
        val rotationZ: Float = 0f,
        val rotationW: Float = 0f,
        
        // Derived values
        val totalAcceleration: Float = 0f,
        val totalRotation: Float = 0f,
        val deviceOrientation: DeviceOrientation = DeviceOrientation.UNKNOWN,
        val motionState: MotionState = MotionState.STATIONARY
    )
    
    data class SensorStatus(
        val accelerometerAvailable: Boolean = false,
        val gyroscopeAvailable: Boolean = false,
        val magnetometerAvailable: Boolean = false,
        val lightSensorAvailable: Boolean = false,
        val proximitySensorAvailable: Boolean = false,
        val gravitySensorAvailable: Boolean = false,
        val linearAccelerationAvailable: Boolean = false,
        val rotationVectorAvailable: Boolean = false,
        val isListening: Boolean = false,
        val samplingRate: Int = AndroidSensorManager.SENSOR_DELAY_NORMAL
    )
    
    enum class DeviceOrientation {
        PORTRAIT, LANDSCAPE_LEFT, LANDSCAPE_RIGHT, PORTRAIT_UPSIDE_DOWN, FACE_UP, FACE_DOWN, UNKNOWN
    }
    
    enum class MotionState {
        STATIONARY, WALKING, RUNNING, SHAKING, ROTATING, UNKNOWN
    }
    
    init {
        initializeSensors()
    }
    
    /**
     * Initialize available sensors
     */
    private fun initializeSensors() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        
        _sensorStatus.value = SensorStatus(
            accelerometerAvailable = accelerometer != null,
            gyroscopeAvailable = gyroscope != null,
            magnetometerAvailable = magnetometer != null,
            lightSensorAvailable = lightSensor != null,
            proximitySensorAvailable = proximitySensor != null,
            gravitySensorAvailable = gravitySensor != null,
            linearAccelerationAvailable = linearAcceleration != null,
            rotationVectorAvailable = rotationVector != null
        )
    }
    
    /**
     * Start listening to sensors
     */
    fun startListening(samplingRate: Int = AndroidSensorManager.SENSOR_DELAY_NORMAL) {
        accelerometer?.let { 
            sensorManager.registerListener(this, it, samplingRate)
        }
        gyroscope?.let { 
            sensorManager.registerListener(this, it, samplingRate)
        }
        magnetometer?.let { 
            sensorManager.registerListener(this, it, samplingRate)
        }
        lightSensor?.let { 
            sensorManager.registerListener(this, it, samplingRate)
        }
        proximitySensor?.let { 
            sensorManager.registerListener(this, it, samplingRate)
        }
        gravitySensor?.let { 
            sensorManager.registerListener(this, it, samplingRate)
        }
        linearAcceleration?.let { 
            sensorManager.registerListener(this, it, samplingRate)
        }
        rotationVector?.let { 
            sensorManager.registerListener(this, it, samplingRate)
        }
        
        _sensorStatus.value = _sensorStatus.value.copy(
            isListening = true,
            samplingRate = samplingRate
        )
    }
    
    /**
     * Stop listening to sensors
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        _sensorStatus.value = _sensorStatus.value.copy(isListening = false)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            val currentData = _sensorData.value
            
            val updatedData = when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    currentData.copy(
                        accelerometerX = sensorEvent.values[0],
                        accelerometerY = sensorEvent.values[1],
                        accelerometerZ = sensorEvent.values[2]
                    )
                }
                Sensor.TYPE_GYROSCOPE -> {
                    currentData.copy(
                        gyroscopeX = sensorEvent.values[0],
                        gyroscopeY = sensorEvent.values[1],
                        gyroscopeZ = sensorEvent.values[2]
                    )
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    currentData.copy(
                        magnetometerX = sensorEvent.values[0],
                        magnetometerY = sensorEvent.values[1],
                        magnetometerZ = sensorEvent.values[2]
                    )
                }
                Sensor.TYPE_LIGHT -> {
                    currentData.copy(lightLevel = sensorEvent.values[0])
                }
                Sensor.TYPE_PROXIMITY -> {
                    currentData.copy(proximity = sensorEvent.values[0])
                }
                Sensor.TYPE_GRAVITY -> {
                    currentData.copy(
                        gravityX = sensorEvent.values[0],
                        gravityY = sensorEvent.values[1],
                        gravityZ = sensorEvent.values[2]
                    )
                }
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    currentData.copy(
                        linearAccelX = sensorEvent.values[0],
                        linearAccelY = sensorEvent.values[1],
                        linearAccelZ = sensorEvent.values[2]
                    )
                }
                Sensor.TYPE_ROTATION_VECTOR -> {
                    currentData.copy(
                        rotationX = sensorEvent.values[0],
                        rotationY = sensorEvent.values[1],
                        rotationZ = sensorEvent.values[2],
                        rotationW = if (sensorEvent.values.size > 3) sensorEvent.values[3] else 0f
                    )
                }
                else -> currentData
            }
            
            // Calculate derived values
            val finalData = updatedData.copy(
                totalAcceleration = calculateTotalAcceleration(updatedData),
                totalRotation = calculateTotalRotation(updatedData),
                deviceOrientation = calculateOrientation(updatedData),
                motionState = calculateMotionState(updatedData)
            )
            
            _sensorData.value = finalData
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
    
    /**
     * Calculate total acceleration magnitude
     */
    private fun calculateTotalAcceleration(data: SensorData): Float {
        return kotlin.math.sqrt(
            data.accelerometerX * data.accelerometerX +
            data.accelerometerY * data.accelerometerY +
            data.accelerometerZ * data.accelerometerZ
        )
    }
    
    /**
     * Calculate total rotation magnitude
     */
    private fun calculateTotalRotation(data: SensorData): Float {
        return kotlin.math.sqrt(
            data.gyroscopeX * data.gyroscopeX +
            data.gyroscopeY * data.gyroscopeY +
            data.gyroscopeZ * data.gyroscopeZ
        )
    }
    
    /**
     * Calculate device orientation based on accelerometer data
     */
    private fun calculateOrientation(data: SensorData): DeviceOrientation {
        val x = data.accelerometerX
        val y = data.accelerometerY
        val z = data.accelerometerZ
        
        return when {
            kotlin.math.abs(z) > 8 -> if (z > 0) DeviceOrientation.FACE_DOWN else DeviceOrientation.FACE_UP
            kotlin.math.abs(y) > kotlin.math.abs(x) -> if (y > 0) DeviceOrientation.PORTRAIT_UPSIDE_DOWN else DeviceOrientation.PORTRAIT
            x > 0 -> DeviceOrientation.LANDSCAPE_LEFT
            x < 0 -> DeviceOrientation.LANDSCAPE_RIGHT
            else -> DeviceOrientation.UNKNOWN
        }
    }
    
    /**
     * Calculate motion state based on sensor data
     */
    private fun calculateMotionState(data: SensorData): MotionState {
        val totalAccel = data.totalAcceleration
        val totalRotation = data.totalRotation
        
        return when {
            totalRotation > 2.0f -> MotionState.ROTATING
            totalAccel > 15.0f -> MotionState.SHAKING
            totalAccel > 12.0f -> MotionState.RUNNING
            totalAccel > 10.5f -> MotionState.WALKING
            totalAccel < 9.0f || totalAccel > 10.5f -> MotionState.STATIONARY
            else -> MotionState.UNKNOWN
        }
    }
    
    /**
     * Get sensor information
     */
    fun getSensorInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        
        accelerometer?.let { sensor ->
            info["Accelerometer"] = "${sensor.name} (${sensor.vendor})"
        }
        gyroscope?.let { sensor ->
            info["Gyroscope"] = "${sensor.name} (${sensor.vendor})"
        }
        magnetometer?.let { sensor ->
            info["Magnetometer"] = "${sensor.name} (${sensor.vendor})"
        }
        lightSensor?.let { sensor ->
            info["Light Sensor"] = "${sensor.name} (${sensor.vendor})"
        }
        proximitySensor?.let { sensor ->
            info["Proximity Sensor"] = "${sensor.name} (${sensor.vendor})"
        }
        
        return info
    }
    
    /**
     * Calibrate sensors (reset baseline values)
     */
    fun calibrateSensors() {
        // Reset to current values as baseline
        // This is a simple implementation - more sophisticated calibration could be added
        _sensorData.value = SensorData()
    }
    
    /**
     * Get educational sensor explanations
     */
    fun getSensorExplanations(): Map<String, String> {
        return mapOf(
            "accelerometer" to "Measures acceleration forces in three dimensions. Used to detect movement and orientation.",
            "gyroscope" to "Measures rotational motion around three axes. Helps determine how the device is rotating.",
            "magnetometer" to "Measures magnetic field strength. Can be used as a digital compass.",
            "light" to "Measures ambient light levels. Useful for automatic brightness adjustment.",
            "proximity" to "Detects nearby objects without physical contact. Often used to turn off screen during calls.",
            "gravity" to "Measures the force of gravity in three dimensions. Helps separate gravity from other accelerations.",
            "linear_acceleration" to "Measures acceleration excluding gravity. Shows pure motion acceleration.",
            "rotation_vector" to "Provides device orientation as a rotation vector. Combines multiple sensors for accuracy."
        )
    }
}