package com.example.calmme.pages.profile

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.net.Uri
import android.widget.Toast

class HelpViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val userDoc = firestore.collection("users").document(userId).get().await()
            _userName.value = userDoc.getString("username")
            _userEmail.value = userDoc.getString("email")
        }
    }

    fun openHelpEmail(
        context: Context,
        subject: String,
        userName: String?,
        userEmail: String?
    ) {
        val adminEmail = "help.calmme787@gmail.com"
        val body = """
        Hi CalmMe Support Team,

        I am contacting you regarding: $subject

        User: ${userName ?: "Unknown User"}
        Email: ${userEmail ?: "Not provided"}

        Please describe your issue or feedback below:
        [Write your message here]

        Thank you for your assistance.

        Best regards,
        ${userName ?: "CalmMe User"}
    """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(adminEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Send email using"))
        } catch (e: Exception) {
            Toast.makeText(context, "No email app installed.", Toast.LENGTH_SHORT).show()
        }
    }
}
