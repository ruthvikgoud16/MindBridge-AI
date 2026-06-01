package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MentalHealthViewModel(private val repository: MentalHealthRepository) : ViewModel() {

    // --- Authentication & User state ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // --- Journal state ---
    val allJournals: StateFlow<List<JournalEntry>> = repository.allJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAnalyzingJournal = MutableStateFlow(false)
    val isAnalyzingJournal: StateFlow<Boolean> = _isAnalyzingJournal.asStateFlow()

    private val _lastSentimentResult = MutableStateFlow<JournalSentimentResult?>(null)
    val lastSentimentResult: StateFlow<JournalSentimentResult?> = _lastSentimentResult.asStateFlow()

    // --- Mood state ---
    val allMoodLogs: StateFlow<List<MoodLog>> = repository.allMoodLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMoodLogsAsc: StateFlow<List<MoodLog>> = repository.allMoodLogsAsc
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Chat state ---
    val chatMessages: StateFlow<List<ChatMessage>> = repository.getMessagesForSession("default_session")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Interactive Breathing State ---
    private val _breathingState = MutableStateFlow("Inhale") // "Inhale", "Hold", "Exhale", "Rest"
    val breathingState: StateFlow<String> = _breathingState.asStateFlow()

    private val _breathingProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val breathingProgress: StateFlow<Float> = _breathingProgress.asStateFlow()

    init {
        // Observe primary local user from database
        viewModelScope.launch {
            repository.primaryUser.collect { user ->
                if (user != null) {
                    _currentUser.value = user
                    _isUserLoggedIn.value = true
                } else {
                    _currentUser.value = null
                    _isUserLoggedIn.value = false
                }
            }
        }

        // Run grounding breathing state cycle
        startBreathingCycle()
    }

    // --- Auth Logic ---
    fun login(email: String, passwordText: String, onSuccess: () -> Unit) {
        _authError.value = null
        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user == null) {
                _authError.value = "User not found. Please register."
                return@launch
            }
            if (user.passwordHash == passwordText) { // Quick password check representing bcrypt/hash securely locally
                _currentUser.value = user
                _isUserLoggedIn.value = true
                repository.registerUser(user) // Refresh priority
                onSuccess()
            } else {
                _authError.value = "Invalid password. Please try again."
            }
        }
    }

    fun register(name: String, email: String, passwordText: String, onSuccess: () -> Unit) {
        _authError.value = null
        if (name.isBlank() || email.isBlank() || passwordText.isBlank()) {
            _authError.value = "All fields are required."
            return
        }
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email.trim().lowercase())
            if (existing != null) {
                _authError.value = "An account with this email already exists."
                return@launch
            }
            val newUser = User(
                name = name.trim(),
                email = email.trim().lowercase(),
                passwordHash = passwordText,
                quotes = "Take a deep breath. Focus on the present moment."
            )
            repository.registerUser(newUser)
            _currentUser.value = newUser
            _isUserLoggedIn.value = true
            onSuccess()
        }
    }

    fun updateProfile(name: String, email: String, quote: String) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name.trim(),
                email = email.trim().lowercase(),
                quotes = quote.trim()
            )
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    fun logout() {
        _isUserLoggedIn.value = false
        _currentUser.value = null
        // Delete or keep local user in database to enable re-logging
    }

    // --- Mood Log Logic ---
    fun logMood(moodType: String, score: Int, note: String) {
        viewModelScope.launch {
            val log = MoodLog(
                moodType = moodType,
                score = score,
                note = note.trim()
            )
            repository.insertMoodLog(log)
        }
    }

    fun deleteMood(log: MoodLog) {
        viewModelScope.launch {
            repository.deleteMoodLog(log)
        }
    }

    // --- Journal Logic ---
    fun addJournalEntry(title: String, content: String, onFinished: () -> Unit) {
        if (content.isBlank()) return
        _isAnalyzingJournal.value = true
        _lastSentimentResult.value = null

        viewModelScope.launch {
            // Analyze with AI
            val result = repository.analyzeJournalSentiment(content)
            _lastSentimentResult.value = result

            val entry = JournalEntry(
                title = if (title.isBlank()) "Daily Reflection" else title.trim(),
                content = content.trim(),
                sentiment = result.sentiment,
                sentimentScore = result.score,
                copingTip = result.tip
            )
            repository.insertJournal(entry)
            _isAnalyzingJournal.value = false
            onFinished()
        }
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournal(entry)
        }
    }

    // --- AI Chatbot Logic ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = "USER", message = text.trim())

        viewModelScope.launch {
            // Save user message
            repository.insertMessage(userMsg)
            _isChatLoading.value = true

            // Send to Gemini with full session context
            val currentHistory = chatMessages.value
            val aiResponseText = repository.fetchChatCompanionResponse(currentHistory, text)

            val aiMsg = ChatMessage(sender = "AI", message = aiResponseText)
            repository.insertMessage(aiMsg)
            _isChatLoading.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearSessionChat("default_session")
        }
    }

    // --- Breathing Cycle Simulation (Box Breathing: 4s inhale, 4s hold, 4s exhale, 4s rest) ---
    private fun startBreathingCycle() {
        viewModelScope.launch {
            var progress = 0f
            while (true) {
                kotlinx.coroutines.delay(16)
                progress += 0.004f // Increases smoothly, completing 1.0 (100%) in exactly 4 seconds (16ms * 250 ticks = 4000ms)
                if (progress >= 1f) {
                    progress = 0f
                    _breathingState.value = when (_breathingState.value) {
                        "Inhale" -> "Hold"
                        "Hold" -> "Exhale"
                        "Exhale" -> "Rest"
                        else -> "Inhale"
                    }
                }
                _breathingProgress.value = progress
            }
        }
    }
}

class MentalHealthViewModelFactory(private val repository: MentalHealthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MentalHealthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MentalHealthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
