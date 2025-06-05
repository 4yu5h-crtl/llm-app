package com.smartbot.ai

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages conversation context and handles educational interactions
 */
class ConversationManager(private val context: Context) {
    
    private val llmManager = LLMManager(context)
    
    private val _conversationState = MutableStateFlow(ConversationState())
    val conversationState: StateFlow<ConversationState> = _conversationState.asStateFlow()
    
    data class ConversationState(
        val isActive: Boolean = false,
        val currentTopic: String = "",
        val conversationHistory: List<String> = emptyList(),
        val educationalMode: EducationalMode = EducationalMode.GENERAL,
        val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER
    )
    
    enum class EducationalMode {
        GENERAL,
        MATH,
        SCIENCE,
        HISTORY,
        LANGUAGE,
        PROGRAMMING,
        ROBOTICS
    }
    
    enum class DifficultyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }
    
    /**
     * Start a new conversation session
     */
    suspend fun startConversation(mode: EducationalMode = EducationalMode.GENERAL): Result<String> {
        try {
            _conversationState.value = _conversationState.value.copy(
                isActive = true,
                educationalMode = mode,
                conversationHistory = emptyList()
            )
            
            val welcomeMessage = generateWelcomeMessage(mode)
            addToHistory("SYSTEM: $welcomeMessage")
            
            return Result.success(welcomeMessage)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Process user input and generate educational response
     */
    suspend fun processUserInput(input: String): Result<String> {
        try {
            if (!_conversationState.value.isActive) {
                return Result.failure(Exception("Conversation not active"))
            }
            
            addToHistory("USER: $input")
            
            // Analyze input for educational context
            val context = analyzeEducationalContext(input)
            
            // Generate contextual prompt
            val prompt = buildEducationalPrompt(input, context)
            
            // Generate response using LLM
            val response = llmManager.generateResponse(prompt)
            
            return response.fold(
                onSuccess = { generatedResponse ->
                    val processedResponse = postProcessResponse(generatedResponse, context)
                    addToHistory("ASSISTANT: $processedResponse")
                    Result.success(processedResponse)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Set educational mode
     */
    fun setEducationalMode(mode: EducationalMode) {
        _conversationState.value = _conversationState.value.copy(educationalMode = mode)
    }
    
    /**
     * Set difficulty level
     */
    fun setDifficultyLevel(level: DifficultyLevel) {
        _conversationState.value = _conversationState.value.copy(difficulty = level)
    }
    
    /**
     * End conversation session
     */
    fun endConversation() {
        _conversationState.value = _conversationState.value.copy(
            isActive = false,
            conversationHistory = emptyList()
        )
    }
    
    /**
     * Get conversation summary
     */
    fun getConversationSummary(): String {
        val history = _conversationState.value.conversationHistory
        return if (history.isNotEmpty()) {
            "Conversation covered: ${_conversationState.value.currentTopic}\n" +
            "Messages exchanged: ${history.size}\n" +
            "Mode: ${_conversationState.value.educationalMode}\n" +
            "Difficulty: ${_conversationState.value.difficulty}"
        } else {
            "No conversation history"
        }
    }
    
    private fun addToHistory(message: String) {
        val currentHistory = _conversationState.value.conversationHistory
        _conversationState.value = _conversationState.value.copy(
            conversationHistory = currentHistory + message
        )
    }
    
    private fun generateWelcomeMessage(mode: EducationalMode): String {
        return when (mode) {
            EducationalMode.GENERAL -> "Hello! I'm SmartBot, your educational assistant. What would you like to learn about today?"
            EducationalMode.MATH -> "Welcome to math mode! I can help you with arithmetic, algebra, geometry, and more. What math topic interests you?"
            EducationalMode.SCIENCE -> "Science mode activated! Let's explore physics, chemistry, biology, or any other scientific topic. What would you like to discover?"
            EducationalMode.HISTORY -> "History mode ready! I can tell you about historical events, figures, and civilizations. What period or topic interests you?"
            EducationalMode.LANGUAGE -> "Language learning mode! I can help with vocabulary, grammar, and conversation practice. Which language are we working on?"
            EducationalMode.PROGRAMMING -> "Programming mode engaged! I can help with coding concepts, algorithms, and problem-solving. What programming topic shall we explore?"
            EducationalMode.ROBOTICS -> "Robotics mode active! Let's learn about robot mechanics, sensors, programming, and control systems. What aspect of robotics interests you?"
        }
    }
    
    private fun analyzeEducationalContext(input: String): EducationalContext {
        // Simple keyword-based analysis (could be enhanced with NLP)
        val keywords = input.lowercase().split(" ")
        
        val mathKeywords = listOf("math", "calculate", "equation", "number", "algebra", "geometry")
        val scienceKeywords = listOf("science", "physics", "chemistry", "biology", "experiment", "theory")
        val historyKeywords = listOf("history", "historical", "ancient", "war", "civilization", "century")
        val programmingKeywords = listOf("code", "programming", "algorithm", "function", "variable", "loop")
        val roboticsKeywords = listOf("robot", "sensor", "motor", "control", "automation", "mechanical")
        
        val detectedMode = when {
            keywords.any { it in mathKeywords } -> EducationalMode.MATH
            keywords.any { it in scienceKeywords } -> EducationalMode.SCIENCE
            keywords.any { it in historyKeywords } -> EducationalMode.HISTORY
            keywords.any { it in programmingKeywords } -> EducationalMode.PROGRAMMING
            keywords.any { it in roboticsKeywords } -> EducationalMode.ROBOTICS
            else -> _conversationState.value.educationalMode
        }
        
        return EducationalContext(
            detectedMode = detectedMode,
            isQuestion = input.contains("?"),
            isRequest = keywords.any { it in listOf("explain", "tell", "show", "how", "what", "why") },
            complexity = estimateComplexity(input)
        )
    }
    
    private fun buildEducationalPrompt(input: String, context: EducationalContext): String {
        val mode = context.detectedMode
        val difficulty = _conversationState.value.difficulty
        val history = _conversationState.value.conversationHistory.takeLast(6).joinToString("\n")
        
        return """
            You are SmartBot, an educational robot assistant. You are currently in ${mode.name} mode with ${difficulty.name} difficulty level.
            
            Context from recent conversation:
            $history
            
            User input: $input
            
            Please provide an educational response that:
            1. Is appropriate for ${difficulty.name} level
            2. Focuses on ${mode.name} topics when relevant
            3. Is encouraging and engaging
            4. Includes practical examples when possible
            5. Suggests follow-up questions or activities
            
            Keep responses concise but informative (2-3 sentences max).
        """.trimIndent()
    }
    
    private fun postProcessResponse(response: String, context: EducationalContext): String {
        // Clean up and enhance the response
        var processedResponse = response.trim()
        
        // Remove any unwanted prefixes
        processedResponse = processedResponse.removePrefix("SmartBot:")
            .removePrefix("Assistant:")
            .removePrefix("Response:")
            .trim()
        
        // Add encouraging elements for educational context
        if (context.isQuestion && !processedResponse.contains("?")) {
            processedResponse += " Would you like to explore this topic further?"
        }
        
        return processedResponse
    }
    
    private fun estimateComplexity(input: String): Int {
        // Simple complexity estimation based on length and vocabulary
        val words = input.split(" ")
        val avgWordLength = words.map { it.length }.average()
        
        return when {
            words.size < 5 && avgWordLength < 5 -> 1 // Simple
            words.size < 15 && avgWordLength < 7 -> 2 // Medium
            else -> 3 // Complex
        }
    }
    
    data class EducationalContext(
        val detectedMode: EducationalMode,
        val isQuestion: Boolean,
        val isRequest: Boolean,
        val complexity: Int
    )
}