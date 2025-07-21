package com.example.todorevamp.goai

import com.example.todorevamp.network.AIAgentApiService
import com.example.todorevamp.network.AIAgentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud-based AI enhancer that calls your hosted AI agent API
 * API Endpoint: http://98.70.33.52:5000/api/ask
 */
@Singleton
class CloudAiEnhancer @Inject constructor(
    private val aiAgentApiService: AIAgentApiService
) : AiEnhancer {
    
    override suspend fun enhanceTodo(title: String, description: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Combine title and description for the AI query
                val query = buildString {
                    append("Enhance this note with helpful details and subtasks: ")
                    append("Title: $title")
                    if (description.isNotBlank()) {
                        append(", Description: $description")
                    }
                }
                
                val request = AIAgentRequest(query = query)
                val response = aiAgentApiService.enhanceNote(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.response.isNotBlank()) {
                        "🤖 AI Enhancement:\n\n${body.response}"
                    } else {
                        getFallbackEnhancement(title, description)
                    }
                } else {
                    getFallbackEnhancement(title, description)
                }
            } catch (e: Exception) {
                // Fallback to local enhancement if API fails
                getFallbackEnhancement(title, description)
            }
        }
    }
    
    /**
     * Fallback enhancement when API is unavailable
     */
    private fun getFallbackEnhancement(title: String, description: String): String {
        return buildString {
            appendLine("🤖 AI Enhancement (Offline):")
            appendLine()
            
            val content = "$title $description".lowercase()
            
            when {
                content.contains(Regex("(meeting|call|conference)", RegexOption.IGNORE_CASE)) -> {
                    appendLine("📝 Meeting Preparation:")
                    appendLine("• Prepare agenda")
                    appendLine("• Set up meeting link")
                    appendLine("• Send calendar invite")
                    appendLine("• Follow up after meeting")
                }
                content.contains(Regex("(travel|flight|trip|vacation)", RegexOption.IGNORE_CASE)) -> {
                    appendLine("✈️ Travel Planning:")
                    appendLine("• Check passport validity")
                    appendLine("• Book accommodation")
                    appendLine("• Plan itinerary")
                    appendLine("• Check weather forecast")
                    appendLine("• Pack essentials")
                }
                content.contains(Regex("(study|learn|research|read)", RegexOption.IGNORE_CASE)) -> {
                    appendLine("📚 Study Plan:")
                    appendLine("• Gather resources")
                    appendLine("• Create study schedule")
                    appendLine("• Take notes")
                    appendLine("• Practice/Review")
                    appendLine("• Test knowledge")
                }
                content.contains(Regex("(project|work|task)", RegexOption.IGNORE_CASE)) -> {
                    appendLine("💼 Project Management:")
                    appendLine("• Define requirements")
                    appendLine("• Create timeline")
                    appendLine("• Assign responsibilities")
                    appendLine("• Track progress")
                    appendLine("• Review and iterate")
                }
                content.contains(Regex("(buy|shopping|purchase|get)", RegexOption.IGNORE_CASE)) -> {
                    appendLine("🛒 Shopping Plan:")
                    appendLine("• Make a shopping list")
                    appendLine("• Check store hours")
                    appendLine("• Set budget reminder")
                    appendLine("• Compare prices")
                    appendLine("• Check for deals/coupons")
                }
                else -> {
                    appendLine("📋 General Task Breakdown:")
                    appendLine("• Break down into smaller steps")
                    appendLine("• Set deadlines")
                    appendLine("• Gather required resources")
                    appendLine("• Monitor progress")
                    appendLine("• Complete and review")
                }
            }
            
            appendLine()
            appendLine("💡 Note: Enhanced offline. Connect to internet for AI-powered insights!")
        }
    }
}
