package com.example.calmme.pages.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmme.commons.Resource
import com.example.calmme.data.ChatRepository
import com.example.calmme.data.AppointmentData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HistoryViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _appointments = MutableStateFlow<List<AppointmentData>>(emptyList())
    val appointments: StateFlow<List<AppointmentData>> = _appointments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userRole = MutableStateFlow("")

    private val _participantNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val participantNames: StateFlow<Map<String, String>> = _participantNames

    private val _participantEmails = MutableStateFlow<Map<String, String>>(emptyMap())
    val participantEmails: StateFlow<Map<String, String>> = _participantEmails

    private val _participantProfilePictures = MutableStateFlow<Map<String, String?>>(emptyMap())
    val participantProfilePictures: StateFlow<Map<String, String?>> = _participantProfilePictures

    private val _currentUserProfilePicture = MutableStateFlow<String?>(null)
    val currentUserProfilePicture: StateFlow<String?> = _currentUserProfilePicture

    init {
        loadUserAppointments()
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val userDoc = firestore.collection("users").document(userId).get().await()
                val profilePicture = userDoc.getString("profilePicture")
                _currentUserProfilePicture.value = profilePicture
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun loadUserAppointments() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = auth.currentUser?.uid ?: return@launch
                val userDoc = firestore.collection("users").document(userId).get().await()
                val role = userDoc.getString("role") ?: "user"
                _userRole.value = role

                val appointmentsList = mutableListOf<AppointmentData>()
                val namesMap = mutableMapOf<String, String>()
                val emailsMap = mutableMapOf<String, String>()
                val profilePicturesMap = mutableMapOf<String, String?>()

                if (role == "psychologist") {
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
                            val appointmentData = appointmentDoc.toObject(AppointmentData::class.java)
                            if (appointmentData != null) {
                                val appointment = appointmentData.copy(appointmentId = appointmentDoc.id)
                                appointmentsList.add(appointment)
                                val patientId = appointment.userId
                                val patientDoc = firestore.collection("users").document(patientId).get().await()
                                val patientEmail = patientDoc.getString("email") ?: ""
                                val patientUsername = patientDoc.getString("username") ?: "Patient"
                                val patientProfilePicture = patientDoc.getString("profilePicture")

                                namesMap[appointment.appointmentId] = patientUsername
                                emailsMap[appointment.appointmentId] = patientEmail
                                profilePicturesMap[appointment.appointmentId] = patientProfilePicture
                            }
                        }
                    }
                } else {
                    val appointmentsQuery = firestore.collection("appointments")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()

                    for (appointmentDoc in appointmentsQuery.documents) {
                        val appointmentData = appointmentDoc.toObject(AppointmentData::class.java)
                        if (appointmentData != null) {
                            val appointment = appointmentData.copy(appointmentId = appointmentDoc.id)
                            appointmentsList.add(appointment)
                            val psychologistDoc = firestore.collection("psychologists")
                                .document(appointment.psychologistId).get().await()
                            val psychologistName = psychologistDoc.getString("name") ?: "Psychologist"
                            val psychologistEmail = psychologistDoc.getString("email") ?: ""

                            val psychologistUserId = psychologistDoc.getString("userId")
                            var psychologistProfilePicture: String? = null
                            if (!psychologistUserId.isNullOrEmpty()) {
                                val psychologistUserDoc = firestore.collection("users")
                                    .document(psychologistUserId).get().await()
                                psychologistProfilePicture = psychologistUserDoc.getString("profilePicture")
                            }

                            namesMap[appointment.appointmentId] = psychologistName
                            emailsMap[appointment.appointmentId] = psychologistEmail
                            profilePicturesMap[appointment.appointmentId] = psychologistProfilePicture
                        }
                    }
                }

                val sortedAppointments = appointmentsList.sortedByDescending {
                    it.appointmentDate
                }

                _appointments.value = sortedAppointments
                _appointments.value = appointmentsList
                _participantNames.value = namesMap
                _participantEmails.value = emailsMap
                _participantProfilePictures.value = profilePicturesMap
                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun createChatRoom(
        appointmentId: String,
        psychologistId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: ""

                when (val result = chatRepository.createOrGetChatRoom(appointmentId, userId, psychologistId)) {
                    is Resource.Success -> onSuccess(result.data)
                    is Resource.Error -> onError(result.message)
                    else -> onError("Unknown error occurred")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create chat room")
            }
        }
    }
}
