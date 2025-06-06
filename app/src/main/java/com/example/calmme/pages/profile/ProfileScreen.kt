package com.example.calmme.pages.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.pages.authentication.AuthViewModel

val montserrat = FontFamily(Font(R.font.montserrat_bold))
val nunito = FontFamily(Font(R.font.nunito_regular))

@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    val navController = LocalNavController.current

    var username by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.getUserData(
            onSuccess = { userData ->
                username = userData["username"] as? String ?: "User"
                email = userData["email"] as? String ?: ""
                profilePictureUrl = userData["profilePicture"] as? String
                isLoading = false
            },
            onError = { error ->
                username = "User"
                isLoading = false
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
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(112.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                navController.navigate("home")
                            }
                    )
                    Text(
                        text = "Profile", style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(profilePictureUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(115.dp)
                        .clip(RoundedCornerShape(55.dp))
                        .clickable {
                            navController.navigate(Routes.Profile.route)
                        },
                    placeholder = painterResource(id = R.drawable.profile),
                    error = painterResource(id = R.drawable.profile),
                    fallback = painterResource(id = R.drawable.profile)
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF8E44AD),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = username,
                        fontFamily = montserrat,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            item {
                Text(
                    text = if (isLoading) "Loading..." else "available",
                    fontFamily = nunito,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFD1C4E9), Color(0xFFFFF9C4))
                            )
                        )
                        .clickable { navController.navigate(Routes.EditProfile.route) }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        fontFamily = nunito,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xE6DAD0F8))
                        .padding(24.dp)
                ) {
                    val menuItems = listOf(
                        "Security",
                        "Notifications",
                        "Payment",
                        "Help",
                        "About"
                    )

                    menuItems.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable {
                                    when (item.lowercase()) {
                                        "security" -> {
                                            navController.navigate(Routes.EditSecurity.route)
                                        }
                                        "notifications" -> {
                                            navController.navigate(Routes.History.route)
                                        }
                                        "payment" -> {
                                            navController.navigate(Routes.PaymentHistory.route)
                                        }
                                        "help" -> {
                                            navController.navigate(Routes.Help.route)
                                        }
                                        "about" -> {
                                            navController.navigate(Routes.About.route)
                                        }
                                    }
                                }
                        ) {
                            val iconRes = when (item.lowercase()) {
                                "security" -> R.drawable.security
                                "notifications" -> R.drawable.notifications
                                "payment" -> R.drawable.membership
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
                            .clickable {
                                authViewModel.logout {
                                    navController.navigate(Routes.Authentication.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
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
}