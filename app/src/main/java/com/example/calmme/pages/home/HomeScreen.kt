package com.example.calmme.pages.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.data.CategoryData
import com.example.calmme.data.categoryList
import com.example.calmme.data.moods
import com.example.calmme.pages.authentication.AuthState
import com.example.calmme.pages.authentication.AuthViewModel
import com.example.calmme.pages.dailymood.DailyMoodViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    dailyMoodViewModel: DailyMoodViewModel
) {
    val navController = LocalNavController.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Authenticated -> {
                authViewModel.checkAndUpdateEmailOnLogin {
                }
            }
            AuthState.Unauthenticated -> {
                navController.navigate(Routes.Authentication.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {
            }
        }
    }

    Box(
        modifier
            .background(
                Brush.linearGradient(
                    0.0f to Color(0xffe4dfc6),
                    0.3f to Color(0xffd3c6de),
                    0.5f to Color(0xffCEBFE6),
                    0.7f to Color(0xffc8ceec),
                    1.0f to Color(0xffc2ddf2),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 800f),
                )
            )
    ) {
        LazyColumn(
            modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(
                    user?.displayName ?: "Guest",
                    authViewModel = authViewModel,
                    dailyMoodViewModel = dailyMoodViewModel
                )
            }
            item { HomeForYou() }
        }
    }
}

@Composable
fun HomeHeader(username: String, modifier: Modifier = Modifier, authViewModel: AuthViewModel, dailyMoodViewModel: DailyMoodViewModel) {
    val navController = LocalNavController.current
    var userData by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var actualUsername by remember { mutableStateOf(username) }

    LaunchedEffect(Unit) {
        authViewModel.getUserData(
            onSuccess = { data ->
                userData = data
                actualUsername = data["username"] as? String ?: username
            },
            onError = {
            }
        )
    }

    Column {
        Spacer(modifier.padding(vertical = 14.dp))
        Row(
            modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    modifier.size(72.dp)
                        .clickable {
                            navController.navigate(Routes.Profile.route)
                        }
                )
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Text(getGreeting(), style = MaterialTheme.typography.bodyLarge)
                    Text("$actualUsername!", style = MaterialTheme.typography.titleLarge)
                }
            }
            IconButton(
                onClick = {
                    navController.navigate(Routes.History.route)
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notif),
                    contentDescription = "Notification",
                    modifier.size(26.dp)
                )
            }
        }
        HomeMood(dailyMoodViewModel = dailyMoodViewModel)
    }
}

@Composable
fun getGreeting(): String {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning,"
        in 12..17 -> "Good Afternoon,"
        else -> "Good Evening,"
    }
}

@Composable
fun HomeMood(modifier: Modifier = Modifier, dailyMoodViewModel: DailyMoodViewModel) {
    val selectedMood by dailyMoodViewModel.selectedMood.collectAsState()
    val isLoading by dailyMoodViewModel.isLoading.collectAsState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("how are you today?", style = MaterialTheme.typography.headlineMedium)

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = Color(0xff933C9F)
            )
        }

        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            items(moods) { mood ->
                MoodItem(
                    name = mood.first,
                    icon = mood.second,
                    isSelected = selectedMood == mood.first,
                    onMoodSelected = {
                        if (!isLoading) {
                            dailyMoodViewModel.selectMood(mood.first)
                        }
                    }
                )
            }
        }

        selectedMood?.let { mood ->
            Text(
                text = "Today's mood: $mood",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xff933C9F),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MoodItem(
    modifier: Modifier = Modifier,
    name: String,
    icon: Int,
    isSelected: Boolean = false,
    onMoodSelected: () -> Unit
) {
    Column(
        modifier = modifier
            .width(90.dp)
            .padding(vertical = 8.dp)
            .clickable { onMoodSelected() }
            .background(
                if (isSelected) Color(0xff933C9F).copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = name,
            modifier = Modifier.size(90.dp),
            alpha = if (isSelected) 1f else 0.7f
        )
        Text(
            name,
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) Color(0xff933C9F) else Color(0xff933C9F).copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun HomeForYou(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current
    Column(
        modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        Row(
            modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "For You",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black
            )
            Text(
                text = "See more",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF933C9F),
                modifier = Modifier.clickable {
                    navController.navigate(Routes.Subscribe.route)
                }
            )
        }

        Spacer(modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate(Routes.Subscribe.route)
                }
        ) {
            Box(
                modifier
                    .background(
                        Brush.linearGradient(
                            0.0f to Color(0xFFdaf0d2),
                            0.4f to Color(0xFFc0e2f4),
                            0.5f to Color(0xFFc0e2f4),
                            0.7f to Color(0xFFc0e2f4),
                            1.0f to Color(0xFFcec2e8),
                            start = Offset(0f, 600f),
                            end = Offset(1000f, 100f),
                        )
                    )
            ) {
                Row(
                    modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "GO PREMIUM 👑",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier.height(14.dp))
                        Text(
                            text = "Upgrade to premium\nget more profit\nnow!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xff863D3D),
                        )
                        Spacer(modifier.height(16.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow),
                            contentDescription = "Notification",
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.foryou_1),
                            contentDescription = "Premium",
                            modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.foryou_2),
                            contentDescription = "Premium",
                            modifier.size(67.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier.height(16.dp))
        HomeCategory()
    }
}



@Composable
fun HomeCategory(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Row(
            modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black
            )
            Text(
                text = "See all",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF933C9F),
                modifier = modifier.clickable {

                },
            )
        }

        Spacer(modifier.height(12.dp))

        LazyVerticalStaggeredGrid (
            columns = StaggeredGridCells.Fixed(2),
            modifier.height(350.dp),
            contentPadding = PaddingValues(4.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(categoryList) { index, category ->
                if (index % 2 == 0 ) {
                    HomeCategoryItem(category = category, isLarge = true)
                } else {
                    HomeCategoryItem(category = category, isLarge = false)
                }
            }
        }
    }
}


@Composable
fun HomeCategoryItem(modifier: Modifier = Modifier, category: CategoryData, isLarge: Boolean) {
    val navController = LocalNavController.current
    Card(
        modifier
            .fillMaxWidth()
            .height(if (isLarge) 180.dp else 150.dp)
            .clickable {
                navController.navigate(category.route)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            category.color1,
                            category.color2
                        )
                    )
                )
        ) {
            Column(
                modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Image(
                    painter = painterResource(id = category.icon),
                    contentDescription = category.name,
                    modifier.size(75.dp).align(Alignment.End)
                )
            }
        }
    }
}