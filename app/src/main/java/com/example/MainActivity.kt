package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.MentalHealthRepository
import com.example.ui.MentalHealthAppContent
import com.example.ui.MentalHealthViewModel
import com.example.ui.MentalHealthViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room SQLite Database and State Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = MentalHealthRepository(
      userDao = database.userDao(),
      journalDao = database.journalDao(),
      moodDao = database.moodDao(),
      chatDao = database.chatDao()
    )

    // Construct ViewModel via abstract factory injection
    val factory = MentalHealthViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[MentalHealthViewModel::class.java]

    setContent {
      MyApplicationTheme {
        MentalHealthAppContent(viewModel = viewModel)
      }
    }
  }
}
