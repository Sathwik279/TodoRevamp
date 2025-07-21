package com.example.todorevamp.di

import com.example.todorevamp.network.AIAgentApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * AI Agent API base URL - your cloud server
     */
    private const val AI_AGENT_BASE_URL = "http://98.70.41.127:5000/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            // Add logging interceptor for debugging
            .addInterceptor { chain ->
                val request = chain.request()
                println("🌐 Making request to: ${request.url}")
                val response = chain.proceed(request)
                println("📡 Response code: ${response.code}")
                response
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AI_AGENT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAIAgentApiService(retrofit: Retrofit): AIAgentApiService {
        return retrofit.create(AIAgentApiService::class.java)
    }
}
