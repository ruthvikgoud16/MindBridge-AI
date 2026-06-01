package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val avatarUrl: String = "",
    val quotes: String = "One breath at a time."
)

@Entity(tableName = "journals")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sentiment: String = "Neutral", // "Peaceful", "Joyful", "Melancholy", "Anxious", "Stressed", "Frustrated"
    val sentimentScore: Float = 0.5f,
    val copingTip: String = ""
)

@Entity(tableName = "moods")
data class MoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moodType: String, // "HAPPY", "CALM", "ANXIOUS", "SAD", "STRESSED", "TIRED"
    val score: Int = 3, // 1 to 5 index for statistics
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String = "default_session",
    val sender: String, // "USER", "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
