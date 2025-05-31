package com.example.calmme.pages.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmme.data.PsikologRepository
import com.example.calmme.commons.Resource
import com.example.calmme.data.ScheduleData
import com.example.calmme.data.TimeSlot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AppointmentData(
    val appointmentId: String = "",
    val userId: String = "",
    val psychologistId: String = "",
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val status: String = "scheduled",
    val paymentStatus: String = "pending",
    val paymentMethod: String = "",
    val chatRoomId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

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

    private val _bookingStatus = MutableStateFlow<Resource<String>?>(null)
    val bookingStatus: StateFlow<Resource<String>?> = _bookingStatus

    private val _scheduleData = MutableStateFlow<List<ScheduleData>>(emptyList())
    val scheduleData: StateFlow<List<ScheduleData>> = _scheduleData

    private val _workingHours = MutableStateFlow<Map<String, String>>(emptyMap())
    val workingHours: StateFlow<Map<String, String>> = _workingHours

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Di AppointmentViewModel.kt - Update loadAvailableTimeSlots
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
                        // Filter out booked time slots
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

    // Tambahkan fungsi baru untuk filter
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


    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        _selectedTimeSlot.value = null // Reset time slot when date changes
        _availableTimeSlots.value = emptyList()
    }

    fun setSelectedTimeSlot(timeSlot: TimeSlot) {
        _selectedTimeSlot.value = timeSlot
    }

    fun setConsultationMethod(method: String) {
        _selectedConsultationMethod.value = method
    }

    suspend fun bookAppointment(
        psychologistId: String,
        paymentMethod: String = "pending"
    ): Resource<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val timeSlot = _selectedTimeSlot.value ?: throw Exception("No time slot selected")
            val date = _selectedDate.value.ifEmpty { throw Exception("No date selected") }
            val consultationMethod = _selectedConsultationMethod.value.ifEmpty { throw Exception("No consultation method selected") }

            _isLoading.value = true

            val appointmentData = mapOf(
                "userId" to userId,
                "psychologistId" to psychologistId,
                "appointmentDate" to date,
                "appointmentTime" to "${timeSlot.startTime}-${timeSlot.endTime}",
                "status" to "scheduled",
                "paymentStatus" to "pending",
                "paymentMethod" to paymentMethod,
                "consultationMethod" to consultationMethod,
                "chatRoomId" to "",
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            val docRef = firestore.collection("appointments")
                .add(appointmentData)
                .await()

            // Update time slot availability
            repository.handlePostAppointmentCreation( // Panggil fungsi yang sudah diubah
                psychologistId = psychologistId,
                date = date,
                timeSlot = timeSlot
            )

            _isLoading.value = false
            Resource.Success(docRef.id)

        } catch (e: Exception) {
            _isLoading.value = false
            Resource.Error(e.message ?: "Failed to book appointment")
        }
    }

    fun clearBookingStatus() {
        _bookingStatus.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Tambahkan fungsi untuk load schedule
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

    // Fungsi untuk memproses working hours dari schedule data
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

    // Fungsi helper untuk mendapatkan working hours summary
    fun getWorkingHoursSummary(): String {
        val workingHours = _workingHours.value
        if (workingHours.isEmpty()) return "Working hours not available"

        // Group similar time ranges
        val timeRanges = workingHours.values.distinct()
        val daysWithSameHours = mutableMapOf<String, MutableList<String>>()

        workingHours.forEach { (day, hours) ->
            if (daysWithSameHours[hours] == null) {
                daysWithSameHours[hours] = mutableListOf()
            }
            daysWithSameHours[hours]!!.add(day.replaceFirstChar { it.uppercase() })
        }

        return daysWithSameHours.entries.joinToString("\n") { (hours, days) ->
            val dayRange = when {
                days.size == 1 -> days.first()
                days.size == 2 -> "${days.first()} & ${days.last()}"
                else -> "${days.first()} - ${days.last()}"
            }
            "$dayRange: $hours"
        }
    }


    // Di AppointmentViewModel.kt - Simplify fungsi ini
    // Di AppointmentViewModel.kt - Ubah fungsi ini untuk selalu membuat appointment
    suspend fun checkUserSubscriptionAndBook(
        psychologistId: String,
        onNavigateToSubscription: () -> Unit,
        onBookingSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            _isLoading.value = true

            // Check user subscription status
            val userDoc = firestore.collection("users").document(userId).get().await()
            val subscriptionStatus = userDoc.getString("subscriptionStatus") ?: "inactive"

            if (subscriptionStatus == "active") {
                // User has active subscription - book with paid status
                val result = bookAppointmentWithSubscription(psychologistId)
                when (result) {
                    is Resource.Success -> onBookingSuccess(result.data)
                    is Resource.Error -> onError(result.message)
                    else -> onError("Unknown error occurred")
                }
            } else {
                // User doesn't have active subscription - book with pending payment, then navigate
                val result = bookAppointmentWithPendingPayment(psychologistId)
                when (result) {
                    is Resource.Success -> {
                        onBookingSuccess(result.data)
                        // Navigate ke subscription setelah appointment berhasil dibuat
                        onNavigateToSubscription()
                    }
                    is Resource.Error -> onError(result.message)
                    else -> onError("Unknown error occurred")
                }
            }

        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.message ?: "Failed to check subscription status")
        }
    }

    // Di AppointmentViewModel.kt - Tambahkan fungsi ini
    private suspend fun bookAppointmentWithPendingPayment(psychologistId: String): Resource<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val timeSlot = _selectedTimeSlot.value ?: throw Exception("No time slot selected")
            val date = _selectedDate.value.ifEmpty { throw Exception("No date selected") }
            val consultationMethod = _selectedConsultationMethod.value.ifEmpty { throw Exception("No consultation method selected") }

            val appointmentData = mapOf(
                "userId" to userId,
                "psychologistId" to psychologistId,
                "appointmentDate" to date,
                "appointmentTime" to "${timeSlot.startTime}-${timeSlot.endTime}",
                "status" to "scheduled",
                "paymentStatus" to "pending", // Set to pending for inactive users
                "paymentMethod" to "", // Empty until payment is made
                "consultationMethod" to consultationMethod,
                "chatRoomId" to "",
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            val docRef = firestore.collection("appointments")
                .add(appointmentData)
                .await()

            // Update time slot availability
            repository.handlePostAppointmentCreation( // Panggil fungsi yang sudah diubah
                psychologistId = psychologistId,
                date = date,
                timeSlot = timeSlot
            )

            _isLoading.value = false
            Resource.Success(docRef.id)

        } catch (e: Exception) {
            _isLoading.value = false
            Resource.Error(e.message ?: "Failed to book appointment")
        }
    }

    // Di AppointmentViewModel.kt - Fungsi alternatif yang lebih sederhana
    suspend fun createAppointmentDirectly(
        psychologistId: String,
        onSuccess: (String, Boolean) -> Unit, // (appointmentId, needsPayment)
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            _isLoading.value = true

            // Check subscription status
            val userDoc = firestore.collection("users").document(userId).get().await()
            val subscriptionStatus = userDoc.getString("subscriptionStatus") ?: "inactive"
            val isSubscribed = subscriptionStatus == "active"

            // Create appointment regardless of subscription status
            val timeSlot = _selectedTimeSlot.value ?: throw Exception("No time slot selected")
            val date = _selectedDate.value.ifEmpty { throw Exception("No date selected") }
            val consultationMethod = _selectedConsultationMethod.value.ifEmpty { throw Exception("No consultation method selected") }

            val appointmentData = mapOf(
                "userId" to userId,
                "psychologistId" to psychologistId,
                "appointmentDate" to date,
                "appointmentTime" to "${timeSlot.startTime}-${timeSlot.endTime}",
                "status" to "scheduled",
                "paymentStatus" to if (isSubscribed) "paid" else "pending",
                "paymentMethod" to if (isSubscribed) "subscription" else "",
                "consultationMethod" to consultationMethod,
                "chatRoomId" to "",
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            val docRef = firestore.collection("appointments")
                .add(appointmentData)
                .await()

            // Update time slot availability
            repository.handlePostAppointmentCreation( // Panggil fungsi yang sudah diubah
                psychologistId = psychologistId,
                date = date,
                timeSlot = timeSlot
            )

            _isLoading.value = false
            onSuccess(docRef.id, !isSubscribed) // Return whether payment is needed

        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.message ?: "Failed to create appointment")
        }
    }




    private suspend fun bookAppointmentWithSubscription(psychologistId: String): Resource<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val timeSlot = _selectedTimeSlot.value ?: throw Exception("No time slot selected")
            val date = _selectedDate.value.ifEmpty { throw Exception("No date selected") }
            val consultationMethod = _selectedConsultationMethod.value.ifEmpty { throw Exception("No consultation method selected") }

            val appointmentData = mapOf(
                "userId" to userId,
                "psychologistId" to psychologistId,
                "appointmentDate" to date,
                "appointmentTime" to "${timeSlot.startTime}-${timeSlot.endTime}",
                "status" to "scheduled",
                "paymentStatus" to "paid", // Set to paid for subscription users
                "paymentMethod" to "subscription", // Set method to subscription
                "consultationMethod" to consultationMethod,
                "chatRoomId" to "",
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            val docRef = firestore.collection("appointments")
                .add(appointmentData)
                .await()

            // Update time slot availability
            repository.handlePostAppointmentCreation( // Panggil fungsi yang sudah diubah
                psychologistId = psychologistId,
                date = date,
                timeSlot = timeSlot
            )

            _isLoading.value = false
            Resource.Success(docRef.id)

        } catch (e: Exception) {
            _isLoading.value = false
            Resource.Error(e.message ?: "Failed to book appointment")
        }
    }
}