package com.example.calmme.commons

import kotlinx.serialization.Serializable

sealed class Routes(val route: String) {
    object Authentication : Routes("authentication")
    object Home : Routes("home")
    object Consultation : Routes("consultation")
    object Community : Routes("community")
    object History : Routes("history")
    object Meditate : Routes("meditate")
    object DailyMood : Routes("dailymood")
    object Profile : Routes("profile")
}
