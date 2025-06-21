package com.psy.dear.domain.model
import java.time.OffsetDateTime

data class Journal(
    val id: String,
    val title: String,
    val content: String,
    val mood: String,
    val createdAt: OffsetDateTime
)

data class User(
    val id: String,
    val username: String,
    val email: String
)

data class GrowthStatistics(
    val totalJournals: Int,
    val mostFrequentMood: String,
    val averageSentiment: Double
)

// Models for Tests
data class TestOption(val text: String, val value: Int)

data class DassQuestion(val text: String, val scale: String)

data class MbtiQuestion(val text: String, val positivePole: String, val negativePole: String)
