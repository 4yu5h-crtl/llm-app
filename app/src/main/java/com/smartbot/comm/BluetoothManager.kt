package com.smartbot.comm

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * Manages Bluetooth communication with ESP32 robot
 */
class BluetoothManager(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()
    
    data class ConnectionState(
        val isConnected: Boolean = false,
        val isConnecting: Boolean = false,
        val isDiscovering: Boolean = false,
        val connectedDevice: BluetoothDevice? = null,
        val error: String? = null,
        val lastMessage: String = "",
        val signalStrength: Int = 0
    )
    
    companion object {
        private val ESP32_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID
        private const val ESP32_NAME_PREFIX = "ESP32"
    }
    
    // Broadcast receiver for device discovery
    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { discoveredDevice ->
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            // Filter for ESP32 devices
                            if (discoveredDevice.name?.contains(ESP32_NAME_PREFIX, ignoreCase = true) == true) {
                                val currentDevices = _discoveredDevices.value.toMutableList()
                                if (!currentDevices.any { it.address == discoveredDevice.address }) {
                                    currentDevices.add(discoveredDevice)
                                    _discoveredDevices.value = currentDevices
                                }
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _connectionState.value = _connectionState.value.copy(isDiscovering = true)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _connectionState.value = _connectionState.value.copy(isDiscovering = false)
                }
            }
        }
    }
    
    init {
        // Register broadcast receiver
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(discoveryReceiver, filter)
    }
    
    /**
     * Check if Bluetooth is available and enabled
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }
    
    /**
     * Start discovering ESP32 devices
     */
    fun startDiscovery(): Boolean {
        if (!isBluetoothAvailable()) return false
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            _connectionState.value = _connectionState.value.copy(error = "Bluetooth scan permission required")
            return false
        }
        
        // Clear previous discoveries
        _discoveredDevices.value = emptyList()
        
        // Start discovery
        return bluetoothAdapter?.startDiscovery() ?: false
    }
    
    /**
     * Stop device discovery
     */
    fun stopDiscovery() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.cancelDiscovery()
        }
    }
    
    /**
     * Get paired ESP32 devices
     */
    fun getPairedESP32Devices(): List<BluetoothDevice> {
        if (!isBluetoothAvailable()) return emptyList()
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }
        
        return bluetoothAdapter?.bondedDevices?.filter { device ->
            device.name?.contains(ESP32_NAME_PREFIX, ignoreCase = true) == true
        } ?: emptyList()
    }
    
    /**
     * Connect to ESP32 device
     */
    fun connectToDevice(device: BluetoothDevice) {
        scope.launch {
            try {
                _connectionState.value = _connectionState.value.copy(
                    isConnecting = true,
                    error = null
                )
                
                // Stop discovery if running
                stopDiscovery()
                
                // Create socket
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    _connectionState.value = _connectionState.value.copy(
                        isConnecting = false,
                        error = "Bluetooth connect permission required"
                    )
                    return@launch
                }
                
                bluetoothSocket = device.createRfcommSocketToServiceRecord(ESP32_UUID)
                
                bluetoothSocket?.let { socket ->
                    socket.connect()
                    
                    inputStream = socket.inputStream
                    outputStream = socket.outputStream
                    
                    _connectionState.value = _connectionState.value.copy(
                        isConnected = true,
                        isConnecting = false,
                        connectedDevice = device,
                        error = null
                    )
                    
                    // Start listening for incoming messages
                    startListening()
                }
                
            } catch (e: IOException) {
                _connectionState.value = _connectionState.value.copy(
                    isConnected = false,
                    isConnecting = false,
                    error = "Connection failed: ${e.message}"
                )
                cleanup()
            }
        }
    }
    
    /**
     * Disconnect from current device
     */
    fun disconnect() {
        scope.launch {
            cleanup()
            _connectionState.value = _connectionState.value.copy(
                isConnected = false,
                isConnecting = false,
                connectedDevice = null,
                error = null
            )
        }
    }
    
    /**
     * Send command to ESP32
     */
    fun sendCommand(command: String): Boolean {
        return try {
            if (!_connectionState.value.isConnected || outputStream == null) {
                _connectionState.value = _connectionState.value.copy(error = "Not connected")
                return false
            }
            
            val message = "$command\n"
            outputStream?.write(message.toByteArray())
            outputStream?.flush()
            
            _connectionState.value = _connectionState.value.copy(
                lastMessage = "Sent: $command",
                error = null
            )
            
            true
        } catch (e: IOException) {
            _connectionState.value = _connectionState.value.copy(
                error = "Send failed: ${e.message}"
            )
            false
        }
    }
    
    /**
     * Send robot movement command
     */
    fun sendMovementCommand(direction: RobotDirection, speed: Int = 100): Boolean {
        val command = when (direction) {
            RobotDirection.FORWARD -> "MOVE:F:$speed"
            RobotDirection.BACKWARD -> "MOVE:B:$speed"
            RobotDirection.LEFT -> "MOVE:L:$speed"
            RobotDirection.RIGHT -> "MOVE:R:$speed"
            RobotDirection.STOP -> "MOVE:S:0"
        }
        return sendCommand(command)
    }
    
    /**
     * Send motor control command
     */
    fun sendMotorCommand(leftSpeed: Int, rightSpeed: Int): Boolean {
        val command = "MOTOR:$leftSpeed:$rightSpeed"
        return sendCommand(command)
    }
    
    /**
     * Request sensor data from ESP32
     */
    fun requestSensorData(): Boolean {
        return sendCommand("SENSOR:ALL")
    }
    
    /**
     * Send LED control command
     */
    fun sendLEDCommand(red: Int, green: Int, blue: Int): Boolean {
        val command = "LED:$red:$green:$blue"
        return sendCommand(command)
    }
    
    /**
     * Start listening for incoming messages
     */
    private fun startListening() {
        scope.launch {
            val buffer = ByteArray(1024)
            
            while (_connectionState.value.isConnected && inputStream != null) {
                try {
                    val bytesRead = inputStream?.read(buffer) ?: 0
                    if (bytesRead > 0) {
                        val message = String(buffer, 0, bytesRead).trim()
                        handleIncomingMessage(message)
                    }
                } catch (e: IOException) {
                    if (_connectionState.value.isConnected) {
                        _connectionState.value = _connectionState.value.copy(
                            error = "Connection lost: ${e.message}"
                        )
                        disconnect()
                    }
                    break
                }
            }
        }
    }
    
    /**
     * Handle incoming messages from ESP32
     */
    private fun handleIncomingMessage(message: String) {
        _connectionState.value = _connectionState.value.copy(
            lastMessage = "Received: $message"
        )
        
        // Parse different message types
        when {
            message.startsWith("SENSOR:") -> {
                // Handle sensor data
                parseSensorData(message)
            }
            message.startsWith("STATUS:") -> {
                // Handle status updates
                parseStatusUpdate(message)
            }
            message.startsWith("ERROR:") -> {
                // Handle error messages
                _connectionState.value = _connectionState.value.copy(
                    error = message.substring(6)
                )
            }
        }
    }
    
    /**
     * Parse sensor data from ESP32
     */
    private fun parseSensorData(message: String) {
        // Example: "SENSOR:ULTRASONIC:25.4"
        val parts = message.split(":")
        if (parts.size >= 3) {
            val sensorType = parts[1]
            val value = parts[2].toFloatOrNull()
            
            // Handle different sensor types
            // This could be expanded based on ESP32 sensors
        }
    }
    
    /**
     * Parse status updates from ESP32
     */
    private fun parseStatusUpdate(message: String) {
        // Example: "STATUS:BATTERY:85"
        val parts = message.split(":")
        if (parts.size >= 3) {
            val statusType = parts[1]
            val value = parts[2]
            
            // Handle different status types
            when (statusType) {
                "BATTERY" -> {
                    // Update battery level
                }
                "MOTOR" -> {
                    // Update motor status
                }
            }
        }
    }
    
    /**
     * Clean up resources
     */
    private fun cleanup() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            // Ignore cleanup errors
        }
        
        inputStream = null
        outputStream = null
        bluetoothSocket = null
    }
    
    /**
     * Clean up when manager is destroyed
     */
    fun destroy() {
        scope.cancel()
        cleanup()
        try {
            context.unregisterReceiver(discoveryReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }
    
    enum class RobotDirection {
        FORWARD, BACKWARD, LEFT, RIGHT, STOP
    }
}