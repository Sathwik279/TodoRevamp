package com.example.todorevamp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Todo::class],
    version = 2
)
abstract class TodoDatabase : RoomDatabase(){
    abstract val dao: TodoDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add goAi fields to existing todo table
                database.execSQL("ALTER TABLE todo ADD COLUMN is_goai_tagged INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE todo ADD COLUMN enhancement_status TEXT NOT NULL DEFAULT 'none'")
                database.execSQL("ALTER TABLE todo ADD COLUMN enhanced_content TEXT")
                database.execSQL("ALTER TABLE todo ADD COLUMN last_updated INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}