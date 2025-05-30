package com.example.calmme.pages.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmme.data.PsikologRepository
import com.example.calmme.data.Resource
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
                        _availableTimeSlots.value = resource.data
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = resource.message
                    }
                }
            }
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
            repository.bookAppointment(
                psychologistId = psychologistId,
                userId = userId,
                date = date,
                timeSlot = timeSlot,
                consultationMethod = consultationMethod
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
}

