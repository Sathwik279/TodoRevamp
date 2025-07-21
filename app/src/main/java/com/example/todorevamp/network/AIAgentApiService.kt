package com.example.todorevamp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for AI Agent cloud service
 * Base URL: http://98.70.33.52:5000/
 */
interface AIAgentApiService {
    
    @POST("api/ask")
    suspend fun enhanceNote(
        @Body request: AIAgentRequest
    ): Response<AIAgentResponse>
}
