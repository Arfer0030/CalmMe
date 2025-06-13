package com.example.calmme.pages.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController

@Composable
fun HelpScreen(helpViewModel: HelpViewModel = viewModel()) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val userName by helpViewModel.userName.collectAsState()
    val userEmail by helpViewModel.userEmail.collectAsState()

    val helpOptions = listOf(
        Triple(R.drawable.ic_email, "Email", "Contact Us - CalmMe App"),
        Triple(R.drawable.ic_feedback, "Send Feedback", "Feedback - CalmMe App"),
        Triple(R.drawable.ic_flag, "Report", "Report Issue - CalmMe App")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF7E7F8), Color.White,Color(0xFFF7E7F8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        .clickable { navController.popBackStack() }
                )

                Spacer(modifier = Modifier.width(120.dp))

                Text(
                    text = "Help",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.p_help),
                contentDescription = "Help",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Get help from the official\nCalmMe support team",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF6C3483),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD6C6E1), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                helpOptions.forEach { (icon, text, subject) ->
                    HelpOption(icon = icon, text = text, subject = subject) {
                        helpViewModel.openHelpEmail(context, subject, userName, userEmail)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun HelpOption(icon: Int, text: String, subject: String, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }.fillMaxWidth().clip(RoundedCornerShape(4.dp))
    ) {
        Row {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                tint = Color(0xFF8E44AD),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = 16.sp, color = Color(0xFF6C3483))
        }
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = text,
            tint = Color(0xFF8E44AD),
            modifier = Modifier.size(24.dp).graphicsLayer {
                scaleX = -1f
            }
        )
    }
}