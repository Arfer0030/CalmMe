package com.example.calmme.pages.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            // Cek apakah email sudah diverifikasi
            if (user.isEmailVerified) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.EmailNotVerified
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    // Login dengan email atau username
    fun login(identifier: String, password: String) {
        _authState.value = AuthState.Loading

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            // Login dengan email
            auth.signInWithEmailAndPassword(identifier, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user?.isEmailVerified == true) {
                            _authState.value = AuthState.Authenticated
                        } else {
                            _authState.value = AuthState.EmailNotVerified
                        }
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                    }
                }
        } else {
            // Login dengan username
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
                                        val user = auth.currentUser
                                        if (user?.isEmailVerified == true) {
                                            _authState.value = AuthState.Authenticated
                                        } else {
                                            _authState.value = AuthState.EmailNotVerified
                                        }
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

    // cek dan update email saat login ketika kasus changeemail
    fun checkAndUpdateEmailOnLogin(onComplete: () -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val currentEmail = user.email
            if (currentEmail != null) {
                firestore.collection("users").document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val firestoreEmail = document.getString("email")
                            if (firestoreEmail != currentEmail) {
                                updateEmailInFirestore(
                                    userId = user.uid,
                                    newEmail = currentEmail,
                                    onSuccess = onComplete,
                                    onError = { onComplete() }
                                )
                            } else {
                                onComplete()
                            }
                        } else {
                            onComplete()
                        }
                    }
                    .addOnFailureListener {
                        onComplete()
                    }
            } else {
                onComplete()
            }
        } else {
            onComplete()
        }
    }


    // Signup dengan email verification
    fun signup(username: String, email: String, password: String) {
        _authState.value = AuthState.Loading

        // Cek username
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Username belum ada, lanjutkan signup
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                user?.sendEmailVerification()
                                    ?.addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            // Simpan data user di Firestore
                                            val userId = user.uid
                                            val userMap = hashMapOf(
                                                "username" to username,
                                                "email" to email,
                                                "gender" to "",
                                                "dateOfBirth" to "",
                                                "role" to "user",
                                                "subscriptionStatus" to "inactive",
                                                "subscriptionStartDate" to "",
                                                "subscriptionEndDate" to "",
                                                "emailVerified" to false,
                                                "createdAt" to Timestamp.now(),
                                                "updatedAt" to Timestamp.now()
                                            )

                                            firestore.collection("users").document(userId).set(userMap)
                                                .addOnSuccessListener {
                                                    _authState.value = AuthState.EmailVerificationSent
                                                }
                                                .addOnFailureListener { e ->
                                                    _authState.value = AuthState.Error(e.message ?: "Failed to save user data")
                                                }
                                        } else {
                                            _authState.value = AuthState.Error("Failed to send verification email")
                                        }
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

    // Buat resend verification email
    fun resendVerificationEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onError(task.exception?.message ?: "Failed to resend verification email")
                    }
                }
        } else {
            onError("No user logged in")
        }
    }

    // Buat cek status verifikasi email
    fun checkEmailVerificationStatus() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    firestore.collection("users").document(user.uid)
                        .update(
                            mapOf(
                                "emailVerified" to true,
                                "updatedAt" to Timestamp.now()
                            )
                        )
                        .addOnSuccessListener {
                            _authState.value = AuthState.Authenticated
                        }
                } else {
                    _authState.value = AuthState.EmailNotVerified
                }
            }
        }
    }

    // Buat update username
    fun updateUsername(newUsername: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .whereEqualTo("username", newUsername)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
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

    // Buat autentikasi ulang user saat ngubah pasword dan cahnge emaik
    fun reauthenticateUser(
        currentPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onError(task.exception?.message ?: "Re-authentication failed")
                    }
                }
        } else {
            onError("User not found")
        }
    }

    // Buat update password
    fun updatePasswordWithReauth(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        reauthenticateUser(
            currentPassword = currentPassword,
            onSuccess = {
                // Setelah re-auth berhasil, update password
                val user = auth.currentUser
                user?.updatePassword(newPassword)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Update timestamp di Firestore
                            firestore.collection("users").document(user.uid)
                                .update("updatedAt", Timestamp.now())
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener { e ->
                                    onError(e.message ?: "Password updated but failed to update timestamp")
                                }
                        } else {
                            onError(task.exception?.message ?: "Failed to update password")
                        }
                    }
            },
            onError = onError
        )
    }

    // Buat update gender
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

    // Buat update tangal lahir
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

    // Buat update email pake verifikasi
    fun updateEmailWithVerification(
        currentPassword: String,
        newEmail: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            // Validasi email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                onError("Invalid email format")
                return
            }

            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.verifyBeforeUpdateEmail(newEmail)
                            .addOnCompleteListener { verifyTask ->
                                if (verifyTask.isSuccessful) {
                                    onSuccess()
                                } else {
                                    val errorMessage = verifyTask.exception?.message ?: "Failed to send verification email"
                                    if (errorMessage.contains("email-already-in-use") ||
                                        errorMessage.contains("already in use")) {
                                        onError("Email is already in use")
                                    } else {
                                        onError(errorMessage)
                                    }
                                }
                            }
                    } else {
                        onError("Current password is incorrect")
                    }
                }
        } else {
            onError("User not found")
        }
    }

    // Buat update email di Firestore
    fun updateEmailInFirestore(
        userId: String,
        newEmail: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("users").document(userId)
            .update("email", newEmail)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to update email in Firestore")
            }
    }

    // Buat ngecek status email update
    fun checkEmailUpdateStatus(
        onEmailUpdated: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    val currentEmail = user.email
                    val userId = user.uid
                    if (currentEmail != null) {
                        onEmailUpdated(currentEmail, userId)
                    } else {
                        onError("Email not found")
                    }
                } else {
                    onError("Failed to reload user data")
                }
            }
        } else {
            onError("User not found - please sign in again")
        }
    }

    // Buat handle setelah email verification
    fun handleEmailVerificationComplete(
        newEmail: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful && user.isEmailVerified) {
                    updateEmailInFirestore(
                        userId = user.uid,
                        newEmail = newEmail,
                        onSuccess = {
                            logout {
                                onSuccess()
                            }
                        },
                        onError = onError
                    )
                } else {
                    onError("Email verification not completed yet")
                }
            }
        } else {
            onError("User session expired - please sign in again")
        }
    }

    // Buat ambil data user
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

    // Buat logout
    fun logout(onLogoutSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onLogoutSuccess()
    }
}
