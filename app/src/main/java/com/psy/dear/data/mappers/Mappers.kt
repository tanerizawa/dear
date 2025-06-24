package com.psy.dear.data.mappers

import com.psy.dear.data.local.entity.ChatMessageEntity
import com.psy.dear.data.local.entity.JournalEntity
import com.psy.dear.domain.model.ChatMessage
import com.psy.dear.domain.model.Journal
import com.psy.dear.data.network.dto.*
import com.psy.dear.domain.model.Article
import com.psy.dear.domain.model.AudioTrack
import com.psy.dear.domain.model.MotivationalQuote
import java.time.OffsetDateTime
import java.util.*

fun JournalResponse.toEntity(): JournalEntity {
    return JournalEntity(
        id = this.id,
        title = this.title,
        content = this.content,
        mood = this.mood,
        sentimentScore = this.sentimentScore,
        sentimentLabel = this.sentimentLabel,
        createdAt = OffsetDateTime.parse(this.createdAt),
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
