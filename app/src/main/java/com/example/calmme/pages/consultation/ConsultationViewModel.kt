package com.example.calmme.pages.consultation

import androidx.lifecycle.ViewModel
import com.example.calmme.R
import com.example.calmme.data.psychologistss
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConsultationViewModel : ViewModel() {

    private val _psychologists = MutableStateFlow<List<PshycologistItem>>(emptyList())
    val psychologists: StateFlow<List<PshycologistItem>> = _psychologists

    private val _selectedPsychologist = MutableStateFlow<PshycologistItem?>(null)
    val selectedPsychologist: StateFlow<PshycologistItem?> = _selectedPsychologist

    init {
        // Simulasi load data
        _psychologists.value = psychologistss
    }

    fun setSelectedPsychologist(psychologist: PshycologistItem) {
        _selectedPsychologist.value = psychologist
    }

}