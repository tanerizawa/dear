package com.psy.dear.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val mood: String,
    val sentimentScore: Double?,
    val sentimentLabel: String?,
    val createdAt: OffsetDateTime,
    val isSynced: Boolean
)
