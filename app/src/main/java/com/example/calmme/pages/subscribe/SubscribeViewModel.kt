package com.example.calmme.pages.subscribe

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// SubscriptionViewModel.kt
class SubscribeViewModel : ViewModel() {
    private val _selectedPlan = MutableStateFlow("")
    val selectedPlan: StateFlow<String> = _selectedPlan

    private val _selectedPayment = MutableStateFlow("")
    val selectedPayment: StateFlow<String> = _selectedPayment

    fun selectPlan(planId: String) {
        _selectedPlan.value = planId
    }

    fun selectPayment(paymentId: String) {
        _selectedPayment.value = paymentId
    }
}
