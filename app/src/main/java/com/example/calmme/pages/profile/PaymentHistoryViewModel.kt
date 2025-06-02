package com.example.calmme.pages.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class PaymentHistoryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _paymentHistory = MutableStateFlow<List<PaymentHistoryItem>>(emptyList())
    val paymentHistory: StateFlow<List<PaymentHistoryItem>> = _paymentHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPaymentHistory()
    }

    fun loadPaymentHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = auth.currentUser?.uid ?: return@launch
            val resultList = mutableListOf<PaymentHistoryItem>()

            // 1. Load Subscription Payments
            val subscriptions = firestore.collection("subscriptions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            subscriptions.documents.forEach { subDoc ->
                val planName = "SUBSCRIPTION"
                val amount = "Rp 275.000,00" // Sesuaikan harga subscription Anda
                val startTimestamp = subDoc.getTimestamp("startDate")
                val endTimestamp = subDoc.getTimestamp("endDate")
                val orderedOn = startTimestamp?.let { formatDate(it) } ?: "-"
                val activeUntil = endTimestamp?.let { formatDate(it) } ?: "-"

                // Cari payment yang terkait subscription ini
                val payments = firestore.collection("payments")
                    .whereEqualTo("subscriptionId", subDoc.getString("subscriptionId"))
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("type", "subscription")
                    .whereEqualTo("status", "success")
                    .get()
                    .await()

                if (!payments.isEmpty) {
                    // Tampilkan hanya payment yang sudah sukses
                    resultList.add(
                        PaymentHistoryItem(
                            planName = planName,
                            amount = amount,
                            orderedOn = orderedOn,
                            activeUntil = activeUntil
                        )
                    )
                }
            }

            // 2. Load BASIC (Consultation) Payments
            val payments = firestore.collection("payments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "consultation")
                .whereEqualTo("status", "success")
                .get()
                .await()

            payments.documents.forEach { payDoc ->
                val planName = "BASIC"
                val amount = "Rp 35.000,00" // Sesuaikan harga basic/consultation Anda
                val orderedOn = payDoc.getTimestamp("createdAt")?.let { formatDate(it) } ?: "-"
                // Untuk consultation, activeUntil bisa diisi sesuai kebutuhan (misal, 1 hari setelah orderedOn)
                val activeUntil = payDoc.getTimestamp("createdAt")?.let {
                    formatDate(it, plusDays = 1)
                } ?: "-"

                resultList.add(
                    PaymentHistoryItem(
                        planName = planName,
                        amount = amount,
                        orderedOn = orderedOn,
                        activeUntil = activeUntil
                    )
                )
            }

            // Urutkan dari pembayaran terbaru ke terlama
            _paymentHistory.value = resultList.sortedByDescending { it.orderedOn }
            _isLoading.value = false
        }
    }

    private fun formatDate(timestamp: Timestamp, plusDays: Int = 0): String {
        val cal = Calendar.getInstance()
        cal.time = timestamp.toDate()
        if (plusDays > 0) cal.add(Calendar.DAY_OF_MONTH, plusDays)
        val sdf = SimpleDateFormat("MMMM dd, yyyy HH:mm a", Locale.getDefault())
        return sdf.format(cal.time)
    }

    data class PaymentHistoryItem(
        val planName: String,
        val amount: String,
        val orderedOn: String,
        val activeUntil: String
    )
}

