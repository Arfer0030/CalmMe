package com.example.calmme.pages.authentication

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onSwitchToLogin: () -> Unit,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val navController = LocalNavController.current
    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Authentication.route) { inclusive = true }
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("LET'S GET STARTED!", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = username,
            onValueChange = { username = it },
            hint = "Username",
            icon = R.drawable.ic_person
        )

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            hint = "Email",
            icon = R.drawable.ic_mail
        )

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            hint = "Password",
            icon = R.drawable.ic_key,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthButton(text = "Sign Up", route = Routes.Home.route, authViewModel = authViewModel) {
            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                authViewModel.signup(username, email, password)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        AuthBottomText("Already have an account?", "Log In", onTabSwitch = onSwitchToLogin)
    }
}
