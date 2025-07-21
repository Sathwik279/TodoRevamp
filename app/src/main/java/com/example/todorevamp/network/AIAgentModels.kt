package com.example.todorevamp.network

import com.google.gson.annotations.SerializedName

/**
 * Request model for AI Agent API
 */
data class AIAgentRequest(
    @SerializedName("query")
    val query: String
)

/**
 * Response model from AI Agent API
 */
data class AIAgentResponse(
    @SerializedName("response")
    val response: String
)

/**
 * Wrapper for API result with error handling
 */
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String) : ApiResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : ApiResult<T>()
}
