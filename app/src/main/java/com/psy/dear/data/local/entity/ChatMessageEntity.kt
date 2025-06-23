package com.psy.dear.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val emotion: String?,
    val timestamp: OffsetDateTime
)
