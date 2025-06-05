package com.smartbot.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Manages speech recognition functionality
 */
class SpeechRecognitionManager(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    private val _recognitionState = MutableStateFlow(RecognitionState())
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()
    
    data class RecognitionState(
        val isListening: Boolean = false,
        val isAvailable: Boolean = false,
        val lastResult: String = "",
        val error: String? = null,
        val confidence: Float = 0f
    )
    
    init {
        checkAvailability()
    }
    
    /**
     * Check if speech recognition is available on the device
     */
    private fun checkAvailability() {
        val isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        _recognitionState.value = _recognitionState.value.copy(isAvailable = isAvailable)
    }
    
    /**
     * Start listening for speech input
     */
    fun startListening(
        language: String = Locale.getDefault().language,
        onResult: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (!_recognitionState.value.isAvailable) {
            onError("Speech recognition not available")
            return
        }
        
        if (isListening) {
            stopListening()
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                _recognitionState.value = _recognitionState.value.copy(
                    isListening = true,
                    error = null
                )
            }
            
            override fun onBeginningOfSpeech() {
                // Speech input has begun
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - could be used for visual feedback
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }
            
            override fun onEndOfSpeech() {
                isListening = false
                _recognitionState.value = _recognitionState.value.copy(isListening = false)
            }
            
            override fun onError(error: Int) {
                isListening = false
                val errorMessage = getErrorMessage(error)
                _recognitionState.value = _recognitionState.value.copy(
                    isListening = false,
                    error = errorMessage
                )
                onError(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    val result = matches[0]
                    val confidence = confidences?.get(0) ?: 0f
                    
                    _recognitionState.value = _recognitionState.value.copy(
                        isListening = false,
                        lastResult = result,
                        confidence = confidence,
                        error = null
                    )
                    
                    onResult(result)
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    // Could be used for real-time transcription display
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle speech recognition events
            }
        })
        
        speechRecognizer?.startListening(intent)
    }
    
    /**
     * Stop listening for speech input
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
        _recognitionState.value = _recognitionState.value.copy(isListening = false)
    }
    
    /**
     * Cancel speech recognition
     */
    fun cancelListening() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
        _recognitionState.value = _recognitionState.value.copy(isListening = false)
    }
    
    /**
     * Check if currently listening
     */
    fun isCurrentlyListening(): Boolean = isListening
    
    /**
     * Get supported languages for speech recognition
     */
    fun getSupportedLanguages(): List<String> {
        return listOf(
            "en-US", "en-GB", "es-ES", "fr-FR", "de-DE", 
            "it-IT", "pt-BR", "ru-RU", "ja-JP", "ko-KR", "zh-CN"
        )
    }
    
    /**
     * Convert error code to human-readable message
     */
    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech input matched"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
            else -> "Unknown error occurred"
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        stopListening()
    }
}