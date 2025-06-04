package com.example.calmme.pages.subscribe

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.pages.authentication.AuthViewModel
import kotlinx.coroutines.launch

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val price: String,
    val description: String,
    val color1: Color,
    val color2: Color
)

@Composable
fun SubscribeScreen(viewModel: SubscribeViewModel, authViewModel: AuthViewModel) {
    val navController = LocalNavController.current
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPlan by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val plans = listOf(
        SubscriptionPlan(
            id = "consultation",
            title = "BASIC",
            price = "Rp 50.000,00 /consultation",
            description = "1x consultation access",
            color1 = Color(0xFFCEBFE6),
            color2 = Color(0xffB8F7FD)
        ),
        SubscriptionPlan(
            id = "subscription",
            title = "PLUS",
            price = "Rp 275.000,00 /month",
            description = "Get 1 month of unlimited consultation access",
            color1 = Color(0xFFFBFFA8),
            color2 = Color(0xffB8F7FD)
        )
    )

    LaunchedEffect(Unit) {
        authViewModel.getProfilePictureUrl(
            onSuccess = { url ->
                profilePictureUrl = url
            },
            onError = { error ->
                Log.e("TopBar", "Failed to load profile picture: $error")
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFe0c6e1), Color(0xFFfdfbfe), Color(0xFFf7e9f8))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() }
                )
                Text(
                    text = "Subscribe",
                    style = MaterialTheme.typography.headlineSmall,
                )
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(profilePictureUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.profile),
                    error = painterResource(id = R.drawable.profile),
                    fallback = painterResource(id = R.drawable.profile),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            ProgressIndicator(currentStep = 1, totalSteps = 3)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Unlock monthly or yearly consultations and join the CalmMe community by subscribing now!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            plans.forEach { plan ->
                SubscriptionPlanCard(
                    plan = plan,
                    isSelected = selectedPlan == plan.id,
                    onSelect = { selectedPlan = plan.id }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.selectPlan(selectedPlan)
                    coroutineScope.launch {
                        viewModel.processPayment(
                            onSuccess = {
                                Toast.makeText(context, "Subscription created successfully!", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.Payment.route)
                            },
                            onError = { error ->
                                Toast.makeText(context, "Failed to create subscription: $error", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8E44AD)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "BUY NOW",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .background(
                brush = Brush.linearGradient(listOf(plan.color1, plan.color2))
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF8E44AD) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E44AD)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_crown),
                        contentDescription = "crown",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = plan.price,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = plan.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProgressIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index < currentStep
            val isCompleted = index < currentStep - 1

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (isActive) Color(0xFF8E44AD) else Color.LightGray,
                        shape = RoundedCornerShape(6.dp)
                    )
            )
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(
                            color = if (isCompleted) Color(0xFF8E44AD) else Color.LightGray
                        )
                )
            }
        }
    }
}