package com.example.todorevamp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( "todo")
data class Todo(
    @ColumnInfo(name="title") val title: String,
    @ColumnInfo(name="description") val description: String,
    @ColumnInfo(name="is_done") val isDone: Boolean,
    @ColumnInfo(name="is_goai_tagged") val isGoAiTagged: Boolean = false,
    @ColumnInfo(name="enhancement_status") val enhancementStatus: String = "none", // "none", "pending", "processing", "completed", "error"
    @ColumnInfo(name="enhanced_content") val enhancedContent: String? = null,
    @ColumnInfo(name="last_updated") val lastUpdated: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)@ColumnInfo(name = "id") val id: Int? = null // ? means it has chance of null
)