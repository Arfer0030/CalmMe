package com.example.calmme.commons

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes(val route: String) {
    object Authentication : Routes("authentication")
    object Home : Routes("home")
    object Consultation : Routes("consultation")
    object Appointment : Routes("appointment")
    object History : Routes("history")
    object Meditate : Routes("meditate")
    object DailyMood : Routes("dailymood")
    object Profile : Routes("profile")
    object Assesment : Routes("assesment")
    object InitAssesment : Routes("initassesment")
    object Subscribe : Routes("subscribe")
    object EditProfile : Routes("edit_profile")
    object EmailVerification : Routes("email_verification/{email}") {
        fun createRoute(email: String) = "email_verification/$email"
    }
    object EditSecurity : Routes("edit_security")
    object Payment : Routes("payment")
    object Confirmation : Routes("confirmation")
    object PsiEditProfile : Routes("psi_edit_profile")
    object Chat : Routes("chat/{chatRoomId}") {
        fun createRoute(chatRoomId: String) = "chat/$chatRoomId"
    }
    object Help : Routes("help")
    object About : Routes("about")
    object PaymentHistory : Routes("payment_history")
}

