package com.psy.dear.data.repository

import com.psy.dear.core.Result
import com.psy.dear.data.datastore.UserPreferencesRepository
import com.psy.dear.data.local.dao.ChatMessageDao
import com.psy.dear.data.local.dao.JournalDao
import com.psy.dear.data.mappers.toDomain
import com.psy.dear.data.mappers.toEntity
import com.psy.dear.data.network.api.AuthApiService
import com.psy.dear.data.network.api.ChatApiService
import com.psy.dear.data.network.api.JournalApiService
import com.psy.dear.data.network.api.UserApiService
import com.psy.dear.data.network.api.ContentApiService
import com.psy.dear.core.UnauthorizedException
import com.psy.dear.data.network.dto.ChatRequest
import com.psy.dear.data.network.dto.CreateJournalRequest
import com.psy.dear.data.network.dto.LoginRequest
import com.psy.dear.data.network.dto.RegisterRequest
import com.psy.dear.data.network.dto.FlagRequest
import com.psy.dear.domain.model.*
import com.psy.dear.domain.repository.AuthRepository
import com.psy.dear.domain.repository.ChatRepository
import com.psy.dear.domain.repository.JournalRepository
import com.psy.dear.domain.repository.UserRepository
import com.psy.dear.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import java.time.OffsetDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException
import kotlin.math.roundToInt

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val prefs: UserPreferencesRepository
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = prefs.authToken.map { it != null }
    override suspend fun login(email: String, password: String): Result<Unit> = try {
        val response = api.login(LoginRequest(email, password)); prefs.saveAuthToken(response.accessToken); Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e) }
    override suspend fun register(username: String, email: String, password: String): Result<Unit> = try {
        api.register(RegisterRequest(username, email, password)); login(email, password)
    } catch (e: Exception) { Result.Error(e) }
    override suspend fun logout() = prefs.clearAuthToken()
}

@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val api: JournalApiService,
    private val dao: JournalDao
) : JournalRepository {
    override fun getJournals(): Flow<List<Journal>> = dao.getAll().map { it.map { entity -> entity.toDomain() } }
    override fun getJournalById(id: String): Flow<Journal?> = dao.getById(id).map { it?.toDomain() }
    override suspend fun syncJournals(): Result<Unit> = try {
        api.getJournals().forEach { dao.insert(it.toEntity()) }; Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e) }
    override suspend fun createJournal(title: String, content: String, mood: String): Result<Unit> = try {
        val response = api.createJournal(CreateJournalRequest(title, content, mood)); dao.insert(response.toEntity()); Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e) }
    override suspend fun updateJournal(
        id: String,
        title: String,
        content: String,
        mood: String
    ): Result<Unit> {
        return try {
            val existing = dao.getById(id).first()
                ?: return Result.Error(IllegalArgumentException("Journal not found"))
            dao.insert(existing.copy(title = title, content = content, mood = mood))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    override suspend fun deleteJournal(id: String): Result<Unit> = try {
        dao.deleteById(id); Result.Success(Unit)
    } catch (e: Exception) { Result.Error(e) }
    override suspend fun getGrowthStatistics(): Result<GrowthStatistics> = try {
        val journals = dao.getAll().first()
        if (journals.isEmpty()) Result.Success(GrowthStatistics(0, "-", 0.0)) else {
            val mostFrequentMood = journals.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key ?: "-"
            val averageSentiment = journals.mapNotNull { it.sentimentScore }.average()
            Result.Success(GrowthStatistics(journals.size, mostFrequentMood, if (averageSentiment.isNaN()) 0.0 else (averageSentiment * 100).roundToInt() / 100.0))
        }
    } catch (e: Exception) { Result.Error(e) }
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApiService
) : UserRepository {
    override suspend fun getProfile(): Result<User> = try {
        val response = api.getProfile(); Result.Success(User(response.id, response.username, response.email))
    } catch (e: Exception) { Result.Error(e) }
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: ChatApiService,
    private val dao: ChatMessageDao,
    private val prefs: UserPreferencesRepository
) : ChatRepository {
    override fun getChatHistory(): Flow<List<ChatMessage>> = dao.getAll().map { it.map { entity -> entity.toDomain() } }
    override suspend fun sendMessage(message: String): Result<Unit> {
        // 1. Simpan pesan pengguna ke DB lokal
        val userMessage = ChatMessage(
            UUID.randomUUID().toString(),
            "user",
            message,
            null,
            OffsetDateTime.now()
        )
        dao.insert(userMessage.toEntity())

        // 2. Kirim ke API dan tunggu balasan
        return try {
            val response = api.postMessage(ChatRequest(message))
            // 3. Simpan balasan AI ke DB lokal
            val assistantMessage = ChatMessage(
                UUID.randomUUID().toString(),
                "assistant",
                response.reply,
                response.emotion,
                OffsetDateTime.now()
            )
            dao.insert(assistantMessage.toEntity())
            Result.Success(Unit)
        } catch (e: HttpException) {
            val code = e.code()
            if (code == 401 || code == 403 || code == 404) {
                prefs.clearAuthToken()
                Result.Error(UnauthorizedException())
            } else {
                Result.Error(e)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteMessage(id: String): Result<Unit> = try {
        dao.deleteById(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun flagMessage(id: String, flagged: Boolean): Result<Unit> = try {
        api.setFlag(id, FlagRequest(flagged))
        dao.updateFlagById(id, flagged)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: ContentApiService
) : ContentRepository {
    override fun getArticles(): Flow<List<Article>> = flow {
        emit(api.getArticles().map { it.toDomain() })
    }

    override fun getAudioTracks(): Flow<List<AudioTrack>> = flow {
        emit(api.getAudio().map { it.toDomain() })
    }

    override fun getMoodMusic(mood: String): Flow<List<AudioTrack>> = flow {
        try {
            emit(api.getMoodMusic(mood).map { it.toDomain() })
        } catch (e: HttpException) {
            if (e.code() == 404) {
                emit(emptyList())
            } else {
                throw e
            }
        }
    }

    override fun getRecommendedMusic(): Flow<List<AudioTrack>> = flow {
        try {
            emit(api.getRecommendedMusic().map { it.toDomain() })
        } catch (e: HttpException) {
            if (e.code() == 404) {
                emit(emptyList())
            } else {
                throw e
            }
        }
    }

    override fun getQuotes(): Flow<List<MotivationalQuote>> = flow {
        emit(api.getQuotes().map { it.toDomain() })
    }
}
