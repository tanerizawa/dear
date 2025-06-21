package com.psy.dear.data.network.dto
import com.google.gson.annotations.SerializedName

// AuthDtos
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class AuthResponse(@SerializedName("access_token") val accessToken: String)

// JournalDtos
data class JournalResponse(
    val id: String,
    val title: String,
    val content: String,
    val mood: String,
    @SerializedName("sentiment_score") val sentimentScore: Double?,
    @SerializedName("sentiment_label") val sentimentLabel: String?,
    @SerializedName("created_at") val createdAt: String
)
data class CreateJournalRequest(val title: String, val content: String, val mood: String)

// ChatDtos
data class ChatRequest(val message: String)
data class ChatResponse(val reply: String)

// UserDtos
data class UserProfileResponse(
    val id: String,
    val username: String,
    val email: String
)
