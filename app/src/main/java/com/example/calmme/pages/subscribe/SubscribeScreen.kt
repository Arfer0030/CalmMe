package com.example.calmme.pages.subscribe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val price: String,
    val description: String,
    val color1: Color,
    val color2: Color
)

@Composable
fun SubscribeScreen(viewModel: SubscribeViewModel) {
    val navController = LocalNavController.current
    var selectedPlan by remember { mutableStateOf("basic") }

    val plans = listOf(
        SubscriptionPlan(
            id = "basic",
            title = "BASIC",
            price = "Rp 50.000,00 /consultation",
            description = "1x consultation access",
            color1 = Color(0xFFCEBFE6),
            color2 = Color(0xffB8F7FD)
        ),
        SubscriptionPlan(
            id = "plus",
            title = "PLUS",
            price = "Rp 275.000,00 /month",
            description = "Get 1 month of unlimited consultation access",
            color1 = Color(0xFFFBFFA8),
            color2 = Color(0xffB8F7FD)
        )
    )

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
            // Header
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
                Icon(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Progress Indicator
            ProgressIndicator(currentStep = 1, totalSteps = 3)

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Choose",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Unlock monthly or yearly consultations and join the CalmMe community by subscribing now!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Subscription Plans
            plans.forEach { plan ->
                SubscriptionPlanCard(
                    plan = plan,
                    isSelected = selectedPlan == plan.id,
                    onSelect = { selectedPlan = plan.id }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buy Now Button
            Button(
                onClick = {
                    viewModel.selectPlan(selectedPlan)
                    navController.navigate(Routes.Payment.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8E44AD)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
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
