package com.example.todorevamp.repository

import com.example.todorevamp.network.AIAgentApiService
import com.example.todorevamp.network.AIAgentRequest
import com.example.todorevamp.network.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIAgentRepository @Inject constructor(
    private val aiAgentApiService: AIAgentApiService
) {
    
    /**
     * Enhance a note using the cloud AI agent
     * @param noteText The original note content to enhance
     * @return ApiResult with enhanced text or error
     */
    suspend fun enhanceNote(noteText: String): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AIAgentRequest(query = noteText)
                val response = aiAgentApiService.enhanceNote(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ApiResult.Success(body.response)
                    } else {
                        ApiResult.Error("Empty response from AI agent")
                    }
                } else {
                    ApiResult.Error("API call failed: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                ApiResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }
    
    /**
     * Get weather information for a specific location
     * @param location The location to get weather for
     * @return ApiResult with weather information or error
     */
    suspend fun getWeatherInfo(location: String): ApiResult<String> {
        return enhanceNote("current weather in $location")
    }
    
    /**
     * Get general AI assistance for any query
     * @param query The user's question or request
     * @return ApiResult with AI response or error
     */
    suspend fun getAIAssistance(query: String): ApiResult<String> {
        return enhanceNote(query)
    }
}
