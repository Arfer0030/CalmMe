package com.example.calmme.pages.subscribe

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class SubscribeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _selectedPlan = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _paymentType = MutableStateFlow("")

    fun selectPlan(planId: String) {
        _selectedPlan.value = planId
        _paymentType.value = if (planId == "consultation") "consultation" else "subscription"
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

            val subscriptionData = mapOf(
                "subscriptionId" to subscriptionId,
                "userId" to userId,
                "startDate" to startDate,
                "endDate" to endDate,
                "status" to "",
                "paymentMethod" to "",
                "createdAt" to Timestamp.now()
            )

            val paymentData = mapOf(
                "paymentId" to paymentId,
                "userId" to userId,
                "type" to "subscription",
                "appointmentId" to null,
                "subscriptionId" to subscriptionId,
                "paymentMethod" to "",
                "status" to "pending",
                "createdAt" to Timestamp.now()
            )

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
            val appointmentsQuery = firestore.collection("appointments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("paymentStatus", "pending")
                .limit(1)
                .get()
                .await()

            if (appointmentsQuery.isEmpty) {
                _isLoading.value = false
                onError("No pending appointments found. Please book an appointment first.")
                return
            }

            val oldestAppointment = appointmentsQuery.documents.first()
            val appointmentId = oldestAppointment.id
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

            firestore.collection("payments").document(paymentId).set(paymentData).await()
            _isLoading.value = false
            onSuccess()
        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.message ?: "Failed to process consultation payment")
        }
    }

    suspend fun updatePaymentMethod(
        paymentMethod: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            _isLoading.value = true

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

            firestore.collection("subscriptions").document(subscriptionDoc.id)
                .update("paymentMethod", paymentMethod)
                .await()

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
            }

            updateAppointmentsPaymentMethod(userId, paymentMethod)
            _isLoading.value = false
            onSuccess()
        } catch (e: Exception) {
            _isLoading.value = false
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

            firestore.collection("payments").document(paymentDoc.id)
                .update("paymentMethod", paymentMethod)
                .await()

            if (appointmentId != null) {
                firestore.collection("appointments").document(appointmentId)
                    .update("paymentMethod", paymentMethod)
                    .await()
            }

            _isLoading.value = false
            onSuccess()
        } catch (e: Exception) {
            _isLoading.value = false
            onError("Failed to update consultation payment method: ${e.localizedMessage}")
        }
    }

    suspend fun finalizePayment(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            _isLoading.value = true

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
            onError(e.message ?: "Failed to finalize payment")
        }
    }

    private suspend fun finalizeSubscriptionPayment(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
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

            firestore.collection("payments").document(paymentDoc.id)
                .update("status", "success")
                .await()

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
                }
            }

            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "subscriptionStatus" to "active",
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()

            updateUserAppointmentsToPaid(userId)
            _isLoading.value = false
            onSuccess()
        } catch (e: Exception) {
            _isLoading.value = false
            onError("Failed to finalize subscription payment: ${e.localizedMessage}")
        }
    }

    private suspend fun finalizeConsultationPayment(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
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

            firestore.collection("payments").document(paymentDoc.id)
                .update("status", "success")
                .await()

            if (appointmentId != null) {
                firestore.collection("appointments").document(appointmentId)
                    .update(
                        mapOf(
                            "paymentStatus" to "paid",
                            "updatedAt" to Timestamp.now()
                        )
                    )
                    .await()
            }

            _isLoading.value = false
            onSuccess()
        } catch (e: Exception) {
            _isLoading.value = false
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

        } catch (_: Exception) {
        }
    }

    private suspend fun updateAppointmentsPaymentMethod(userId: String, paymentMethod: String) {
        try {
            val appointmentsQuery = firestore.collection("appointments")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            appointmentsQuery.documents.forEach { doc ->
                firestore.collection("appointments").document(doc.id)
                    .update("paymentMethod", paymentMethod)
                    .await()
            }
        } catch (_: Exception) {
        }
    }

}

