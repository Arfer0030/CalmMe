package com.example.calmme.pages.dailymood

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            preferences[TODAY_MOOD_KEY] = moodEntry.mood
            preferences[TODAY_DATE_KEY] = moodEntry.date

            val currentHistoryString = preferences[MOOD_HISTORY_KEY] ?: "[]"
            val currentHistory = try {
                json.decodeFromString<List<MoodEntry>>(currentHistoryString).toMutableList()
            } catch (e: Exception) {
                mutableListOf<MoodEntry>()
            }

            currentHistory.removeAll { it.date == moodEntry.date }
            currentHistory.add(moodEntry)

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