package com.example.calmme.pages.dailymood

// DailyMoodViewModel.kt
import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.example.calmme.R
import com.example.calmme.data.moods
import java.text.SimpleDateFormat
import java.util.*

class DailyMoodViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val moodDataStore = MoodDataStore(context)

    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood: StateFlow<String?> = _selectedMood

    private val _moodHistory = MutableStateFlow<List<MoodEntry>>(emptyList())
    val moodHistory: StateFlow<List<MoodEntry>> = _moodHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadTodaysMood()
        loadMoodHistory()
    }

    fun selectMood(mood: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val moodEntry = MoodEntry(
                    mood = mood,
                    date = currentDate,
                    timestamp = System.currentTimeMillis()
                )

                moodDataStore.saveMoodEntry(moodEntry)
                _selectedMood.value = mood

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTodaysMood() {
        viewModelScope.launch {
            try {
                moodDataStore.getTodayMood().collect { mood ->
                    _selectedMood.value = mood
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadMoodHistory() {
        viewModelScope.launch {
            try {
                moodDataStore.getMoodHistory().collect { history ->
                    _moodHistory.value = history.sortedByDescending { it.timestamp }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTodaysMoodEntry(): MoodEntry? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return _moodHistory.value.find { it.date == today }
    }

    fun getMoodsByDateRange(startDate: String, endDate: String): List<MoodEntry> {
        return _moodHistory.value.filter { entry ->
            entry.date in startDate..endDate
        }
    }

    // Pindahkan helper functions ke dalam ViewModel
    fun calculateStreak(): Int {
        val moodHistory = _moodHistory.value
        if (moodHistory.isEmpty()) return 0

        val sortedHistory = moodHistory.sortedByDescending { it.date }
        var streak = 0
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var currentDate = today

        for (entry in sortedHistory) {
            if (entry.date == currentDate) {
                streak++
                // Move to previous day
                val cal = Calendar.getInstance()
                cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentDate)!!
                cal.add(Calendar.DAY_OF_MONTH, -1)
                currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            } else {
                break
            }
        }

        return streak
    }

    fun getRecentMoods(days: Int): List<Pair<String, String>> {
        val moodHistory = _moodHistory.value
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE dd", Locale.getDefault())

        val recentMoods = mutableListOf<Pair<String, String>>()

        for (i in days - 1 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_MONTH, -i)
            val date = dateFormat.format(cal.time)
            val dayString = dayFormat.format(cal.time)

            val moodEntry = moodHistory.find { it.date == date }
            val mood = moodEntry?.mood ?: "neutral"

            recentMoods.add(dayString to mood)
        }

        return recentMoods
    }

    fun calculateMoodPercentages(period: String): List<Pair<String, Float>> {
        val moodHistory = _moodHistory.value
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Filter data based on period
        val filteredHistory = when (period) {
            "Week" -> {
                cal.add(Calendar.DAY_OF_MONTH, -7)
                val weekAgo = dateFormat.format(cal.time)
                moodHistory.filter { it.date >= weekAgo }
            }
            "Month" -> {
                cal.add(Calendar.MONTH, -1)
                val monthAgo = dateFormat.format(cal.time)
                moodHistory.filter { it.date >= monthAgo }
            }
            else -> moodHistory
        }

        if (filteredHistory.isEmpty()) {
            return moods.map { it.first to 0f }
        }

        val moodCounts = filteredHistory.groupingBy { it.mood }.eachCount()
        val total = filteredHistory.size.toFloat()

        return moods.map { (mood, _) ->
            val count = moodCounts[mood] ?: 0
            val percentage = (count / total) * 100
            mood to percentage
        }
    }

    fun getMoodIcon(mood: String): Int {
        return moods.find { it.first == mood }?.second ?: R.drawable.md_calm
    }

    fun getMoodColor(mood: String): Color {
        return when (mood) {
            "happy" -> Color(0xFFFFE066)
            "sad" -> Color(0xFF66B2FF)
            "angry" -> Color(0xFFFF6B6B)
            "calm" -> Color(0xFF66FFB2)
            "worried" -> Color(0xFFB266FF)
            "frustrated" -> Color(0xFFFF9966)
            "surprised" -> Color(0xFFFFB366)
            "bored" -> Color(0xFFB3B3B3)
            "disappoint" -> Color(0xFF9999FF)
            else -> Color(0xFFB9A6FF)
        }
    }

    fun getDateRange(period: String): String {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, yy", Locale.getDefault())

        return when (period) {
            "Week" -> {
                val endDate = dateFormat.format(cal.time)
                cal.add(Calendar.DAY_OF_MONTH, -6)
                val startDate = dateFormat.format(cal.time)
                "$startDate - $endDate"
            }
            "Month" -> {
                val endDate = dateFormat.format(cal.time)
                cal.add(Calendar.MONTH, -1)
                val startDate = dateFormat.format(cal.time)
                "$startDate - $endDate"
            }
            else -> ""
        }
    }
}

