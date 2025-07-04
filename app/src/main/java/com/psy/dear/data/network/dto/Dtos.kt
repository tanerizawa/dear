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
data class ChatResponse(
    @SerializedName("content")
    val reply: String,
    val emotion: String?
)

data class FlagRequest(
    @SerializedName("is_flagged") val isFlagged: Boolean
)

// UserDtos
data class UserProfileResponse(
    val id: String,
    val username: String,
    val email: String
)

// ContentDtos
data class ArticleResponse(val id: String, val title: String, val url: String)
data class AudioTrackResponse(val id: String, val title: String, val url: String)
data class MotivationalQuoteResponse(val id: String, val text: String, val author: String)
