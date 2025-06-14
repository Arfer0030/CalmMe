package com.example.calmme.pages.profile

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.pages.authentication.AuthViewModel

@Composable
fun EditSecurityScreen(authViewModel: AuthViewModel) {
    val navController = LocalNavController.current
    val context = LocalContext.current

    var currentEmail by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var updateMode by remember { mutableStateOf("email") }
    var showEmailVerificationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.getUserData(
            onSuccess = { userData ->
                currentEmail = userData["email"] as? String ?: ""
                newEmail = currentEmail
            },
            onError = { error ->
                Toast.makeText(context, "Failed to load user data: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF7E7F8), Color.White,Color(0xFFF7E7F8))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            navController.popBackStack()
                        }
                )
                Spacer(modifier = Modifier.width(85.dp))
                Text(
                    text = "Edit Security",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { updateMode = "email" },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (updateMode == "email") Color(0xFF8E44AD) else Color.DarkGray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Update Email",
                        color = if (updateMode == "email") Color(0xFF8E44AD) else Color.Black
                    )
                }
                OutlinedButton(
                    onClick = { updateMode = "password" },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (updateMode == "password") Color(0xFF8E44AD) else Color.DarkGray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Update Password",
                        color =if (updateMode == "password") Color(0xFF8E44AD) else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (updateMode == "email") {
                EmailUpdateSection(
                    currentEmail = currentEmail,
                    newEmail = newEmail,
                    onNewEmailChange = { newEmail = it },
                    currentPassword = currentPassword,
                    onCurrentPasswordChange = { currentPassword = it },
                    currentPasswordVisible = currentPasswordVisible,
                    onPasswordVisibilityToggle = { currentPasswordVisible = !currentPasswordVisible },
                    isLoading = isLoading,
                    onUpdateEmail = {
                        when {
                            newEmail.isBlank() -> {
                                Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                                return@EmailUpdateSection
                            }
                            newEmail == currentEmail -> {
                                Toast.makeText(context, "New email must be different from current email", Toast.LENGTH_SHORT).show()
                                return@EmailUpdateSection
                            }
                            currentPassword.isBlank() -> {
                                Toast.makeText(context, "Current password is required", Toast.LENGTH_SHORT).show()
                                return@EmailUpdateSection
                            }
                        }

                        isLoading = true
                        authViewModel.updateEmailWithVerification(
                            currentPassword = currentPassword,
                            newEmail = newEmail,
                            onSuccess = {
                                isLoading = false
                                showEmailVerificationDialog = true
                            },
                            onError = { error ->
                                isLoading = false
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            } else {
                PasswordUpdateSection(
                    currentPassword = currentPassword,
                    onCurrentPasswordChange = { currentPassword = it },
                    newPassword = newPassword,
                    onNewPasswordChange = { newPassword = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    currentPasswordVisible = currentPasswordVisible,
                    passwordVisible = passwordVisible,
                    confirmPasswordVisible = confirmPasswordVisible,
                    onCurrentPasswordVisibilityToggle = { currentPasswordVisible = !currentPasswordVisible },
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    onConfirmPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                    isLoading = isLoading,
                    onUpdatePassword = {
                        when {
                            currentPassword.isBlank() -> {
                                Toast.makeText(context, "Current password is required", Toast.LENGTH_SHORT).show()
                                return@PasswordUpdateSection
                            }
                            newPassword.isBlank() -> {
                                Toast.makeText(context, "New password cannot be empty", Toast.LENGTH_SHORT).show()
                                return@PasswordUpdateSection
                            }
                            newPassword.length < 6 -> {
                                Toast.makeText(context, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                return@PasswordUpdateSection
                            }
                            newPassword != confirmPassword -> {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@PasswordUpdateSection
                            }
                        }

                        isLoading = true
                        authViewModel.updatePasswordWithReauth(
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            onSuccess = {
                                isLoading = false
                                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onError = { error ->
                                isLoading = false
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            }
        }
    }

    if (showEmailVerificationDialog) {
        EmailVerificationDialog(
            newEmail = newEmail,
            authViewModel = authViewModel,
            onDismiss = {
                showEmailVerificationDialog = false
                authViewModel.logout {
                    Toast.makeText(context, "Please sign in with your new email", Toast.LENGTH_LONG).show()
                    navController.navigate(Routes.Authentication.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        )
    }
}

@Composable
fun EmailUpdateSection(
    currentEmail: String,
    newEmail: String,
    onNewEmailChange: (String) -> Unit,
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    currentPasswordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    isLoading: Boolean,
    onUpdateEmail: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = currentEmail,
            onValueChange = { },
            label = { Text("Current Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.LightGray,
                disabledLabelColor = Color.Gray,
                disabledTextColor = Color.Gray
            )
        )

        OutlinedTextField(
            value = newEmail,
            onValueChange = onNewEmailChange,
            label = { Text("New Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8E44AD),
                focusedLabelColor = Color(0xFF8E44AD)
            )
        )

        PasswordTextField(
            value = currentPassword,
            onValueChange = onCurrentPasswordChange,
            label = "Current Password (for verification)",
            isVisible = currentPasswordVisible,
            onVisibilityToggle = onPasswordVisibilityToggle
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "• Current password is required for security\n" +
                    "• A verification email will be sent to your new email\n" +
                    "• You must click the verification link to complete the change\n" +
                    "• You will be signed out after verification",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onUpdateEmail,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E44AD)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Send Verification Email", color = Color.White)
            }
        }
    }
}

@Composable
fun PasswordUpdateSection(
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    currentPasswordVisible: Boolean,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    onCurrentPasswordVisibilityToggle: () -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    isLoading: Boolean,
    onUpdatePassword: () -> Unit
) {
    Column {
        PasswordTextField(
            value = currentPassword,
            onValueChange = onCurrentPasswordChange,
            label = "Current Password",
            isVisible = currentPasswordVisible,
            onVisibilityToggle = onCurrentPasswordVisibilityToggle
        )

        PasswordTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = "New Password",
            isVisible = passwordVisible,
            onVisibilityToggle = onPasswordVisibilityToggle
        )

        PasswordTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            isVisible = confirmPasswordVisible,
            onVisibilityToggle = onConfirmPasswordVisibilityToggle
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "• Current password is required for security\n" +
                    "• New password must be at least 6 characters",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onUpdatePassword,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E44AD)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Update Password", color = Color.White)
            }
        }
    }
}

@Composable
fun EmailVerificationDialog(
    newEmail: String,
    onDismiss: () -> Unit,
    authViewModel: AuthViewModel
) {
    AlertDialog(
        onDismissRequest = {  },
        title = {
            Text(
                text = "Email Verification Sent",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("A verification link has been sent to:")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = newEmail,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E44AD)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Please check your email and click the verification link to complete the change.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You will be signed out now. Please sign in again with your new email after verification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E44AD))
            ) {
                Text("OK", color = Color.White)
            }
        }
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF8E44AD),
            focusedLabelColor = Color(0xFF8E44AD)
        ),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_eye),
                contentDescription = "Toggle password visibility",
                modifier = Modifier.clickable {
                    onVisibilityToggle()
                },
                tint = if (isVisible) Color.Black else Color.Gray
            )
        }
    )
}
