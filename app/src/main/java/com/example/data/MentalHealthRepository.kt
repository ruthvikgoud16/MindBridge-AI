package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class JournalSentimentResult(
    val sentiment: String = "Neutral",
    val score: Float = 0.5f,
    val tip: String = "Take deep breaths and hold space for your feelings."
)

class MentalHealthRepository(
    private val userDao: UserDao,
    private val journalDao: JournalDao,
    private val moodDao: MoodDao,
    private val chatDao: ChatDao
) {
    private val apiService = RetrofitClient.service
    private val moshi = Moshi.Builder().build()
    private val sentimentAdapter = moshi.adapter(JournalSentimentResult::class.java)

    // --- User Profile ---
    val primaryUser: Flow<User?> = userDao.getPrimaryUser()

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun registerUser(user: User): Long = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    // --- Journal ---
    val allJournals: Flow<List<JournalEntry>> = journalDao.getAllJournals()

    suspend fun insertJournal(entry: JournalEntry): Long = withContext(Dispatchers.IO) {
        journalDao.insertJournal(entry)
    }

    suspend fun deleteJournal(entry: JournalEntry) = withContext(Dispatchers.IO) {
        journalDao.deleteJournal(entry)
    }

    suspend fun analyzeJournalSentiment(content: String): JournalSentimentResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext JournalSentimentResult(
                sentiment = "Reflective",
                score = 0.6f,
                tip = "Write a bit more to unlock deeper AI insights. Connect to the Gemini API to get real sentiment analysis."
            )
        }

        val systemPrompt = "You are a compassionate, professional mental wellness chatbot. Analyze the following user journal entry and determine: 1. Sentiment label (e.g. Peaceful, Joyful, Melancholy, Anxious, Stressed, Frustrated). 2. Emotional intensity score (0.0 to 1.0). 3. A 1-sentence supportive coping advice/tip. Output in clean JSON only."
        val userPrompt = """
            Analyze the journal entry:
            "$content"
            
            Produce valid JSON content only matching this schema:
            {
              "sentiment": "Peaceful or Joyful or Melancholy or Anxious or Stressed or Frustrated",
              "score": 0.5,
              "tip": "Write a gentle supportive tip"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(
                temperature = 0.2f,
                responseMimeType = "application/json"
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (fullText != null) {
                val cleaned = fullText.trim()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                sentimentAdapter.fromJson(cleaned) ?: JournalSentimentResult()
            } else {
                JournalSentimentResult()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            JournalSentimentResult(
                sentiment = "Neutral",
                score = 0.5f,
                tip = "We recorded your thoughts. Keep reflecting on your daily journey!"
            )
        }
    }

    // --- Mood Logs ---
    val allMoodLogs: Flow<List<MoodLog>> = moodDao.getAllMoodLogs()
    val allMoodLogsAsc: Flow<List<MoodLog>> = moodDao.getAllMoodLogsAsc()

    suspend fun insertMoodLog(log: MoodLog): Long = withContext(Dispatchers.IO) {
        moodDao.insertMoodLog(log)
    }

    suspend fun deleteMoodLog(log: MoodLog) = withContext(Dispatchers.IO) {
        moodDao.deleteMoodLog(log)
    }

    // --- Chat Companion ---
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun insertMessage(message: ChatMessage): Long = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
    }

    suspend fun clearSessionChat(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.clearSessionChat(sessionId)
    }

    suspend fun fetchChatCompanionResponse(
        history: List<ChatMessage>,
        newMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Hi there! I am your AI mental-wellness companion. I am currently in offline/prototype mode. To enable my fully empathetic, intelligent conversational capabilities, please configure a GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        // Map recent history limits to prevent huge loads
        val contextParts = mutableListOf<Content>()
        
        // Add historic messages
        history.takeLast(12).forEach { msg ->
            val role = if (msg.sender == "USER") "user" else "model"
            // Simple content mapping
            contextParts.add(
                Content(parts = listOf(Part(text = "${msg.sender}: ${msg.message}")))
            )
        }
        
        // Add latest user input
        contextParts.add(Content(parts = listOf(Part(text = "USER: $newMessage"))))

        val systemPrompt = "You are a warm, empathetic, and professional mental health AI companion named Serene. Your role is to listen actively, offer safe validation, provide evidence-based cognitive behavioral coping ideas (e.g. deep breathing, box breathing, grounding exercises), and help users process stress or feelings. Do NOT diagnose clinical conditions, prescribe drugs, or declare yourself a doctor. If the user presents crisis-level or self-harm keywords, ALWAYS respond immediately with warm redirection, and suggest contacting professional helplines (like 988 or text HOME to 741741). Be concise, caring, and beautifully supportive."

        val request = GenerateContentRequest(
            contents = contextParts,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 600
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I am listening. Could you tell me more about what is on your mind?"
        } catch (e: Exception) {
            e.printStackTrace()
            "I'm having a little trouble connecting with the network right now. I am still here for you. Please try to take a deep breath, and let's try chatting again in a moment."
        }
    }
}
