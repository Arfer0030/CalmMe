package com.example.calmme.pages.meditate

import android.media.MediaPlayer
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.calmme.R

@Composable
fun MeditateScreen() {
    val context = LocalContext.current
    var activeTrackId by remember { mutableStateOf<Int?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF4EAF9), Color(0xFFFBE9F2))
                )
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Meditate Time",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

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
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )


        val items = listOf(
            MeditateItems("Drift Into Dreams", R.drawable.dreams, "A soothing soundtrack to drift into peaceful sleep.", "1:23", R.raw.calm_dream),
            MeditateItems("Moonlight Calm", R.drawable.moonlight, "Gentle melodies under the moonlight.", "0:58", R.raw.moonlight_calm),
            MeditateItems("Whispers of the Forest", R.drawable.forest, "Nature sounds from a serene forest.", "1:07", R.raw.forest_whispers)
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
                    audioResId = item.audioResId,
                    isActive = activeTrackId == item.audioResId,
                    onPlayPause = {
                        if (activeTrackId == item.audioResId) {
                            // Pause current track
                            mediaPlayer?.pause()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            activeTrackId = null
                        } else {
                            // Stop current playing track
                            mediaPlayer?.stop()
                            mediaPlayer?.release()

                            // Start new track
                            val newPlayer = MediaPlayer.create(context, item.audioResId)
                            newPlayer.start()
                            mediaPlayer = newPlayer
                            activeTrackId = item.audioResId

                            // Auto reset when done
                            newPlayer.setOnCompletionListener {
                                activeTrackId = null
                                mediaPlayer?.release()
                                mediaPlayer = null
                            }
                        }
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
    audioResId: Int,
    isActive: Boolean,
    onPlayPause: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
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
            Icon(
                painter = if (isActive)
                    painterResource(id = R.drawable.pause_icon) // pakai icon sendiri
                else
                    painterResource(id = R.drawable.play_icon), // pakai icon sendiri
                contentDescription = if (isActive) "Pause" else "Play",
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 8.dp)
                    .clickable { onPlayPause() }
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
