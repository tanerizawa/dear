package com.psy.dear.data.network.api

import com.psy.dear.data.network.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest)
}

interface JournalApiService {
    @GET("journals")
    suspend fun getJournals(): List<JournalResponse>
    @POST("journals")
    suspend fun createJournal(@Body request: CreateJournalRequest): JournalResponse
}

interface ChatApiService {
    @POST("chat")
    suspend fun postMessage(@Body request: ChatRequest): ChatResponse
}

interface UserApiService {
    @GET("users/me")
    suspend fun getProfile(): UserProfileResponse
}
