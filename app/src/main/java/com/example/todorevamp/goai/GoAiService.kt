package com.example.todorevamp.goai

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoAiService @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val GO_AI_WORK_NAME = "goai_processing"
        private const val PROCESSING_DELAY_MINUTES = 2L // Short delay for MVP demo
    }
    
    /**
     * Schedule background processing for goAi tagged todos
     */
    fun scheduleProcessing() {
        val workRequest = OneTimeWorkRequestBuilder<GoAiWorker>()
            .setInitialDelay(PROCESSING_DELAY_MINUTES, TimeUnit.MINUTES)
            .build()
            
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                GO_AI_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
    
    /**
     * Cancel any pending goAi processing
     */
    fun cancelProcessing() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(GO_AI_WORK_NAME)
    }
    
    /**
     * Check if goAi feature is enabled (feature flag)
     */
    fun isGoAiEnabled(): Boolean {
        // For MVP, always enabled. In production, this could check:
        // - Build variant (debug/release)
        // - Remote config
        // - User preference
        // - Subscription status
        return true
    }
}
