package com.example.calmme.pages.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController

@Composable
fun AuthScreen(authViewModel: AuthViewModel) {
    var isLogin by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFe0c6e1), Color(0xFFfdfbfe), Color(0xFFf7e9f8))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            Text(
                text = "CalmMe",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black
            )

            AuthTabSwitcher(
                isLogin = isLogin,
                onTabSelected = { isLogin = it }
            )

            if (isLogin) {
                LoginScreen(onSwitchToRegister = { isLogin = false }, authViewModel = authViewModel)
            } else {
                RegisterScreen(onSwitchToLogin = { isLogin = true }, authViewModel = authViewModel)
            }
        }
    }
}

@Composable
fun AuthTabSwitcher(isLogin: Boolean, onTabSelected: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFFB68AD6))
            .padding(horizontal = 0.dp)
            .width(350.dp)
            .height(40.dp)
    ) {
        Button(
            onClick = { onTabSelected(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLogin) Color.White else Color(0xFFB68AD6)
            ),
            modifier = Modifier.weight(1f),
            border = BorderStroke(1.dp, if (isLogin) Color(0xFFB68AD6) else Color.Transparent),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text("Log In",fontWeight = FontWeight.Bold, color = if (isLogin) Color(0xFFB68AD6) else Color.White)
        }

        Button(
            onClick = { onTabSelected(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isLogin) Color.White else Color(0xFFB68AD6)
            ),
            modifier = Modifier.weight(1f),
            border = BorderStroke(1.dp, if (!isLogin) Color(0xFFB68AD6) else Color.Transparent),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text("Register", fontWeight = FontWeight.Bold,color = if (!isLogin) Color(0xFFB68AD6) else Color.White)
        }
    }
}

@Composable
fun AuthButton(text: String, route: String, authViewModel: AuthViewModel, onClick: () -> Unit) {
    val navController = LocalNavController.current
    val authState = authViewModel.authState.observeAsState()

    Button(
        enabled = authState.value != AuthState.Loading,
        onClick = onClick,
        modifier = Modifier
            .height(43.dp)
            .width(240.dp)
            .padding(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB68AD6))
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    icon: Int,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
            )
        },
        trailingIcon = if (isPassword) {
            {
                Icon(
                    painter = painterResource(id = R.drawable.ic_eye),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        passwordVisible = !passwordVisible
                    },
                    tint = if (passwordVisible) Color.Black else Color.Gray
                )
            }
        } else null,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = Color(0xFFB68AD6)
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None
    )
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable
fun AuthBottomText(text: String, textbutton:String, onTabSwitch: () -> Unit){
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text)
        TextButton(
            modifier = Modifier.padding(start = 0.dp),
            onClick = {
                onTabSwitch()
            }
        )
        {
            Text(text = textbutton, color = Color(0xFFB68AD6))
        }
    }
}

