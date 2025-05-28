package com.example.calmme.pages.dailymood

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

val Context.moodDataStore: DataStore<Preferences> by preferencesDataStore(name = "mood_preferences")

class MoodDataStore(val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private val MOOD_HISTORY_KEY = stringPreferencesKey("mood_history")
        private val TODAY_MOOD_KEY = stringPreferencesKey("today_mood")
        private val TODAY_DATE_KEY = stringPreferencesKey("today_date")
    }

    suspend fun saveMoodEntry(moodEntry: MoodEntry) {
        context.moodDataStore.edit { preferences ->
            // Simpan mood hari ini
            preferences[TODAY_MOOD_KEY] = moodEntry.mood
            preferences[TODAY_DATE_KEY] = moodEntry.date

            // Update history menggunakan Kotlin Serialization
            val currentHistoryString = preferences[MOOD_HISTORY_KEY] ?: "[]"
            val currentHistory = try {
                json.decodeFromString<List<MoodEntry>>(currentHistoryString).toMutableList()
            } catch (e: Exception) {
                mutableListOf<MoodEntry>()
            }

            // Hapus entry hari ini jika ada, lalu tambah yang baru
            currentHistory.removeAll { it.date == moodEntry.date }
            currentHistory.add(moodEntry)

            // Encode menggunakan Kotlin Serialization
            preferences[MOOD_HISTORY_KEY] = json.encodeToString(currentHistory)
        }
    }

    fun getTodayMood(): Flow<String?> {
        return context.moodDataStore.data.map { preferences ->
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val savedDate = preferences[TODAY_DATE_KEY]

            if (savedDate == today) {
                preferences[TODAY_MOOD_KEY]
            } else {
                null
            }
        }
    }

    fun getMoodHistory(): Flow<List<MoodEntry>> {
        return context.moodDataStore.data.map { preferences ->
            val historyString = preferences[MOOD_HISTORY_KEY] ?: "[]"
            try {
                json.decodeFromString<List<MoodEntry>>(historyString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}