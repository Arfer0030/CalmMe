package com.example.calmme.pages.meditate

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calmme.R
import com.example.calmme.pages.authentication.AuthViewModel

@Composable
fun MeditateScreen(navController: NavController, authViewModel: AuthViewModel) {
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFF7E7F8), Color.White,Color(0xFFF7E7F8))
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )
            Text(
                text = "Meditate Time",
                style = MaterialTheme.typography.headlineSmall
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

        Image(
            painter = painterResource(id = R.drawable.meditate_header),
            contentDescription = "Meditate Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFF8AB4F8), RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Calm Picks",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val items = listOf(
            MeditateItems("Drift Into Dreams", R.drawable.dreams, "A soothing soundtrack to drift into peaceful sleep.", "02:09", R.raw.calm_dream),
            MeditateItems("Moonlight Calm", R.drawable.moonlight, "Gentle melodies under the moonlight.", "02:06", R.raw.moonlight_calm),
            MeditateItems("Whispers of the Forest", R.drawable.forest, "Nature sounds from a serene forest.", "02:12", R.raw.forest_whispers)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items) { item ->
                SoundCard(
                    title = item.name,
                    imageRes = item.image,
                    description = item.description,
                    duration = item.schedule,
                    onClick = {
                        navController.navigate("music/${item.audioResId}")
                    }
                )
            }
        }
    }
}

@Composable
fun SoundCard(
    title: String,
    imageRes: Int,
    description: String,
    duration: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2E7FB)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = duration,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

data class MeditateItems(
    val name: String,
    val image: Int,
    val description: String,
    val schedule: String,
    val audioResId: Int
)