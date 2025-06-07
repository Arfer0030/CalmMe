package com.example.calmme.pages.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController

@Composable
fun AboutScreen() {
    val navController = LocalNavController.current
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
                    text = "About",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "CalmMe Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "About",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF8E44AD)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CalmMe is a mental health app that provides a safe space to share stories, " +
                        "track moods, consult with psychologists, " +
                        "and makes mental well-being more accessible for everyone.",
                fontSize = 14.sp,
                color = Color(0xFF6C3483),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text("Recognition", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_crown),
                        contentDescription = null,
                        tint = Color(0xFFF7CA18),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trusted by 1000+ Users", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Focus Topics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("• Anxiety", fontSize = 13.sp)
                    Text("• Focus & Productivity", fontSize = 13.sp)
                    Text("• Sleep Improvement", fontSize = 13.sp)
                    Text("• Self-Esteem", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Features", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("• Meditate Time", fontSize = 13.sp)
                    Text("• Daily Mood Tracker", fontSize = 13.sp)
                    Text("• Self-Assessment", fontSize = 13.sp)
                    Text("• Consultation", fontSize = 13.sp)
                }
            }
        }
    }
}
