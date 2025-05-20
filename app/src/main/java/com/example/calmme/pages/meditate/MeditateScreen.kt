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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.dp
import com.example.calmme.R

@Composable
fun MeditateScreen() {
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
            MeditateItems(
                name = "Drift Into Dreams",
                image = R.drawable.dreams,
                description = "A soothing soundtrack to drift into peaceful sleep.",
                schedule = "1:23",
                audioResId = R.raw.calm_dream
            ),
            MeditateItems(
                name = "Moonlight Calm",
                image = R.drawable.moonlight,
                description = "Gentle melodies under the moonlight.",
                schedule = "0:58",
                audioResId = R.raw.moonlight_calm
            ),
            MeditateItems(
                name = "Whispers of the Forest",
                image = R.drawable.forest,
                description = "Nature sounds from a serene forest.",
                schedule = "1:07",
                audioResId = R.raw.forest_whispers
            )
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
                    audioResId = item.audioResId
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
    audioResId: Int
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 8.dp)
                    .clickable {
                        if (!isPlaying) {
                            mediaPlayer = MediaPlayer.create(context, audioResId)
                            mediaPlayer?.start()
                            isPlaying = true
                            mediaPlayer?.setOnCompletionListener {
                                isPlaying = false
                                it.release()
                                mediaPlayer = null
                            }
                        } else {
                            mediaPlayer?.pause()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPlaying = false
                        }
                    }
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
