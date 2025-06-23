package com.psy.dear.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.psy.dear.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAll(): Flow<List<ChatMessageEntity>>

    @Query("UPDATE chat_messages SET isFlagged = :flag WHERE id = :id")
    suspend fun updateFlagById(id: String, flag: Boolean)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteById(id: String)
}
