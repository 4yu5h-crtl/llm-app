package com.smartbot.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manager class for handling local LLM operations
 * Supports TinyLLaMA, Phi-2, and Mistral models
 */
class LLMManager(private val context: Context) {
    
    private var isModelLoaded = false
    private var currentModel: LLMModel? = null
    
    enum class ModelType {
        TINY_LLAMA,
        PHI_2,
        MISTRAL_7B
    }
    
    data class LLMModel(
        val type: ModelType,
        val name: String,
        val filePath: String,
        val sizeInMB: Int
    )
    
    companion object {
        private const val MODELS_DIR = "models"
        
        val AVAILABLE_MODELS = listOf(
            LLMModel(ModelType.TINY_LLAMA, "TinyLLaMA 1.1B", "tinyllama-1.1b-chat-v1.0.q4_k_m.gguf", 669),
            LLMModel(ModelType.PHI_2, "Phi-2 2.7B", "phi-2.q4_k_m.gguf", 1560),
            LLMModel(ModelType.MISTRAL_7B, "Mistral 7B", "mistral-7b-instruct-v0.1.q4_k_m.gguf", 4080)
        )
        
        // Load native library
        init {
            try {
                System.loadLibrary("llama")
            } catch (e: UnsatisfiedLinkError) {
                // Handle library loading error
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Load a specific model
     */
    suspend fun loadModel(modelType: ModelType): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val model = AVAILABLE_MODELS.find { it.type == modelType }
                ?: return@withContext Result.failure(Exception("Model not found"))
            
            val modelFile = File(context.filesDir, "${MODELS_DIR}/${model.filePath}")
            
            if (!modelFile.exists()) {
                return@withContext Result.failure(Exception("Model file not found: ${model.filePath}"))
            }
            
            // Initialize the model using JNI
            val success = nativeLoadModel(modelFile.absolutePath)
            
            if (success) {
                currentModel = model
                isModelLoaded = true
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to load model"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate response from the loaded model
     */
    suspend fun generateResponse(
        prompt: String,
        maxTokens: Int = 256,
        temperature: Float = 0.7f
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isModelLoaded) {
                return@withContext Result.failure(Exception("No model loaded"))
            }
            
            val response = nativeGenerateResponse(prompt, maxTokens, temperature)
            
            if (response.isNotEmpty()) {
                Result.success(response)
            } else {
                Result.failure(Exception("Empty response from model"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a model is currently loaded
     */
    fun isModelLoaded(): Boolean = isModelLoaded
    
    /**
     * Get the currently loaded model
     */
    fun getCurrentModel(): LLMModel? = currentModel
    
    /**
     * Unload the current model
     */
    suspend fun unloadModel(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (isModelLoaded) {
                nativeUnloadModel()
                isModelLoaded = false
                currentModel = null
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a model file exists
     */
    fun isModelDownloaded(modelType: ModelType): Boolean {
        val model = AVAILABLE_MODELS.find { it.type == modelType } ?: return false
        val modelFile = File(context.filesDir, "${MODELS_DIR}/${model.filePath}")
        return modelFile.exists()
    }
    
    /**
     * Get model file size
     */
    fun getModelFileSize(modelType: ModelType): Long {
        val model = AVAILABLE_MODELS.find { it.type == modelType } ?: return 0L
        val modelFile = File(context.filesDir, "${MODELS_DIR}/${model.filePath}")
        return if (modelFile.exists()) modelFile.length() else 0L
    }
    
    // Native methods (implemented in C++ via JNI)
    private external fun nativeLoadModel(modelPath: String): Boolean
    private external fun nativeGenerateResponse(prompt: String, maxTokens: Int, temperature: Float): String
    private external fun nativeUnloadModel(): Boolean
}