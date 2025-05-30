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
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.example.calmme.BuildConfig
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

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    init {
        loadTodaysMood()
        loadMoodHistory()
        if (BuildConfig.DEBUG) {
            debugDataStore()
        }
    }

    private fun debugDataStore() {
        viewModelScope.launch {
            moodDataStore.context.moodDataStore.data.collect { preferences ->
                Log.d("MoodDataStore", "DataStore size: ${preferences.asMap().size}")
                preferences.asMap().forEach { (key, value) ->
                    Log.d("MoodDataStore", "${key.name}: $value")
                }
            }
        }
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

                // Refresh data setelah menyimpan
                loadMoodHistory()

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
                    _streak.value = calculateStreakFromHistory(history)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun calculateStreakFromHistory(history: List<MoodEntry>): Int {
        if (history.isEmpty()) return 0

        val sortedHistory = history.sortedByDescending { it.date }
        var streak = 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        // Mulai dari hari ini
        var currentDate = dateFormat.format(Date())

        for (i in 0 until 365) { // Maksimal 365 hari
            val hasEntry = sortedHistory.any { it.date == currentDate }

            if (hasEntry) {
                streak++
                // Pindah ke hari sebelumnya
                cal.time = dateFormat.parse(currentDate)!!
                cal.add(Calendar.DAY_OF_MONTH, -1)
                currentDate = dateFormat.format(cal.time)
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
            val mood = moodEntry?.mood ?: "empty" // Ubah dari "neutral" ke "empty"

            recentMoods.add(dayString to mood)
        }

        return recentMoods
    }

    fun calculateMoodPercentages(period: String): List<Pair<String, Float>> {
        val moodHistory = _moodHistory.value
        val currentDate = Date()

        // Tentukan rentang tanggal berdasarkan periode
        val (startDate, endDate, totalDays) = when (period) {
            "Week" -> {
                val weekRange = getWeekRange(currentDate)
                Triple(weekRange.first, weekRange.second, 7)
            }
            "Month" -> {
                val monthRange = getMonthRange(currentDate)
                Triple(monthRange.first, monthRange.second, 30)
            }
            else -> {
                val weekRange = getWeekRange(currentDate)
                Triple(weekRange.first, weekRange.second, 7)
            }
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateStr = dateFormat.format(startDate)
        val endDateStr = dateFormat.format(endDate)

        // Filter mood dalam rentang tanggal yang fixed
        val filteredHistory = moodHistory.filter { entry ->
            entry.date >= startDateStr && entry.date <= endDateStr
        }

        // Hitung persentase berdasarkan total hari dalam periode
        val moodCounts = filteredHistory.groupingBy { it.mood }.eachCount()

        return moods.map { (mood, _) ->
            val count = moodCounts[mood] ?: 0
            val percentage = (count.toFloat() / totalDays.toFloat()) * 100
            mood to percentage
        }
    }

    private fun getWeekRange(date: Date): Pair<Date, Date> {
        val cal = Calendar.getInstance()
        cal.time = date

        // Set ke hari Minggu (SUNDAY = 1)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = if (dayOfWeek == Calendar.SUNDAY) 0 else dayOfWeek - Calendar.SUNDAY

        // Mundur ke hari Minggu
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract)
        val startDate = cal.time

        // Maju ke hari Sabtu (6 hari setelah Minggu)
        cal.add(Calendar.DAY_OF_MONTH, 6)
        val endDate = cal.time

        return Pair(startDate, endDate)
    }

    private fun getMonthRange(date: Date): Pair<Date, Date> {
        val cal = Calendar.getInstance()
        cal.time = date

        // Hitung minggu ke berapa dalam bulan ini
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val weekOfMonth = ((dayOfMonth - 1) / 7) + 1

        // Set ke hari pertama minggu ini dalam bulan
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = if (dayOfWeek == Calendar.SUNDAY) 0 else dayOfWeek - Calendar.SUNDAY
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract)

        // Mundur ke awal periode 30 hari
        val weeksToSubtract = (weekOfMonth - 1) * 7
        cal.add(Calendar.DAY_OF_MONTH, -weeksToSubtract)
        val startDate = cal.time

        // Maju 29 hari untuk total 30 hari
        cal.add(Calendar.DAY_OF_MONTH, 29)
        val endDate = cal.time

        return Pair(startDate, endDate)
    }


    fun getMoodIcon(mood: String): Int {
        return when (mood) {
            "empty" -> R.drawable.ic_empty_mood // Tambahkan icon untuk mood kosong
            else -> moods.find { it.first == mood }?.second ?: R.drawable.md_calm
        }
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
            "empty" -> Color(0xFFE0E0E0) // Warna abu-abu untuk mood kosong
            else -> Color(0xFFB9A6FF)
        }
    }

    fun calculateMoodFillCounts(period: String): Pair<Int, Int> {
        val moodHistory = _moodHistory.value
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val (startDate, endDate, totalDays) = when (period) {
            "Week" -> {
                val weekRange = getWeekRange(currentDate)
                Triple(weekRange.first, weekRange.second, 7)
            }
            "Month" -> {
                val monthRange = getMonthRange(currentDate)
                Triple(monthRange.first, monthRange.second, 30)
            }
            else -> {
                val weekRange = getWeekRange(currentDate)
                Triple(weekRange.first, weekRange.second, 7)
            }
        }

        val startDateStr = dateFormat.format(startDate)
        val endDateStr = dateFormat.format(endDate)

        // Filter mood dalam rentang tanggal yang fixed
        val filteredHistory = moodHistory.filter { entry ->
            entry.date >= startDateStr && entry.date <= endDateStr && entry.mood != "empty"
        }

        val fillCount = filteredHistory.size
        return Pair(fillCount, totalDays)
    }


    fun getDateRange(period: String): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("MMM dd, yy", Locale.getDefault())

        return when (period) {
            "Week" -> {
                val (startDate, endDate) = getWeekRange(currentDate)
                val startStr = dateFormat.format(startDate)
                val endStr = dateFormat.format(endDate)
                "$startStr - $endStr"
            }
            "Month" -> {
                val (startDate, endDate) = getMonthRange(currentDate)
                val startStr = dateFormat.format(startDate)
                val endStr = dateFormat.format(endDate)
                "$startStr - $endStr"
            }
            else -> ""
        }
    }
}

