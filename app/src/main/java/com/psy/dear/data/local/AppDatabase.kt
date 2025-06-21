package com.psy.dear.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psy.dear.data.local.dao.ChatMessageDao
import com.psy.dear.data.local.dao.JournalDao
import com.psy.dear.data.local.entity.ChatMessageEntity
import com.psy.dear.data.local.entity.JournalEntity

@Database(
    entities = [JournalEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun chatMessageDao(): ChatMessageDao
}
