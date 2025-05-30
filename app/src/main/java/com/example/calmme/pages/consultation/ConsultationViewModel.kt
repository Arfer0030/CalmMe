package com.example.calmme.pages.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmme.data.PsikologRepository
import com.example.calmme.data.Resource
import com.example.calmme.data.PsychologistData
import com.example.calmme.data.TimeSlot
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConsultationViewModel(
    private val repository: PsikologRepository = PsikologRepository()
) : ViewModel() {

    private val _psychologists = MutableStateFlow<List<PsychologistData>>(emptyList())
    val psychologists: StateFlow<List<PsychologistData>> = _psychologists

    private val _selectedPsychologist = MutableStateFlow<PsychologistData?>(null)
    val selectedPsychologist: StateFlow<PsychologistData?> = _selectedPsychologist

    private val _selectedPsychologistId = MutableStateFlow("")
    val selectedPsychologistId: StateFlow<String> = _selectedPsychologistId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _filteredPsychologists = MutableStateFlow<List<PsychologistData>>(emptyList())
    val filteredPsychologists: StateFlow<List<PsychologistData>> = _filteredPsychologists

    // Tambahan untuk time slots hari ini
    private val _psychologistTimeSlots = MutableStateFlow<Map<String, List<TimeSlot>>>(emptyMap())
    val psychologistTimeSlots: StateFlow<Map<String, List<TimeSlot>>> = _psychologistTimeSlots

    init {
        loadPsychologists()
    }

    private fun loadPsychologists() {
        viewModelScope.launch {
            repository.getPsychologistsRealtime().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                        _errorMessage.value = null
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _psychologists.value = resource.data
                        _filteredPsychologists.value = resource.data
                        loadTodayTimeSlotsForAllPsychologists(resource.data)
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = resource.message
                    }
                }
            }
        }
    }

    fun setSelectedPsychologist(psychologist: PsychologistData) {
        _selectedPsychologist.value = psychologist
        _selectedPsychologistId.value = psychologist.psychologistId
    }

    fun setSelectedPsychologistById(psychologistId: String) {
        _selectedPsychologistId.value = psychologistId
        loadPsychologistById(psychologistId)
    }

    private fun loadPsychologistById(psychologistId: String) {
        viewModelScope.launch {
            repository.getPsychologistById(psychologistId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _selectedPsychologist.value = resource.data
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = resource.message
                    }
                }
            }
        }
    }

    fun searchPsychologists(query: String) {
        val currentList = _psychologists.value
        val filtered = if (query.isEmpty()) {
            currentList
        } else {
            currentList.filter { psychologist ->
                psychologist.name.contains(query, ignoreCase = true) ||
                        psychologist.specialization.any { it.contains(query, ignoreCase = true) } ||
                        psychologist.description.contains(query, ignoreCase = true)
            }
        }
        _filteredPsychologists.value = filtered
    }

    fun filterPsychologistsBySpecialization(specialization: String) {
        val currentList = _psychologists.value
        val filtered = if (specialization.isEmpty()) {
            currentList
        } else {
            currentList.filter { psychologist ->
                psychologist.specialization.any {
                    it.contains(specialization, ignoreCase = true)
                }
            }
        }
        _filteredPsychologists.value = filtered
    }

    fun refreshPsychologists() {
        viewModelScope.launch {
            repository.getAllPsychologists().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _psychologists.value = resource.data
                        _filteredPsychologists.value = resource.data
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = resource.message
                    }
                }
            }
        }
    }

    private fun loadTodayTimeSlotsForAllPsychologists(psychologists: List<PsychologistData>) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val timeSlotsMap = mutableMapOf<String, List<TimeSlot>>()

            psychologists.forEach { psychologist ->
                repository.getAvailableTimeSlots(psychologist.psychologistId, today)
                    .collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                timeSlotsMap[psychologist.psychologistId] = resource.data
                                _psychologistTimeSlots.value = timeSlotsMap.toMap()
                            }
                            is Resource.Error -> {
                                timeSlotsMap[psychologist.psychologistId] = emptyList()
                                _psychologistTimeSlots.value = timeSlotsMap.toMap()
                            }
                            is Resource.Loading -> {
                                // Loading handled by main loading state
                            }
                        }
                    }
            }
        }
    }

    fun getTodayTimeSlotsForPsychologist(psychologistId: String): List<TimeSlot> {
        return _psychologistTimeSlots.value[psychologistId] ?: emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }

}
