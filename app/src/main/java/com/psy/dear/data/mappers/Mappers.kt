package com.psy.dear.data.mappers

import com.psy.dear.data.local.entity.ChatMessageEntity
import com.psy.dear.data.local.entity.JournalEntity
import com.psy.dear.domain.model.ChatMessage
import com.psy.dear.domain.model.Journal
import com.psy.dear.data.network.dto.*
import com.psy.dear.domain.model.Article
import com.psy.dear.domain.model.AudioTrack
import com.psy.dear.domain.model.MotivationalQuote
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

fun JournalResponse.toEntity(): JournalEntity {
    // -- REVISI DI SINI ---
    // 1. Parse sebagai LocalDateTime (yang tidak peduli timezone)
    val localDateTime = LocalDateTime.parse(this.createdAt)
    // 2. Konversi ke OffsetDateTime dengan asumsi UTC (atau offset lain jika perlu)
    val offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC)

    return JournalEntity(
        id = this.id,
        title = this.title,
        content = this.content,
        mood = this.mood,
        sentimentScore = this.sentimentScore,
        sentimentLabel = this.sentimentLabel,
        createdAt = offsetDateTime, // Gunakan hasil konversi
        isSynced = true
    )
}

fun JournalEntity.toDomain(): Journal {
    return Journal(
        id = this.id,
        title = this.title,
        content = this.content,
        mood = this.mood,
        createdAt = this.createdAt
    )
}

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = this.id,
        role = this.role,
        content = this.content,
        emotion = this.emotion,
        timestamp = this.timestamp
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = this.id,
        role = this.role,
        content = this.content,
        emotion = this.emotion,
        timestamp = this.timestamp,
        isFlagged = false
    )
}

fun ArticleResponse.toDomain() = Article(id, title, url)
fun AudioTrackResponse.toDomain() = AudioTrack(id, title, url)
fun MotivationalQuoteResponse.toDomain() = MotivationalQuote(id, text, author)