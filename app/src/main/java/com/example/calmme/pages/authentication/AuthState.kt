package com.example.calmme.pages.authentication

sealed class AuthState {
    object Initial : AuthState()
    object Authenticated : AuthState()
    object EmailNotVerified : AuthState()
    object EmailVerificationSent : AuthState()
    object EmailUpdateVerificationSent : AuthState() // Tambahkan state baru
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}