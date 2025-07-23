package com.example.todorevamp.data

import kotlinx.coroutines.flow.Flow

interface TodoRepository {

    suspend fun insertTodo(todo: Todo)

    suspend fun deleteTodo(todo: Todo)

    suspend fun getTodoById(id: Int): Todo? // here we are using nullable to stop app from crashing

    fun getTodos(): Flow<List<Todo>> // flow means we get realtime updates when something changes and these help to update the ui

    fun searchTodos(query: String): Flow<List<Todo>> // search todos including tags

    // goAi specific methods
    suspend fun getPendingGoAiTodos(): List<Todo>
    
    suspend fun getRecentlyProcessedTodos(since: Long): List<Todo>
    
    suspend fun updateEnhancementStatus(id: Int, status: String)

}