package com.example.todorevamp.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.todorevamp.data.TodoDatabase
import com.example.todorevamp.data.TodoRepository
import com.example.todorevamp.data.TodoRepositoryImplementation
import com.example.todorevamp.goai.AiEnhancer
import com.example.todorevamp.goai.CloudAiEnhancer
import com.example.todorevamp.goai.LocalStubEnhancer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(app: Application): TodoDatabase {
        return Room.databaseBuilder(
            app,
            TodoDatabase::class.java,
            "todo_db"
        ).addMigrations(
            TodoDatabase.MIGRATION_1_2,
            TodoDatabase.MIGRATION_2_3
        ).build()

    }

    @Provides
    @Singleton
    fun provideTodoRepository(db: TodoDatabase): TodoRepository {
        return TodoRepositoryImplementation(db.dao)
    }

    @Provides
    @Singleton
    fun provideAiEnhancer(cloudAiEnhancer: CloudAiEnhancer): AiEnhancer = cloudAiEnhancer

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context
}