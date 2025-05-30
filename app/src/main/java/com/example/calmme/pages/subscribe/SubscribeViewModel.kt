package com.example.calmme.pages.subscribe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class SubscribeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _selectedPlan = MutableStateFlow("")
    val selectedPlan: StateFlow<String> = _selectedPlan

    private val _selectedPaymentMethod = MutableStateFlow("")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _paymentType = MutableStateFlow("")
    val paymentType: StateFlow<String> = _paymentType

    fun selectPlan(planId: String) {
        _selectedPlan.value = planId
        // Set payment type berdasarkan plan
        _paymentType.value = if (planId == "consultation") "consultation" else "subscription"
    }

    fun selectPaymentMethod(paymentMethod: String) {
        _selectedPaymentMethod.value = paymentMethod
    }

    suspend fun processPayment(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            _isLoading.value = true

            when (_paymentType.value) {
                "subscription" -> {
                    processSubscriptionPayment(userId, onSuccess, onError)
                }

                "consultation" -> {
                    processConsultationPayment(userId, onSuccess, onError)
                }

                else -> {
                    onError("Invalid payment type")
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.message ?: "Payment failed")
        }
    }

    private suspend fun processSubscriptionPayment(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val subscriptionId = UUID.randomUUID().toString()
            val paymentId = UUID.randomUUID().toString()

            val calendar = Calendar.getInstance()
            val startDate = Timestamp.now()
            calendar.add(Calendar.MONTH, 1)
            val endDate = Timestamp(calendar.time)

            // Create subscription dengan status dan paymentMethod kosong
            val subscriptionData = mapOf(
                "subscriptionId" to subscriptionId,
                "userId" to userId,
                "startDate" to startDate,
                "endDate" to endDate,
                "status" to "", // Kosongkan dulu
                "paymentMethod" to "", // Kosongkan dulu
                "createdAt" to Timestamp.now()
            )

            // Create payment record dengan status pending dan paymentMethod kosong
            val paymentData = mapOf(
                "paymentId" to paymentId,
                "userId" to userId,
                "type" to "subscription",
                "appointmentId" to null,
                "subscriptionId" to subscriptionId,
                "paymentMethod" to "", // Kosongkan dulu
                "status" to "pending", // Set ke pending
                "createdAt" to Timestamp.now()
            )

            // Save to Firestore
            firestore.collection("subscriptions").document(subscriptionId).set(subscriptionData)
                .await()
            firestore.collection("payments").document(paymentId).set(paymentData).await()

            _isLoading.value = false
            onSuccess()

        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.message ?: "Failed to process subscription")
        }
    }

    private suspend fun processConsultationPayment(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val paymentId = UUID.randomUUID().toString()

            // Perbaikan 1: Hapus orderBy untuk menghindari masalah index
            val appointmentsQuery = firestore.collection("appointments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("paymentStatus", "pending")
                .limit(1) // Ambil satu saja tanpa ordering
                .get()
                .await()

            if (appointmentsQuery.isEmpty) {
                _isLoading.value = false
                onError("No pending appointments found. Please book an appointment first.")
                return
            }

            val oldestAppointment = appointmentsQuery.documents.first()
            val appointmentId = oldestAppointment.id

            // Create payment record untuk consultation
            val paymentData = mapOf(
                "paymentId" to paymentId,
                "userId" to userId,
                "type" to "consultation",
                "appointmentId" to appointmentId,
                "subscriptionId" to null,
                "paymentMethod" to "",
                "status" to "pending",
                "createdAt" to Timestamp.now()
            )

            // Save payment
            firestore.collection("payments").document(paymentId).set(paymentData).await()

            _isLoading.value = false
            onSuccess()

        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.message ?: "Failed to process consultation payment")
        }
    }


    // Di SubscribeViewModel.kt - Perbaiki fungsi updatePaymentMethod
    suspend fun updatePaymentMethod(
        paymentMethod: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            _isLoading.value = true

            Log.d("SubscribeVM", "Updating payment method to: $paymentMethod for user: $userId")
            Log.d("SubscribeVM", "Selected plan: ${_selectedPlan.value}")

            when (_selectedPlan.value) {
                "subscription" -> {
                    updateSubscriptionPaymentMethod(userId, paymentMethod, onSuccess, onError)
                }

                "consultation" -> {
                    updateConsultationPaymentMethod(userId, paymentMethod, onSuccess, onError)
                }

                else -> {
                    _isLoading.value = false
                    onError("Invalid payment type")
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.message ?: "Failed to update payment method")
        }
    }

    private suspend fun updateSubscriptionPaymentMethod(
        userId: String,
        paymentMethod: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d("SubscribeVM", "Looking for subscription for user: $userId")

            // Load subscription data yang sudah ada
            val subscriptionsQuery = firestore.collection("subscriptions")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (subscriptionsQuery.isEmpty) {
                _isLoading.value = false
                onError("No subscription found. Please select a plan first.")
                return
            }

            val subscriptionDoc = subscriptionsQuery.documents.first()
            val subscriptionData = subscriptionDoc.data
            val subscriptionId = subscriptionData?.get("subscriptionId") as? String ?: subscriptionDoc.id

            Log.d("SubscribeVM", "Found subscription: $subscriptionId")

            // Update payment method di subscription
            firestore.collection("subscriptions").document(subscriptionDoc.id)
                .update("paymentMethod", paymentMethod)
                .await()

            Log.d("SubscribeVM", "Updated subscription payment method")

            // Update payment method di payments collection
            val paymentsQuery = firestore.collection("payments")
                .whereEqualTo("subscriptionId", subscriptionId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (!paymentsQuery.isEmpty) {
                val paymentDoc = paymentsQuery.documents.first()
                firestore.collection("payments").document(paymentDoc.id)
                    .update("paymentMethod", paymentMethod)
                    .await()

                Log.d("SubscribeVM", "Updated payment method in payments collection")
            }

            // TAMBAHAN: Update payment method di semua appointments user
            updateAppointmentsPaymentMethod(userId, paymentMethod)

            _isLoading.value = false
            onSuccess()

        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("SubscribeVM", "Error updating subscription payment method", e)
            onError("Failed to update subscription payment method: ${e.localizedMessage}")
        }
    }


    private suspend fun updateConsultationPaymentMethod(
        userId: String,
        paymentMethod: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d("SubscribeVM", "Looking for consultation payment for user: $userId")

            // Load consultation payment data yang sudah ada
            val paymentsQuery = firestore.collection("payments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "consultation")
                .whereEqualTo("status", "pending")
                .limit(1)
                .get()
                .await()

            if (paymentsQuery.isEmpty) {
                _isLoading.value = false
                onError("No pending consultation payment found. Please select consultation plan first.")
                return
            }

            val paymentDoc = paymentsQuery.documents.first()
            val appointmentId = paymentDoc.getString("appointmentId")

            // Update payment method di payments collection
            firestore.collection("payments").document(paymentDoc.id)
                .update("paymentMethod", paymentMethod)
                .await()

            Log.d("SubscribeVM", "Updated consultation payment method")

            // TAMBAHAN: Update payment method di appointment yang terkait
            if (appointmentId != null) {
                firestore.collection("appointments").document(appointmentId)
                    .update("paymentMethod", paymentMethod)
                    .await()

                Log.d("SubscribeVM", "Updated payment method in appointment: $appointmentId")
            }

            _isLoading.value = false
            onSuccess()

        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("SubscribeVM", "Error updating consultation payment method", e)
            onError("Failed to update consultation payment method: ${e.localizedMessage}")
        }
    }


    // Di SubscribeViewModel.kt - Tambahkan fungsi ini
    suspend fun finalizePayment(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            _isLoading.value = true

            Log.d("SubscribeVM", "Finalizing payment for user: $userId")
            Log.d("SubscribeVM", "Payment type: ${_paymentType.value}")

            when (_paymentType.value) {
                "subscription" -> {
                    finalizeSubscriptionPayment(userId, onSuccess, onError)
                }
                "consultation" -> {
                    finalizeConsultationPayment(userId, onSuccess, onError)
                }
                else -> {
                    _isLoading.value = false
                    onError("Invalid payment type")
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("SubscribeVM", "Error finalizing payment", e)
            onError(e.message ?: "Failed to finalize payment")
        }
    }

    private suspend fun finalizeSubscriptionPayment(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // 1. Update payment status menjadi success
            val paymentsQuery = firestore.collection("payments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "subscription")
                .whereEqualTo("status", "pending")
                .limit(1)
                .get()
                .await()

            if (paymentsQuery.isEmpty) {
                _isLoading.value = false
                onError("No pending subscription payment found")
                return
            }

            val paymentDoc = paymentsQuery.documents.first()
            val subscriptionId = paymentDoc.getString("subscriptionId")

            // Update payment status ke success
            firestore.collection("payments").document(paymentDoc.id)
                .update("status", "success")
                .await()

            Log.d("SubscribeVM", "Updated payment status to success")

            // 2. Update subscription status menjadi active
            if (subscriptionId != null) {
                val subscriptionsQuery = firestore.collection("subscriptions")
                    .whereEqualTo("subscriptionId", subscriptionId)
                    .limit(1)
                    .get()
                    .await()

                if (!subscriptionsQuery.isEmpty) {
                    val subscriptionDoc = subscriptionsQuery.documents.first()
                    firestore.collection("subscriptions").document(subscriptionDoc.id)
                        .update("status", "active")
                        .await()

                    Log.d("SubscribeVM", "Updated subscription status to active")
                }
            }

            // 3. Update user subscription status
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "subscriptionStatus" to "active",
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            Log.d("SubscribeVM", "Updated user subscription status to active")

            // 4. Update semua pending appointments menjadi paid
            updateUserAppointmentsToPaid(userId)

            _isLoading.value = false
            onSuccess()

        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("SubscribeVM", "Error finalizing subscription payment", e)
            onError("Failed to finalize subscription payment: ${e.localizedMessage}")
        }
    }

    private suspend fun finalizeConsultationPayment(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // 1. Update payment status menjadi success
            val paymentsQuery = firestore.collection("payments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "consultation")
                .whereEqualTo("status", "pending")
                .limit(1)
                .get()
                .await()

            if (paymentsQuery.isEmpty) {
                _isLoading.value = false
                onError("No pending consultation payment found")
                return
            }

            val paymentDoc = paymentsQuery.documents.first()
            val appointmentId = paymentDoc.getString("appointmentId")

            // Update payment status ke success
            firestore.collection("payments").document(paymentDoc.id)
                .update("status", "success")
                .await()

            Log.d("SubscribeVM", "Updated consultation payment status to success")

            // 2. Update appointment payment status menjadi paid
            if (appointmentId != null) {
                firestore.collection("appointments").document(appointmentId)
                    .update(
                        mapOf(
                            "paymentStatus" to "paid",
                            "updatedAt" to Timestamp.now()
                        )
                    )
                    .await()

                Log.d("SubscribeVM", "Updated appointment payment status to paid")
            }

            _isLoading.value = false
            onSuccess()

        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("SubscribeVM", "Error finalizing consultation payment", e)
            onError("Failed to finalize consultation payment: ${e.localizedMessage}")
        }
    }

    private suspend fun updateUserAppointmentsToPaid(userId: String) {
        try {
            val appointments = firestore.collection("appointments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("paymentStatus", "pending")
                .get()
                .await()

            appointments.documents.forEach { doc ->
                firestore.collection("appointments").document(doc.id)
                    .update(
                        mapOf(
                            "paymentStatus" to "paid",
                            "paymentMethod" to "subscription",
                            "updatedAt" to Timestamp.now()
                        )
                    )
                    .await()
            }

            Log.d("SubscribeVM", "Updated ${appointments.size()} appointments to paid")
        } catch (e: Exception) {
            Log.e("SubscribeVM", "Error updating appointments", e)
            // Silent fail untuk appointment updates
        }
    }

    // Tambahkan fungsi helper ini di SubscribeViewModel
    private suspend fun updateAppointmentsPaymentMethod(userId: String, paymentMethod: String) {
        try {
            // Update payment method untuk semua appointments user (untuk subscription)
            val appointmentsQuery = firestore.collection("appointments")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            appointmentsQuery.documents.forEach { doc ->
                firestore.collection("appointments").document(doc.id)
                    .update("paymentMethod", paymentMethod)
                    .await()
            }

            Log.d("SubscribeVM", "Updated payment method for ${appointmentsQuery.size()} appointments")

        } catch (e: Exception) {
            Log.e("SubscribeVM", "Error updating appointments payment method", e)
            // Silent fail untuk appointment updates
        }
    }

}

