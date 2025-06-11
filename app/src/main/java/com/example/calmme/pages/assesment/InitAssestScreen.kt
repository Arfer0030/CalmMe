package com.example.calmme.pages.assesment

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.pages.authentication.AuthViewModel

@Composable
fun InitAssestScreen(authViewModel: AuthViewModel){
    val navController = LocalNavController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFbcf7f8), Color(0xffd5fad8), Color(0xffC4D9F1))
                )
            )
    ) {
        InitAssestTopBar(navController, authViewModel)
        Spacer(modifier = Modifier.height(28.dp))
        BannerAssests(navController)
    }
}

// Bagian top bar
@Composable
fun InitAssestTopBar(navController: NavController, authViewModel: AuthViewModel) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            text = "Self-Assesment",
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
}

// Bagian informasi
@Composable
fun BannerAssests(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(width = 320.dp, height = 540.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xff493EAC))
        ) {
            // Content di atas background
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Let’s Start Test!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Be honest, be calm, and trust yourself. Self-awareness is the first step to growth.",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BannerPoint("Read the question carefully.", "There are 7 questions that must be answered.")
                    BannerPoint("Reflect on your real experiences.", "Think about how you’ve reacted, behaved, or felt in similar situations in real life.")
                    BannerPoint("Trust your first instinct.", "Usually, your first honest reaction is the most accurate one. Don’t overthink.")
                    BannerPoint("Notes: ", "GAD-7 is an anxiety disorder measurement test. It does not replace clinical examination & cannot make a diagnosis by itself.")

                }
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ){
                    Image(
                        painter = painterResource(id = R.drawable.ass_brain),
                        contentDescription = "Profile",
                        modifier = Modifier.size(150.dp)
                    )
                    Text("CalmMe", style = MaterialTheme.typography.displaySmall, color = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {navController.navigate(Routes.Assesment.route)},
            colors = ButtonDefaults.buttonColors(
                containerColor =  Color(0xFF933C9F),
            )
        ) {
            Text("Start Test", style = MaterialTheme.typography.titleLarge)
        }
    }
}

// Buat bikin point" keterangan di banner
@Composable
fun BannerPoint(title: String, desc: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
            )
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.85f)
            ),

        )
    }
}
