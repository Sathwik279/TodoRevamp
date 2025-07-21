package com.example.todorevamp.goai

/**
 * Interface for AI enhancement functionality
 * This allows easy swapping between local stub and server implementation
 */
interface AiEnhancer {
    suspend fun enhanceTodo(title: String, description: String): String
}

/**
 * Local stub implementation for MVP
 * Simulates AI enhancement with predefined patterns
 */
class LocalStubEnhancer : AiEnhancer {
    override suspend fun enhanceTodo(title: String, description: String): String {
        // Simulate processing delay
        kotlinx.coroutines.delay(2000)
        
        return buildString {
            appendLine("🤖 AI Enhancement:")
            appendLine()
            
            // Task breakdown simulation
            if (title.contains(Regex("(buy|shopping|store|get)", RegexOption.IGNORE_CASE))) {
                appendLine("📝 Suggested breakdown:")
                appendLine("• Make a shopping list")
                appendLine("• Check store hours")
                appendLine("• Set budget reminder")
                appendLine()
            }
            
            // Priority analysis
            val priority = when {
                title.contains(Regex("(urgent|asap|important|deadline)", RegexOption.IGNORE_CASE)) -> "High"
                title.contains(Regex("(later|someday|maybe)", RegexOption.IGNORE_CASE)) -> "Low"
                else -> "Medium"
            }
            appendLine("⚡ Priority: $priority")
            appendLine()
            
            // Smart suggestions
            appendLine("💡 Smart suggestions:")
            when {
                description.contains(Regex("(meeting|call|appointment)", RegexOption.IGNORE_CASE)) -> {
                    appendLine("• Set a reminder 15 minutes before")
                    appendLine("• Prepare agenda items")
                }
                description.contains(Regex("(workout|exercise|gym)", RegexOption.IGNORE_CASE)) -> {
                    appendLine("• Check weather if outdoor activity")
                    appendLine("• Prepare workout gear")
                }
                else -> {
                    appendLine("• Break down into smaller steps")
                    appendLine("• Set a specific deadline")
                }
            }
            appendLine()
            
            // Contextual tips
            appendLine("🎯 Pro tip: Consider setting a specific time and location for better completion rates!")
        }
    }
}

/**
 * Placeholder for future server implementation
 */
class ServerAiEnhancer : AiEnhancer {
    override suspend fun enhanceTodo(title: String, description: String): String {
        // TODO: Implement actual API call to backend AI service
        // This could use Retrofit to call your goAi backend
        throw NotImplementedError("Server implementation coming soon")
    }
}
