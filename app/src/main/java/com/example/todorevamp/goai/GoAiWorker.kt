package com.example.todorevamp.goai

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todorevamp.data.TodoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class GoAiWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: TodoRepository,
    private val aiEnhancer: AiEnhancer
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get all pending todos
            val pendingTodos = repository.getPendingGoAiTodos()
            
            if (pendingTodos.isEmpty()) {
                return@withContext Result.success()
            }
            
            // Process each todo
            pendingTodos.forEach { todo ->
                try {
                    // Update status to processing
                    repository.updateEnhancementStatus(todo.id!!, "processing")
                    
                    // Generate AI enhancement
                    val enhancement = aiEnhancer.enhanceTodo(todo.title, todo.description)
                    
                    // Update todo with enhancement
                    val enhancedTodo = todo.copy(
                        enhancementStatus = "completed",
                        enhancedContent = enhancement,
                        lastUpdated = System.currentTimeMillis()
                    )
                    
                    repository.insertTodo(enhancedTodo)
                    
                } catch (e: Exception) {
                    // Mark as error if enhancement fails
                    repository.updateEnhancementStatus(todo.id!!, "error")
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
