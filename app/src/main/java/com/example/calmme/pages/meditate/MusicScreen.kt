package com.example.calmme.pages.meditate

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.calmme.R

data class MusicData(
    val title: String,
    val imageRes: Int,
    val audioRes: Int,
    val durationMs: Int,
    val durationStr: String
)

val musicList = listOf(
    MusicData("Drift Into Dreams", R.drawable.dreams, R.raw.calm_dream, 129000, "02:09"),
    MusicData("Whispers of the Forest", R.drawable.forest, R.raw.forest_whispers, 132000, "02:12"),
    MusicData("Moonlight Calm", R.drawable.moonlight, R.raw.moonlight_calm, 126000, "02:06")
)

@Composable
fun MusicScreen(
    navController: NavController,
    initialAudioRes: Int
) {
    val context = LocalContext.current
    val initialIndex = remember {
        musicList.indexOfFirst { it.audioRes == initialAudioRes }.coerceAtLeast(0)
    }
    var currentIndex by remember { mutableStateOf(initialIndex) }

    val currentMusic = musicList[currentIndex]

    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(currentMusic.durationMs) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(currentMusic.audioRes) {
        mediaPlayer?.release()
        val player = MediaPlayer.create(context, currentMusic.audioRes)
        player.setOnPreparedListener {
            duration = player.duration
            if (isPlaying) player.start()
        }
        player.setOnCompletionListener {
            isPlaying = false
            currentPosition = duration
        }
        mediaPlayer = player
        isPlaying = true
        currentPosition = 0

        onDispose {
            player.stop()
            player.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying) {
        mediaPlayer?.let { player ->
            if (isPlaying && !player.isPlaying) player.start()
            else if (!isPlaying && player.isPlaying) player.pause()
        }
    }

    LaunchedEffect(isPlaying, mediaPlayer) {
        while (isPlaying && mediaPlayer != null) {
            currentPosition = mediaPlayer?.currentPosition ?: 0
            delay(300L)
            if (currentPosition >= duration) break
        }
    }

    BackHandler {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFF4EAF9), Color(0xFFFBE9F2))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            navController.popBackStack()
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(3f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = currentMusic.imageRes),
                contentDescription = currentMusic.title,
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFFF4EAF9),
                        CircleShape
                    )
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = currentMusic.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = {
                        currentPosition = it.toInt()
                        mediaPlayer?.seekTo(currentPosition)
                    },
                    valueRange = 0f..duration.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFB388F8),
                        activeTrackColor = Color(0xFFB388F8)
                    )
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMillis(currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = currentMusic.durationStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val prev = if (currentIndex == 0) musicList.lastIndex else currentIndex - 1
                        currentIndex = prev
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color(0xFFE3CFFF),
                            CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.prev_icon),
                        contentDescription = "Previous",
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Color(0xFFB388F8),
                            CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPlaying) R.drawable.pause_icon else R.drawable.play_icon
                        ),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                IconButton(
                    onClick = {
                        val next = if (currentIndex == musicList.lastIndex) 0 else currentIndex + 1
                        currentIndex = next
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color(0xFFE3CFFF),
                            CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.next_icon),
                        contentDescription = "Next",
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

fun formatMillis(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}