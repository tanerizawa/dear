package com.psy.dear.data.network.api

import com.psy.dear.data.network.dto.*
import retrofit2.http.*

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
    @POST("chat/")
    suspend fun postMessage(@Body request: ChatRequest): ChatResponse

    @DELETE("chat/{id}")
    suspend fun deleteMessage(@Path("id") id: String)

    @PATCH("chat/{id}/flag")
    suspend fun setFlag(
        @Path("id") id: String,
        @Body request: FlagRequest
    )
}

interface UserApiService {
    @GET("users/me")
    suspend fun getProfile(): UserProfileResponse
}

interface ContentApiService {
    @GET("articles")
    suspend fun getArticles(): List<ArticleResponse>

    @GET("audio")
    suspend fun getAudio(): List<AudioTrackResponse>

    @GET("music")
    suspend fun getMoodMusic(@Query("mood") mood: String): List<AudioTrackResponse>

    @GET("music/recommend")
    suspend fun getRecommendedMusic(): List<AudioTrackResponse>

    @GET("quotes")
    suspend fun getQuotes(): List<MotivationalQuoteResponse>
}
