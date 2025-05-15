package com.example.calmme.pages.authentication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmme.R

@Composable
fun RegisterScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("LET'S GET STARTED!", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(hint = "Username", icon = R.drawable.ic_person)
        AuthTextField(hint = "Email", icon = R.drawable.ic_mail)
        AuthTextField(hint = "Password", icon = R.drawable.ic_key, isPassword = true)

        Spacer(modifier = Modifier.height(16.dp))

        AuthButton(text = "Sign Up")

        Spacer(modifier = Modifier.height(140.dp))

        AuthBottomText("Already have an account?","Log In")
    }
}