package com.example.todorevamp.data

import kotlinx.coroutines.flow.Flow

class TodoRepositoryImplementation(
    private val dao: TodoDao
): TodoRepository {

    override suspend fun insertTodo(todo: Todo) {
        return dao.insertTodo(todo)
    }

    override suspend fun deleteTodo(todo: Todo) {
        return dao.deleteTodo(todo)
    }

    override suspend fun getTodoById(id: Int): Todo? {
        return dao.getTodoById(id)
    }

    override fun getTodos(): Flow<List<Todo>> {
        return dao.getTodos()
    }

    override fun searchTodos(query: String): Flow<List<Todo>> {
        return dao.searchTodos(query)
    }

    override suspend fun getPendingGoAiTodos(): List<Todo> {
        return dao.getPendingGoAiTodos()
    }

    override suspend fun getRecentlyProcessedTodos(since: Long): List<Todo> {
        return dao.getRecentlyProcessedTodos(since)
    }

    override suspend fun updateEnhancementStatus(id: Int, status: String) {
        return dao.updateEnhancementStatus(id, status)
    }

}