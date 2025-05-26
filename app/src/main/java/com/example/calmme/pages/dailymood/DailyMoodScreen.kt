package com.example.calmme.pages.dailymood

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import kotlinx.serialization.Serializable

@Composable
fun DailyMoodScreen(dailyMoodViewModel: DailyMoodViewModel) {
    val moodHistory by dailyMoodViewModel.moodHistory.collectAsState()
    val selectedPeriod = remember { mutableStateOf("Week") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF5EFFF), Color(0xFFF9F2FF))))
            .padding(16.dp)
    ) {
        TopBar()
        Spacer(modifier = Modifier.height(16.dp))
        MoodStreakSection(dailyMoodViewModel = dailyMoodViewModel)
        Spacer(modifier = Modifier.height(24.dp))
        MoodChartSection(
            dailyMoodViewModel = dailyMoodViewModel,
            selectedPeriod = selectedPeriod.value,
            onPeriodChanged = { selectedPeriod.value = it }
        )
        Spacer(modifier = Modifier.height(32.dp))
        MotivationCard()
    }
}


@Composable
fun TopBar() {
    val navController = LocalNavController.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Back",
            modifier = Modifier.clickable {
                navController.popBackStack()
            }
        )
        Text("Daily Mood Tracker", style = MaterialTheme.typography.headlineSmall)
        Icon(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Profile",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun MoodStreakSection(dailyMoodViewModel: DailyMoodViewModel) {
    val streak = dailyMoodViewModel.calculateStreak()
    val recentMoods = dailyMoodViewModel.getRecentMoods(7) // Last 7 days

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mood", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = streak.toString(),
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(" Day Streak", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentMoods) { moodData ->
                MoodCard(
                    day = moodData.first,
                    mood = moodData.second,
                    moodIcon = dailyMoodViewModel.getMoodIcon(moodData.second)
                )
            }
        }
    }
}

@Composable
fun MoodCard(day: String, mood: String, moodIcon: Int) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .background(Color(0xFFD9C2FF), shape = RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Mood Icon
        Icon(
            painter = painterResource(id = moodIcon),
            contentDescription = mood,
            modifier = Modifier.size(40.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mood,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun MoodChartSection(
    dailyMoodViewModel: DailyMoodViewModel, // Ubah parameter
    selectedPeriod: String,
    onPeriodChanged: (String) -> Unit
) {
    Column {
        Text("Mood Chart", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // Period Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Week", "Month").forEach { period ->
                Button(
                    onClick = { onPeriodChanged(period) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPeriod == period) Color(0xFFB9A6FF) else Color(0xFFEFE0FF)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = period,
                        color = if (selectedPeriod == period) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("${selectedPeriod}ly Average", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFEFE0FF), shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            // Gunakan fungsi dari ViewModel
            val moodPercentages = dailyMoodViewModel.calculateMoodPercentages(selectedPeriod)

            Column {
                // Percentage labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("100%", fontSize = 10.sp)
                    Text("50%", fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    moodPercentages.forEach { (mood, percentage) ->
                        MoodBar(
                            heightFraction = percentage / 100f,
                            mood = mood,
                            percentage = percentage,
                            dailyMoodViewModel = dailyMoodViewModel // Pass ViewModel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mood icons at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moodPercentages.forEach { (mood, _) ->
                        Icon(
                            painter = painterResource(id = dailyMoodViewModel.getMoodIcon(mood)), // Gunakan ViewModel
                            contentDescription = mood,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val dateRange = dailyMoodViewModel.getDateRange(selectedPeriod) // Gunakan ViewModel
        Text(
            text = dateRange,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Gray
        )
    }
}


@Composable
fun MoodBar(
    heightFraction: Float,
    mood: String,
    percentage: Float,
    dailyMoodViewModel: DailyMoodViewModel // Tambah parameter
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${percentage.toInt()}%",
            fontSize = 8.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(heightFraction.coerceAtLeast(0.05f)) // Minimum height
                .background(
                    dailyMoodViewModel.getMoodColor(mood), // Gunakan ViewModel
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )
    }
}


@Composable
fun MotivationCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDE3FF), shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "IT'S OKAY IF YOU.....",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                "Don't know what to do next",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}


@Serializable
data class MoodEntry(
    val mood: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)