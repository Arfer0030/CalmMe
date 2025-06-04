package com.example.calmme.pages.dailymood

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.pages.authentication.AuthViewModel
import kotlinx.serialization.Serializable

@Composable
fun DailyMoodScreen(dailyMoodViewModel: DailyMoodViewModel, authViewModel: AuthViewModel) {
    val selectedPeriod = remember { mutableStateOf("Week") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFe0c6e1), Color(0xFFfdfbfe), Color(0xFFf7e9f8))
                )
            )
            .padding(14.dp)
    ) {
        TopBar(authViewModel)
        Spacer(modifier = Modifier.height(20.dp))
        MoodStreakSection(dailyMoodViewModel = dailyMoodViewModel)
        Spacer(modifier = Modifier.height(26.dp))
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
fun TopBar(authViewModel: AuthViewModel) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }

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

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
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
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MoodStreakSection(dailyMoodViewModel: DailyMoodViewModel) {
    val streak = dailyMoodViewModel.calculateStreakFromHistory(dailyMoodViewModel.moodHistory.value)
    val recentMoods = dailyMoodViewModel.getRecentMoods(7)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mood", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = streak.toString(),
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(" Day Streak", style = MaterialTheme.typography.headlineSmall)
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
            .background(
                if (mood == "empty") Color(0xFFF5F5F5) else Color(0xFFD9C2FF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (mood == "empty") {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE0E0E0), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    fontSize = 20.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Icon(
                painter = painterResource(id = moodIcon),
                contentDescription = mood,
                modifier = Modifier.size(40.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (mood == "empty") "Empty" else mood,
            style = MaterialTheme.typography.bodyLarge,
            color = if (mood == "empty") Color.Gray else Color.Black
        )
    }
}


@Composable
fun MoodChartSection(
    dailyMoodViewModel: DailyMoodViewModel,
    selectedPeriod: String,
    onPeriodChanged: (String) -> Unit
) {
    Column {
        Text("Mood Chart", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

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
                        style = MaterialTheme.typography.titleLarge,
                        color = if (selectedPeriod == period) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        val (fillCount, totalDays) = dailyMoodViewModel.calculateMoodFillCounts(selectedPeriod)
        Text(
            text = "${selectedPeriod}ly Average ($fillCount of $totalDays days filled)",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFEFE0FF), shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            val moodPercentages = dailyMoodViewModel.calculateMoodPercentages(selectedPeriod)

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$fillCount of $totalDays",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF933C9F)
                    )
                    Text(
                        text = "${(fillCount.toFloat() / totalDays * 100).toInt()}% filled",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moodPercentages.forEach { (mood, _) ->
                        Icon(
                            painter = painterResource(id = dailyMoodViewModel.getMoodIcon(mood)),
                            contentDescription = mood,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val dateRange = dailyMoodViewModel.getDateRange(selectedPeriod)
        Text(
            text = dateRange,
            style = MaterialTheme.typography.titleSmall,
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
    dailyMoodViewModel: DailyMoodViewModel
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
                .fillMaxHeight(heightFraction.coerceAtLeast(0.05f))
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
    val timestamp: Long
)