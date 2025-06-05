package com.smartbot.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Manages text-to-speech functionality
 */
class TextToSpeechManager(private val context: Context) {
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    
    private val _ttsState = MutableStateFlow(TTSState())
    val ttsState: StateFlow<TTSState> = _ttsState.asStateFlow()
    
    data class TTSState(
        val isInitialized: Boolean = false,
        val isSpeaking: Boolean = false,
        val currentLanguage: Locale = Locale.getDefault(),
        val speechRate: Float = 1.0f,
        val pitch: Float = 1.0f,
        val error: String? = null
    )
    
    init {
        initializeTTS()
    }
    
    /**
     * Initialize the TextToSpeech engine
     */
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                
                // Set default language
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                
                val languageSupported = when (result) {
                    TextToSpeech.LANG_MISSING_DATA,
                    TextToSpeech.LANG_NOT_SUPPORTED -> false
                    else -> true
                }
                
                _ttsState.value = _ttsState.value.copy(
                    isInitialized = languageSupported,
                    error = if (!languageSupported) "Language not supported" else null
                )
                
                // Set up utterance progress listener
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _ttsState.value = _ttsState.value.copy(isSpeaking = true)
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        _ttsState.value = _ttsState.value.copy(isSpeaking = false)
                    }
                    
                    override fun onError(utteranceId: String?) {
                        _ttsState.value = _ttsState.value.copy(
                            isSpeaking = false,
                            error = "Speech synthesis error"
                        )
                    }
                })
            } else {
                _ttsState.value = _ttsState.value.copy(
                    isInitialized = false,
                    error = "TextToSpeech initialization failed"
                )
            }
        }
    }
    
    /**
     * Speak the given text
     */
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        utteranceId: String = "smartbot_utterance_${System.currentTimeMillis()}"
    ): Boolean {
        if (!isInitialized || textToSpeech == null) {
            _ttsState.value = _ttsState.value.copy(error = "TTS not initialized")
            return false
        }
        
        val result = textToSpeech?.speak(text, queueMode, null, utteranceId)
        
        return when (result) {
            TextToSpeech.SUCCESS -> {
                _ttsState.value = _ttsState.value.copy(error = null)
                true
            }
            TextToSpeech.ERROR -> {
                _ttsState.value = _ttsState.value.copy(error = "Speech synthesis failed")
                false
            }
            else -> false
        }
    }
    
    /**
     * Stop speaking
     */
    fun stop() {
        textToSpeech?.stop()
        _ttsState.value = _ttsState.value.copy(isSpeaking = false)
    }
    
    /**
     * Set speech rate (0.5 to 2.0, where 1.0 is normal)
     */
    fun setSpeechRate(rate: Float): Boolean {
        if (!isInitialized || textToSpeech == null) return false
        
        val clampedRate = rate.coerceIn(0.5f, 2.0f)
        val result = textToSpeech?.setSpeechRate(clampedRate)
        
        if (result == TextToSpeech.SUCCESS) {
            _ttsState.value = _ttsState.value.copy(speechRate = clampedRate)
            return true
        }
        return false
    }
    
    /**
     * Set pitch (0.5 to 2.0, where 1.0 is normal)
     */
    fun setPitch(pitch: Float): Boolean {
        if (!isInitialized || textToSpeech == null) return false
        
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        val result = textToSpeech?.setPitch(clampedPitch)
        
        if (result == TextToSpeech.SUCCESS) {
            _ttsState.value = _ttsState.value.copy(pitch = clampedPitch)
            return true
        }
        return false
    }
    
    /**
     * Set language
     */
    fun setLanguage(locale: Locale): Boolean {
        if (!isInitialized || textToSpeech == null) return false
        
        val result = textToSpeech?.setLanguage(locale)
        
        return when (result) {
            TextToSpeech.LANG_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                _ttsState.value = _ttsState.value.copy(
                    currentLanguage = locale,
                    error = null
                )
                true
            }
            TextToSpeech.LANG_MISSING_DATA -> {
                _ttsState.value = _ttsState.value.copy(error = "Language data missing")
                false
            }
            TextToSpeech.LANG_NOT_SUPPORTED -> {
                _ttsState.value = _ttsState.value.copy(error = "Language not supported")
                false
            }
            else -> false
        }
    }
    
    /**
     * Get available languages
     */
    fun getAvailableLanguages(): Set<Locale> {
        return textToSpeech?.availableLanguages ?: emptySet()
    }
    
    /**
     * Check if language is supported
     */
    fun isLanguageSupported(locale: Locale): Boolean {
        if (!isInitialized || textToSpeech == null) return false
        
        val result = textToSpeech?.isLanguageAvailable(locale)
        return when (result) {
            TextToSpeech.LANG_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
            else -> false
        }
    }
    
    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }
    
    /**
     * Get common educational phrases for quick access
     */
    fun getEducationalPhrases(): Map<String, String> {
        return mapOf(
            "welcome" to "Welcome to SmartBot! I'm here to help you learn.",
            "good_job" to "Great job! You're doing excellent work.",
            "try_again" to "That's not quite right. Let's try again!",
            "explain_more" to "Would you like me to explain that in more detail?",
            "next_topic" to "Ready to move on to the next topic?",
            "question" to "Do you have any questions about what we just learned?",
            "encouragement" to "Keep up the great work! Learning is a journey.",
            "goodbye" to "Thanks for learning with me today. See you next time!"
        )
    }
    
    /**
     * Speak an educational phrase
     */
    fun speakEducationalPhrase(phraseKey: String): Boolean {
        val phrases = getEducationalPhrases()
        val phrase = phrases[phraseKey] ?: return false
        return speak(phrase)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        _ttsState.value = TTSState()
    }
}