package com.example.calmme.pages.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class AppointmentHistory(
    val appointmentId: String,
    val psychologistName: String,
    val psychologistEmail: String,
    val appointmentDate: String,
    val appointmentTime: String,
    val paymentStatus: String,
    val status: String,
    val consultationMethod: String,
    val chatRoomId: String? = null
)

class HistoryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _appointments = MutableStateFlow<List<AppointmentHistory>>(emptyList())
    val appointments: StateFlow<List<AppointmentHistory>> = _appointments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userRole = MutableStateFlow("")
    val userRole: StateFlow<String> = _userRole

    init {
        loadUserAppointments()
    }

    private fun loadUserAppointments() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = auth.currentUser?.uid ?: return@launch

                // Get user role
                val userDoc = firestore.collection("users").document(userId).get().await()
                val role = userDoc.getString("role") ?: "user"
                _userRole.value = role

                val appointmentsList = mutableListOf<AppointmentHistory>()

                if (role == "psychologist") {
                    // Load appointments untuk psychologist
                    val psychologistQuery = firestore.collection("psychologists")
                        .whereEqualTo("userId", userId)
                        .limit(1)
                        .get()
                        .await()

                    if (!psychologistQuery.isEmpty) {
                        val psychologistId = psychologistQuery.documents.first().id

                        val appointmentsQuery = firestore.collection("appointments")
                            .whereEqualTo("psychologistId", psychologistId)
                            .get()
                            .await()

                        for (appointmentDoc in appointmentsQuery.documents) {
                            val appointment = appointmentDoc.data
                            if (appointment != null) {
                                val patientId = appointment["userId"] as? String ?: ""
                                val patientDoc = firestore.collection("users").document(patientId).get().await()
                                val patientEmail = patientDoc.getString("email") ?: ""

                                appointmentsList.add(
                                    AppointmentHistory(
                                        appointmentId = appointmentDoc.id,
                                        psychologistName = "Patient",
                                        psychologistEmail = patientEmail,
                                        appointmentDate = appointment["appointmentDate"] as? String ?: "",
                                        appointmentTime = appointment["appointmentTime"] as? String ?: "",
                                        paymentStatus = appointment["paymentStatus"] as? String ?: "",
                                        status = appointment["status"] as? String ?: "",
                                        consultationMethod = appointment["consultationMethod"] as? String ?: "",
                                        chatRoomId = appointment["chatRoomId"] as? String
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // Load appointments untuk user biasa
                    val appointmentsQuery = firestore.collection("appointments")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()

                    for (appointmentDoc in appointmentsQuery.documents) {
                        val appointment = appointmentDoc.data
                        if (appointment != null) {
                            val psychologistId = appointment["psychologistId"] as? String ?: ""
                            val psychologistDoc = firestore.collection("psychologists").document(psychologistId).get().await()
                            val psychologistName = psychologistDoc.getString("name") ?: ""
                            val psychologistEmail = psychologistDoc.getString("email") ?: ""

                            appointmentsList.add(
                                AppointmentHistory(
                                    appointmentId = appointmentDoc.id,
                                    psychologistName = psychologistName,
                                    psychologistEmail = psychologistEmail,
                                    appointmentDate = appointment["appointmentDate"] as? String ?: "",
                                    appointmentTime = appointment["appointmentTime"] as? String ?: "",
                                    paymentStatus = appointment["paymentStatus"] as? String ?: "",
                                    status = appointment["status"] as? String ?: "",
                                    consultationMethod = appointment["consultationMethod"] as? String ?: "",
                                    chatRoomId = appointment["chatRoomId"] as? String
                                )
                            )
                        }
                    }
                }

                _appointments.value = appointmentsList.sortedByDescending { it.appointmentDate }
                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun createChatRoom(appointmentId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    onError("User not authenticated")
                    return@launch
                }

                // Get appointment data
                val appointmentDoc = firestore.collection("appointments").document(appointmentId).get().await()
                val appointmentData = appointmentDoc.data
                if (appointmentData == null) {
                    onError("Appointment not found")
                    return@launch
                }

                val psychologistId = appointmentData["psychologistId"] as? String ?: ""
                val patientId = appointmentData["userId"] as? String ?: ""
                val appointmentDate = appointmentData["appointmentDate"] as? String ?: ""
                val appointmentTime = appointmentData["appointmentTime"] as? String ?: ""

                if (psychologistId.isEmpty() || patientId.isEmpty()) {
                    onError("Invalid appointment data")
                    return@launch
                }

                // ✅ Get psychologist data untuk mendapatkan userId dan name
                val psychologistDoc = firestore.collection("psychologists").document(psychologistId).get().await()
                val psychologistData = psychologistDoc.data
                if (psychologistData == null) {
                    onError("Psychologist not found")
                    return@launch
                }

                val psychologistUserId = psychologistData["userId"] as? String ?: ""
                val psychologistName = psychologistData["name"] as? String ?: "Psychologist"

                if (psychologistUserId.isEmpty()) {
                    onError("Psychologist user ID not found")
                    return@launch
                }

                // ✅ Get user data untuk mendapatkan username
                val userDoc = firestore.collection("users").document(patientId).get().await()
                val userData = userDoc.data
                val userName = userData?.get("username") as? String ?: "User"

                // Parse time untuk startTime dan endTime
                val timeRange = appointmentTime.split("-")
                val startTime = if (timeRange.size >= 1) "${appointmentDate}T${timeRange[0]}:00Z" else ""
                val endTime = if (timeRange.size >= 2) "${appointmentDate}T${timeRange[1]}:00Z" else ""

                val chatRoomId = UUID.randomUUID().toString()

                val chatRoomData = hashMapOf(
                    "chatRoomId" to chatRoomId,
                    "appointmentId" to appointmentId,
                    "userId" to listOf(patientId, psychologistUserId),
                    "psychologistId" to psychologistId,
                    "isActive" to false,
                    "startTime" to startTime,
                    "endTime" to endTime,
                    "createdAt" to Timestamp.now(),
                    "userName" to userName, // ✅ Username dari collection users
                    "psychologistName" to psychologistName // ✅ Name dari collection psychologists
                )

                // Create chat room
                firestore.collection("chat_rooms").document(chatRoomId).set(chatRoomData).await()

                // Update appointment dengan chatRoomId
                firestore.collection("appointments").document(appointmentId)
                    .update("chatRoomId", chatRoomId)
                    .await()

                onSuccess(chatRoomId)

            } catch (e: Exception) {
                onError(e.message ?: "Failed to create chat room")
            }
        }
    }
}
