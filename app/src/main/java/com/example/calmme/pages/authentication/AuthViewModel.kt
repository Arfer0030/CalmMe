package com.example.calmme.pages.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    // Login dengan email atau username
    fun login(identifier: String, password: String) {
        _authState.value = AuthState.Loading

        // Cek apakah identifier adalah email atau username
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            // Login dengan email
            auth.signInWithEmailAndPassword(identifier, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                    }
                }
        } else {
            // Login dengan username, cari email dari username di Firestore
            firestore.collection("users")
                .whereEqualTo("username", identifier)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val email = documents.documents[0].getString("email")
                        if (email != null) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        _authState.value = AuthState.Authenticated
                                    } else {
                                        _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                                    }
                                }
                        } else {
                            _authState.value = AuthState.Error("Email not found for username")
                        }
                    } else {
                        _authState.value = AuthState.Error("Username not found")
                    }
                }
                .addOnFailureListener { e ->
                    _authState.value = AuthState.Error(e.message ?: "Something went wrong")
                }
        }
    }

    // Signup dengan menyimpan username di Firestore
    fun signup(username: String, email: String, password: String) {
        _authState.value = AuthState.Loading

        // Cek apakah username sudah ada
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Username belum ada, lanjutkan signup
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Simpan data user di Firestore
                                val userId = auth.currentUser?.uid ?: ""
                                val userMap = hashMapOf(
                                    "username" to username,
                                    "email" to email,
                                    "gender" to "",
                                    "dateOfBirth" to "",
                                    "role" to "user",
                                    "subscriptionStatus" to "inactive",
                                    "subscriptionStartDate" to "",
                                    "subscriptionEndDate" to "",
                                    "createdAt" to Timestamp.now(),
                                    "updatedAt" to Timestamp.now()
                                )

                                firestore.collection("users").document(userId).set(userMap)
                                    .addOnSuccessListener {
                                        _authState.value = AuthState.Authenticated
                                    }
                                    .addOnFailureListener { e ->
                                        _authState.value = AuthState.Error(e.message ?: "Failed to save user data")
                                    }
                            } else {
                                _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                            }
                        }
                } else {
                    _authState.value = AuthState.Error("Username already exists")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Something went wrong")
            }
    }

    // Update username
    fun updateUsername(newUsername: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Cek apakah username baru sudah ada
            firestore.collection("users")
                .whereEqualTo("username", newUsername)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Username belum ada, update
                        firestore.collection("users").document(userId)
                            .update(
                                mapOf(
                                    "username" to newUsername,
                                    "updatedAt" to Timestamp.now()
                                )
                            )
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onError(e.message ?: "Failed to update username") }
                    } else {
                        onError("Username already exists")
                    }
                }
                .addOnFailureListener { e -> onError(e.message ?: "Something went wrong") }
        } else {
            onError("User not authenticated")
        }
    }

    // Update email
    fun updateEmail(newEmail: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        val userId = user?.uid

        if (user != null && userId != null) {
            user.updateEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update email di Firestore juga
                        firestore.collection("users").document(userId)
                            .update(
                                mapOf(
                                    "email" to newEmail,
                                    "updatedAt" to Timestamp.now()
                                )
                            )
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onError(e.message ?: "Failed to update email in database") }
                    } else {
                        onError(task.exception?.message ?: "Failed to update email")
                    }
                }
        } else {
            onError("User not authenticated")
        }
    }

    // Update password
    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser

        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update timestamp di Firestore
                        firestore.collection("users").document(user.uid)
                            .update("updatedAt", Timestamp.now())
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onError(e.message ?: "Password updated but failed to update timestamp") }
                    } else {
                        onError(task.exception?.message ?: "Failed to update password")
                    }
                }
        } else {
            onError("User not authenticated")
        }
    }

    // Update gender
    fun updateGender(gender: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "gender" to gender,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Failed to update gender") }
        } else {
            onError("User not authenticated")
        }
    }

    // Update date of birth
    fun updateDateOfBirth(dateOfBirth: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "dateOfBirth" to dateOfBirth,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Failed to update date of birth") }
        } else {
            onError("User not authenticated")
        }
    }

    // Get user data
    fun getUserData(onSuccess: (Map<String, Any>) -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        onSuccess(document.data ?: emptyMap())
                    } else {
                        onError("User data not found")
                    }
                }
                .addOnFailureListener { e -> onError(e.message ?: "Failed to get user data") }
        } else {
            onError("User not authenticated")
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onLogoutSuccess()
    }
}
