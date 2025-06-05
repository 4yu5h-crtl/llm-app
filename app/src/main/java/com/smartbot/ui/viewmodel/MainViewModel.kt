package com.smartbot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppState(
    val isConnectedToRobot: Boolean = false,
    val isLLMLoaded: Boolean = false,
    val isSpeechRecognitionActive: Boolean = false,
    val currentMessage: String = "",
    val chatHistory: List<ChatMessage> = emptyList(),
    val sensorData: SensorData = SensorData(),
    val robotStatus: RobotStatus = RobotStatus()
)

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class SensorData(
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 0f,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f,
    val magnetometerX: Float = 0f,
    val magnetometerY: Float = 0f,
    val magnetometerZ: Float = 0f
)

data class RobotStatus(
    val batteryLevel: Int = 0,
    val motorLeftSpeed: Int = 0,
    val motorRightSpeed: Int = 0,
    val isMoving: Boolean = false,
    val lastCommand: String = ""
)

class MainViewModel : ViewModel() {
    
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    fun updateConnectionStatus(isConnected: Boolean) {
        _appState.value = _appState.value.copy(isConnectedToRobot = isConnected)
    }
    
    fun updateLLMStatus(isLoaded: Boolean) {
        _appState.value = _appState.value.copy(isLLMLoaded = isLoaded)
    }
    
    fun addChatMessage(message: String, isFromUser: Boolean) {
        val newMessage = ChatMessage(message, isFromUser)
        val updatedHistory = _appState.value.chatHistory + newMessage
        _appState.value = _appState.value.copy(chatHistory = updatedHistory)
    }
    
    fun updateCurrentMessage(message: String) {
        _appState.value = _appState.value.copy(currentMessage = message)
    }
    
    fun updateSensorData(sensorData: SensorData) {
        _appState.value = _appState.value.copy(sensorData = sensorData)
    }
    
    fun updateRobotStatus(robotStatus: RobotStatus) {
        _appState.value = _appState.value.copy(robotStatus = robotStatus)
    }
    
    fun sendMessageToLLM(message: String) {
        viewModelScope.launch {
            addChatMessage(message, true)
            // TODO: Implement LLM processing
            // For now, just echo back
            addChatMessage("Echo: $message", false)
        }
    }
    
    fun sendRobotCommand(command: String) {
        viewModelScope.launch {
            // TODO: Implement robot communication
            val updatedStatus = _appState.value.robotStatus.copy(lastCommand = command)
            updateRobotStatus(updatedStatus)
        }
    }
}