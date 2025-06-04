package com.example.calmme.pages.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmme.data.PsikologRepository
import com.example.calmme.commons.Resource
import com.example.calmme.data.AppointmentData
import com.example.calmme.data.PsychologistData
import com.example.calmme.data.ScheduleData
import com.example.calmme.data.TimeSlot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppointmentViewModel(
    private val repository: PsikologRepository = PsikologRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _availableTimeSlots = MutableStateFlow<List<TimeSlot>>(emptyList())
    val availableTimeSlots: StateFlow<List<TimeSlot>> = _availableTimeSlots

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate

    private val _selectedTimeSlot = MutableStateFlow<TimeSlot?>(null)
    val selectedTimeSlot: StateFlow<TimeSlot?> = _selectedTimeSlot

    private val _selectedConsultationMethod = MutableStateFlow("")
    val selectedConsultationMethod: StateFlow<String> = _selectedConsultationMethod

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _scheduleData = MutableStateFlow<List<ScheduleData>>(emptyList())
    val scheduleData: StateFlow<List<ScheduleData>> = _scheduleData

    private val _workingHours = MutableStateFlow<Map<String, String>>(emptyMap())
    val workingHours: StateFlow<Map<String, String>> = _workingHours

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedPsychologist = MutableStateFlow<PsychologistData?>(null)
    val selectedPsychologist: StateFlow<PsychologistData?> = _selectedPsychologist

    private val _selectedPsychologistProfilePicture = MutableStateFlow<String?>(null)
    val selectedPsychologistProfilePicture: StateFlow<String?> = _selectedPsychologistProfilePicture

    private val _currentUserProfilePicture = MutableStateFlow<String?>(null)
    val currentUserProfilePicture: StateFlow<String?> = _currentUserProfilePicture

    init {
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

    fun loadSelectedPsychologistProfile(psychologistId: String) {
        viewModelScope.launch {
            try {
                val psychologistDoc = firestore.collection("psychologists")
                    .document(psychologistId).get().await()
                val userId = psychologistDoc.getString("userId")

                if (!userId.isNullOrEmpty()) {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    val profilePicture = userDoc.getString("profilePicture")
                    _selectedPsychologistProfilePicture.value = profilePicture
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    fun setSelectedPsychologist(psychologist: PsychologistData) {
        _selectedPsychologist.value = psychologist
        loadSelectedPsychologistProfile(psychologist.psychologistId)
        loadPsychologistSchedule(psychologist.psychologistId)
    }

    // Fungsi buat load timeslot yang tersedia (belom dipesan)
    fun loadAvailableTimeSlots(psychologistId: String, date: String) {
        if (psychologistId.isEmpty() || date.isEmpty()) return

        viewModelScope.launch {
            repository.getAvailableTimeSlots(psychologistId, date).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        filterBookedTimeSlots(resource.data, psychologistId, date)
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = resource.message
                    }
                }
            }
        }
    }

    // Fungsi buat filter timeslot yang sudah di pesan
    private suspend fun filterBookedTimeSlots(
        allTimeSlots: List<TimeSlot>,
        psychologistId: String,
        date: String
    ) {
        try {
            val bookedAppointments = firestore.collection("appointments")
                .whereEqualTo("psychologistId", psychologistId)
                .whereEqualTo("appointmentDate", date)
                .whereIn("status", listOf("scheduled", "confirmed"))
                .get()
                .await()

            val bookedTimeSlots = bookedAppointments.documents.mapNotNull { doc ->
                doc.getString("appointmentTime")
            }.toSet()

            val availableSlots = allTimeSlots.filter { timeSlot ->
                val timeRange = "${timeSlot.startTime}-${timeSlot.endTime}"
                timeRange !in bookedTimeSlots
            }

            _availableTimeSlots.value = availableSlots

        } catch (e: Exception) {
            _availableTimeSlots.value = allTimeSlots
            _errorMessage.value = "Could not filter booked slots: ${e.message}"
        }
    }

    // setter selecteddate
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        _selectedTimeSlot.value = null
        _availableTimeSlots.value = emptyList()
    }
    // setter selectedtimeslot
    fun setSelectedTimeSlot(timeSlot: TimeSlot) {
        _selectedTimeSlot.value = timeSlot
    }
    // setter consulmethod
    fun setConsultationMethod(method: String) {
        _selectedConsultationMethod.value = method
    }
    // menghapus error
    fun clearError() {
        _errorMessage.value = null
    }

    // Fungsi buat load schedule
    fun loadPsychologistSchedule(psychologistId: String) {
        if (psychologistId.isEmpty()) return

        viewModelScope.launch {
            repository.getSchedules(psychologistId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _scheduleData.value = resource.data
                        processWorkingHours(resource.data)
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = resource.message
                    }
                }
            }
        }
    }

    // Fungsi buat proses working hours dari schedule data
    private fun processWorkingHours(schedules: List<ScheduleData>) {
        val workingHoursMap = mutableMapOf<String, String>()

        schedules.forEach { schedule ->
            if (schedule.timeSlots.isNotEmpty()) {
                val earliestTime = schedule.timeSlots.minByOrNull { it.startTime }?.startTime ?: ""
                val latestTime = schedule.timeSlots.maxByOrNull { it.endTime }?.endTime ?: ""

                if (earliestTime.isNotEmpty() && latestTime.isNotEmpty()) {
                    workingHoursMap[schedule.dayOfWeek] = "$earliestTime - $latestTime"
                }
            }
        }

        _workingHours.value = workingHoursMap
    }

    // Fungsi buat bikin appointment langsung
    suspend fun createAppointmentDirectly(
        psychologistId: String,
        onSuccess: (String, Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            _isLoading.value = true

            val userDoc = firestore.collection("users").document(userId).get().await()
            val subscriptionStatus = userDoc.getString("subscriptionStatus") ?: "inactive"
            val isSubscribed = subscriptionStatus == "active"
            val timeSlot = _selectedTimeSlot.value ?: throw Exception("No time slot selected")
            val date = _selectedDate.value.ifEmpty { throw Exception("No date selected") }
            val consultationMethod = _selectedConsultationMethod.value.ifEmpty { throw Exception("No consultation method selected") }

            val appointmentData = AppointmentData(
                userId = userId,
                psychologistId = psychologistId,
                appointmentDate = date,
                appointmentTime = "${timeSlot.startTime}-${timeSlot.endTime}",
                status = "scheduled",
                paymentStatus = if (isSubscribed) "paid" else "pending",
                paymentMethod = if (isSubscribed) "subscription" else "",
                consultationMethod = consultationMethod,
                chatRoomId = "",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val docRef = firestore.collection("appointments")
                .add(appointmentData)
                .await()

            repository.handlePostAppointmentCreation(
                psychologistId = psychologistId,
                date = date,
                timeSlot = timeSlot
            )

            _isLoading.value = false
            onSuccess(docRef.id, !isSubscribed)

        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.message ?: "Failed to create appointment")
        }
    }
}