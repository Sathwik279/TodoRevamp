package com.example.todorevamp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Todo::class],
    version = 3
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
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new fields for pin, tags, and images
                database.execSQL("ALTER TABLE todo ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE todo ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE todo ADD COLUMN image_paths TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}