package com.example.todorevamp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo:Todo)

    @Query("SELECT * from todo where id = :id")
    suspend fun getTodoById(id: Int): Todo? // nullable to handle case where no recode is found we need to handle nullability or app will crash

    @Query("select * from todo")
    fun getTodos(): Flow<List<Todo>>

    // goAi specific queries
    @Query("SELECT * FROM todo WHERE is_goai_tagged = 1 AND enhancement_status = 'pending'")
    suspend fun getPendingGoAiTodos(): List<Todo>

    @Query("SELECT * FROM todo WHERE is_goai_tagged = 1 AND enhancement_status = 'completed' AND last_updated > :since")
    suspend fun getRecentlyProcessedTodos(since: Long): List<Todo>

    @Query("UPDATE todo SET enhancement_status = :status WHERE id = :id")
    suspend fun updateEnhancementStatus(id: Int, status: String)

}