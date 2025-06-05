package com.smartbot.comm

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Manages WiFi communication with ESP32 robot
 */
class WiFiManager(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _connectionState = MutableStateFlow(WiFiConnectionState())
    val connectionState: StateFlow<WiFiConnectionState> = _connectionState.asStateFlow()
    
    data class WiFiConnectionState(
        val isConnected: Boolean = false,
        val isConnecting: Boolean = false,
        val esp32IpAddress: String = "",
        val esp32Port: Int = 80,
        val error: String? = null,
        val lastResponse: String = "",
        val responseTime: Long = 0L
    )
    
    companion object {
        private const val DEFAULT_ESP32_IP = "192.168.4.1" // ESP32 AP mode default
        private const val DEFAULT_PORT = 80
        private const val CONNECTION_TIMEOUT = 5000L
    }
    
    /**
     * Connect to ESP32 via WiFi
     */
    fun connectToESP32(ipAddress: String = DEFAULT_ESP32_IP, port: Int = DEFAULT_PORT) {
        scope.launch {
            try {
                _connectionState.value = _connectionState.value.copy(
                    isConnecting = true,
                    error = null
                )
                
                // Test connection with a simple ping
                val success = testConnection(ipAddress, port)
                
                if (success) {
                    _connectionState.value = _connectionState.value.copy(
                        isConnected = true,
                        isConnecting = false,
                        esp32IpAddress = ipAddress,
                        esp32Port = port,
                        error = null
                    )
                } else {
                    _connectionState.value = _connectionState.value.copy(
                        isConnected = false,
                        isConnecting = false,
                        error = "Failed to connect to ESP32 at $ipAddress:$port"
                    )
                }
                
            } catch (e: Exception) {
                _connectionState.value = _connectionState.value.copy(
                    isConnected = false,
                    isConnecting = false,
                    error = "Connection error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Disconnect from ESP32
     */
    fun disconnect() {
        _connectionState.value = _connectionState.value.copy(
            isConnected = false,
            isConnecting = false,
            esp32IpAddress = "",
            error = null
        )
    }
    
    /**
     * Test connection to ESP32
     */
    private suspend fun testConnection(ipAddress: String, port: Int): Boolean {
        return try {
            val url = "http://$ipAddress:$port/ping"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val responseTime = System.currentTimeMillis() - startTime
            
            _connectionState.value = _connectionState.value.copy(responseTime = responseTime)
            
            response.isSuccessful
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * Send HTTP GET request to ESP32
     */
    suspend fun sendGetRequest(endpoint: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!_connectionState.value.isConnected) {
                    return@withContext Result.failure(Exception("Not connected to ESP32"))
                }
                
                val url = "http://${_connectionState.value.esp32IpAddress}:${_connectionState.value.esp32Port}$endpoint"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                
                val startTime = System.currentTimeMillis()
                val response = client.newCall(request).execute()
                val responseTime = System.currentTimeMillis() - startTime
                
                _connectionState.value = _connectionState.value.copy(responseTime = responseTime)
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    _connectionState.value = _connectionState.value.copy(
                        lastResponse = "GET $endpoint: $responseBody",
                        error = null
                    )
                    Result.success(responseBody)
                } else {
                    val error = "HTTP ${response.code}: ${response.message}"
                    _connectionState.value = _connectionState.value.copy(error = error)
                    Result.failure(Exception(error))
                }
                
            } catch (e: IOException) {
                val error = "Network error: ${e.message}"
                _connectionState.value = _connectionState.value.copy(error = error)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Send HTTP POST request to ESP32
     */
    suspend fun sendPostRequest(endpoint: String, jsonData: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!_connectionState.value.isConnected) {
                    return@withContext Result.failure(Exception("Not connected to ESP32"))
                }
                
                val url = "http://${_connectionState.value.esp32IpAddress}:${_connectionState.value.esp32Port}$endpoint"
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonData.toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
                
                val startTime = System.currentTimeMillis()
                val response = client.newCall(request).execute()
                val responseTime = System.currentTimeMillis() - startTime
                
                _connectionState.value = _connectionState.value.copy(responseTime = responseTime)
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    _connectionState.value = _connectionState.value.copy(
                        lastResponse = "POST $endpoint: $responseBody",
                        error = null
                    )
                    Result.success(responseBody)
                } else {
                    val error = "HTTP ${response.code}: ${response.message}"
                    _connectionState.value = _connectionState.value.copy(error = error)
                    Result.failure(Exception(error))
                }
                
            } catch (e: IOException) {
                val error = "Network error: ${e.message}"
                _connectionState.value = _connectionState.value.copy(error = error)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Send movement command via WiFi
     */
    suspend fun sendMovementCommand(direction: RobotDirection, speed: Int = 100): Result<String> {
        val command = when (direction) {
            RobotDirection.FORWARD -> "forward"
            RobotDirection.BACKWARD -> "backward"
            RobotDirection.LEFT -> "left"
            RobotDirection.RIGHT -> "right"
            RobotDirection.STOP -> "stop"
        }
        
        val jsonData = """{"command": "$command", "speed": $speed}"""
        return sendPostRequest("/move", jsonData)
    }
    
    /**
     * Send motor control command via WiFi
     */
    suspend fun sendMotorCommand(leftSpeed: Int, rightSpeed: Int): Result<String> {
        val jsonData = """{"left_motor": $leftSpeed, "right_motor": $rightSpeed}"""
        return sendPostRequest("/motor", jsonData)
    }
    
    /**
     * Request sensor data via WiFi
     */
    suspend fun requestSensorData(): Result<String> {
        return sendGetRequest("/sensors")
    }
    
    /**
     * Send LED control command via WiFi
     */
    suspend fun sendLEDCommand(red: Int, green: Int, blue: Int): Result<String> {
        val jsonData = """{"red": $red, "green": $green, "blue": $blue}"""
        return sendPostRequest("/led", jsonData)
    }
    
    /**
     * Get robot status via WiFi
     */
    suspend fun getRobotStatus(): Result<String> {
        return sendGetRequest("/status")
    }
    
    /**
     * Send custom command via WiFi
     */
    suspend fun sendCustomCommand(command: String, parameters: Map<String, Any> = emptyMap()): Result<String> {
        val jsonData = buildString {
            append("""{"command": "$command"""")
            if (parameters.isNotEmpty()) {
                parameters.forEach { (key, value) ->
                    append(""", "$key": """)
                    when (value) {
                        is String -> append(""""$value"""")
                        is Number -> append(value)
                        is Boolean -> append(value)
                        else -> append(""""$value"""")
                    }
                }
            }
            append("}")
        }
        
        return sendPostRequest("/command", jsonData)
    }
    
    /**
     * Scan for ESP32 devices on local network
     */
    suspend fun scanForESP32Devices(): List<String> {
        return withContext(Dispatchers.IO) {
            val foundDevices = mutableListOf<String>()
            val baseIP = "192.168."
            val subnets = listOf("1.", "4.", "0.") // Common ESP32 subnets
            
            val jobs = mutableListOf<Job>()
            
            subnets.forEach { subnet ->
                for (i in 1..254) {
                    val ip = "$baseIP$subnet$i"
                    val job = launch {
                        if (testConnection(ip, DEFAULT_PORT)) {
                            synchronized(foundDevices) {
                                foundDevices.add(ip)
                            }
                        }
                    }
                    jobs.add(job)
                }
            }
            
            // Wait for all scans to complete with timeout
            withTimeoutOrNull(10000) {
                jobs.joinAll()
            }
            
            foundDevices.toList()
        }
    }
    
    /**
     * Get connection statistics
     */
    fun getConnectionStats(): Map<String, Any> {
        val state = _connectionState.value
        return mapOf(
            "connected" to state.isConnected,
            "ip_address" to state.esp32IpAddress,
            "port" to state.esp32Port,
            "last_response_time" to state.responseTime,
            "last_response" to state.lastResponse,
            "error" to (state.error ?: "None")
        )
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        scope.cancel()
        client.dispatcher.executorService.shutdown()
    }
    
    enum class RobotDirection {
        FORWARD, BACKWARD, LEFT, RIGHT, STOP
    }
}