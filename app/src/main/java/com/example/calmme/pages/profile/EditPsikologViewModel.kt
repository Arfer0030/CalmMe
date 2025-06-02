package com.example.calmme.pages.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmme.data.PsychologistData
import com.example.calmme.data.ScheduleData
import com.example.calmme.data.TimeSlot
import com.example.calmme.data.PsikologRepository
import com.example.calmme.commons.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditPsikologViewModel (
    private val repository: PsikologRepository = PsikologRepository()
) : ViewModel() {

    private val _psychologistData = MutableStateFlow(PsychologistData())
    val psychologistData: StateFlow<PsychologistData> = _psychologistData

    private val _schedules = MutableStateFlow<List<ScheduleData>>(emptyList())
    val schedules: StateFlow<List<ScheduleData>> = _schedules

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)

    init {
        loadPsychologistData()
    }

    fun loadPsychologistData() {
        viewModelScope.launch {
            repository.getCurrentPsychologistData().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                        _errorMessage.value = null
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _psychologistData.value = resource.data ?: PsychologistData()

                        resource.data?.let { psychologist ->
                            if (psychologist.psychologistId.isNotEmpty()) {
                                loadSchedules(psychologist.psychologistId)
                            }
                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = resource.message
                    }
                }
            }
        }
    }

    fun loadSchedules(psychologistId: String = _psychologistData.value.psychologistId) {
        if (psychologistId.isEmpty()) return

        viewModelScope.launch {
            repository.getSchedules(psychologistId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        _schedules.value = resource.data
                    }
                    is Resource.Error -> {
                        _errorMessage.value = resource.message
                    }
                }
            }
        }
    }

    fun updatePsychologistData(
        name: String,
        specialization: List<String>,
        description: String,
        experience: String,
        education: String,
        license: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = repository.updatePsychologistData(
                name, specialization, description, experience, education, license
            )) {
                is Resource.Success -> {
                    _psychologistData.value = result.data
                    _isLoading.value = false
                    onSuccess()
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = result.message
                    onError(result.message)
                }
                is Resource.Loading -> {

                }
            }
        }
    }

    fun updateSchedule(
        dayOfWeek: String,
        timeSlots: List<TimeSlot>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val psychologistId = _psychologistData.value.psychologistId

            when (val result = repository.updateSchedule(psychologistId, dayOfWeek, timeSlots)) {
                is Resource.Success -> {
                    loadSchedules()
                    onSuccess()
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    onError(result.message)
                }
                is Resource.Loading -> {

                }
            }
        }
    }
}
