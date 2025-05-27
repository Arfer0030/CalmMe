package com.example.calmme.pages.profile

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.pages.authentication.AuthViewModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun EditProfileScreen(authViewModel: AuthViewModel) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var dateOfBirth by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Load user data saat screen pertama kali dibuka
    LaunchedEffect(Unit) {
        authViewModel.getUserData(
            onSuccess = { userData ->
                username = userData["username"] as? String ?: ""
                email = userData["email"] as? String ?: ""
                gender = userData["gender"] as? String ?: "male"
                dateOfBirth = userData["dateOfBirth"] as? String ?: ""
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
                    listOf(Color(0xFFF3E7FE), Color(0xFFF7F2F9))
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
                Spacer(modifier = Modifier.width(96.dp))
                Text(
                    text = "Edit Profile",
                    fontFamily = montserrat,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(32.dp))

            textField("Username", username) { username = it }
            textField("Email", email) { email = it }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Gender", fontFamily = nunito, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GenderOption(
                    icon = R.drawable.male,
                    selected = gender == "male",
                    label = "Male",
                    onClick = { gender = "male" },
                    modifier = Modifier.weight(1f)
                )
                GenderOption(
                    icon = R.drawable.female,
                    selected = gender == "female",
                    label = "Female",
                    onClick = { gender = "female" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Date of Birth", fontFamily = nunito, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))

            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { },
                placeholder = { Text("dd/mm/yyyy", fontFamily = nunito) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.date),
                        contentDescription = "Calendar Icon",
                        modifier = Modifier.clickable {
                            showPurpleDatePicker(
                                context = context,
                                currentDate = dateOfBirth,
                                onDateSelected = { selectedDate ->
                                    dateOfBirth = selectedDate
                                }
                            )
                        }
                    )
                },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isBlank() || email.isBlank()) {
                        Toast.makeText(context, "Username and Email cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        // Update username
                        authViewModel.updateUsername(
                            newUsername = username,
                            onSuccess = {
                                // Update email
                                authViewModel.updateEmail(
                                    newEmail = email,
                                    onSuccess = {
                                        // Update gender
                                        authViewModel.updateGender(
                                            gender = gender,
                                            onSuccess = {
                                                // Update date of birth
                                                authViewModel.updateDateOfBirth(
                                                    dateOfBirth = dateOfBirth,
                                                    onSuccess = {
                                                        isLoading = false
                                                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                                        navController.popBackStack()
                                                    },
                                                    onError = { error ->
                                                        isLoading = false
                                                        Toast.makeText(context, "Failed to update date of birth: $error", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            },
                                            onError = { error ->
                                                isLoading = false
                                                Toast.makeText(context, "Failed to update gender: $error", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        Toast.makeText(context, "Failed to update email: $error", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onError = { error ->
                                isLoading = false
                                Toast.makeText(context, "Failed to update username: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
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
                    Text("Save", fontFamily = nunito, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Fungsi untuk menampilkan DatePicker dengan warna ungu
fun showPurpleDatePicker(
    context: android.content.Context,
    currentDate: String,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()

    // Parse tanggal saat ini jika ada
    if (currentDate.isNotEmpty()) {
        try {
            val parts = currentDate.split("/")
            if (parts.size == 3) {
                calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
        } catch (e: Exception) {
            // Gunakan tanggal saat ini jika parsing gagal
        }
    }

    val datePicker = DatePickerDialog(
        context,
        R.style.PurpleDatePickerTheme, // Custom theme untuk warna ungu
        { _: DatePicker, year: Int, month: Int, day: Int ->
            val selectedDate = "%02d/%02d/%d".format(day, month + 1, year)
            onDateSelected(selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Set warna accent untuk DatePicker
    datePicker.datePicker.setBackgroundColor(Color(0xFFF3E7FE).toArgb())
    datePicker.show()
}

@Composable
fun textField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = nunito) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF8E44AD),
            focusedLabelColor = Color(0xFF8E44AD)
        )
    )
}

@Composable
fun GenderOption(
    icon: Int,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) Color(0xFFF3D9F9) else Color.White

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(40.dp)
        )
        Text(label, fontFamily = nunito, fontSize = 14.sp)
    }
}
