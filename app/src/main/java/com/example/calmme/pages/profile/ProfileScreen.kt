package com.example.calmme.pages.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.calmme.R

val montserrat = FontFamily(Font(R.font.montserrat_bold))
val nunito = FontFamily(Font(R.font.nunito_regular))

@Composable
fun ProfileScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E7FE),
                        Color(0xFFF7F2F9)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                modifier = Modifier
                    .align(Alignment.Start)
                    .size(24.dp)
                    .clickable {
                        navController.navigate("home")
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray, CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "user123",
                fontFamily = montserrat,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "available",
                fontFamily = nunito,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFD1C4E9), Color(0xFFFFF9C4))
                        )
                    )
                    .clickable { }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Edit Profile",
                    fontFamily = nunito,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE6DAD0F8))
                    .padding(24.dp)
            ) {
                val menuItems = listOf(
                    "Privacy",
                    "Security",
                    "Notifications",
                    "Membership",
                    "Help",
                    "About"
                )

                menuItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable { }
                    ) {
                        val iconRes = when (item.lowercase()) {
                            "privacy" -> R.drawable.privacy
                            "security" -> R.drawable.security
                            "notifications" -> R.drawable.notifications
                            "membership" -> R.drawable.membership
                            "help" -> R.drawable.help
                            "about" -> R.drawable.about
                            else -> android.R.drawable.ic_menu_help
                        }

                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = "$item Icon",
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 20.dp)
                        )

                        Text(
                            text = item,
                            fontFamily = nunito,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logout),
                        contentDescription = "Logout Icon",
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 20.dp)
                    )

                    Text(
                        text = "Logout",
                        fontFamily = nunito,
                        fontSize = 16.sp,
                        color = Color.Red
                    )
                }
            }
        }
    }
}
