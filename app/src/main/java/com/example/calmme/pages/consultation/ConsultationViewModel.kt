package com.example.calmme.pages.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmme.commons.Resource
import com.example.calmme.data.PsikologRepository
import com.example.calmme.data.PsychologistData
import com.example.calmme.data.TimeSlot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultationViewModel(
    private val repository: PsikologRepository = PsikologRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _psychologists = MutableStateFlow<List<PsychologistData>>(emptyList())

    private val _selectedPsychologist = MutableStateFlow<PsychologistData?>(null)
    val selectedPsychologist: StateFlow<PsychologistData?> = _selectedPsychologist

    private val _selectedPsychologistId = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _filteredPsychologists = MutableStateFlow<List<PsychologistData>>(emptyList())
    val filteredPsychologists: StateFlow<List<PsychologistData>> = _filteredPsychologists

    private val _psychologistTimeSlots = MutableStateFlow<Map<String, List<TimeSlot>>>(emptyMap())

    private val _currentUserProfilePicture = MutableStateFlow<String?>(null)
    val currentUserProfilePicture: StateFlow<String?> = _currentUserProfilePicture

    private val _psychologistProfilePictures = MutableStateFlow<Map<String, String?>>(emptyMap())
    val psychologistProfilePictures: StateFlow<Map<String, String?>> = _psychologistProfilePictures

    init {
        loadPsychologists()
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
                        loadPsychologistProfilePictures(resource.data)
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = resource.message
                    }
                }
            }
        }
    }

    private fun loadPsychologistProfilePictures(psychologists: List<PsychologistData>) {
        viewModelScope.launch {
            val profilePicturesMap = mutableMapOf<String, String?>()

            psychologists.forEach { psychologist ->
                try {
                    val psychologistDoc = firestore.collection("psychologists")
                        .document(psychologist.psychologistId).get().await()
                    val userId = psychologistDoc.getString("userId")

                    if (!userId.isNullOrEmpty()) {
                        val userDoc = firestore.collection("users").document(userId).get().await()
                        val profilePicture = userDoc.getString("profilePicture")
                        profilePicturesMap[psychologist.psychologistId] = profilePicture
                    } else {
                        profilePicturesMap[psychologist.psychologistId] = null
                    }
                } catch (e: Exception) {
                    profilePicturesMap[psychologist.psychologistId] = null
                }
            }

            _psychologistProfilePictures.value = profilePicturesMap
        }
    }

    fun setSelectedPsychologist(psychologist: PsychologistData) {
        _selectedPsychologist.value = psychologist
        _selectedPsychologistId.value = psychologist.psychologistId
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
                        loadPsychologistProfilePictures(resource.data)
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

                            }
                        }
                    }
            }
        }
    }

    fun getTodayTimeSlotsForPsychologist(psychologistId: String): List<TimeSlot> {
        return _psychologistTimeSlots.value[psychologistId] ?: emptyList()
    }
}