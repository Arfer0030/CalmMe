package com.example.calmme.pages.assesment

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import kotlinx.serialization.Serializable

@Composable
fun AssesmentScreen(assesmentViewModel: AssesmentViewModel = viewModel()) {
    val questions = assesmentViewModel.questions.collectAsState().value
    val canSubmit = assesmentViewModel.canSubmit.collectAsState().value
    val progress = assesmentViewModel.getProgress()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf("") }
    val navController = LocalNavController.current

    // Reset jawaban ketika screen di-dispose (keluar dari assessment)
    DisposableEffect(Unit) {
        onDispose {
            assesmentViewModel.resetAllAnswers()
        }
    }

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
        HeaderSection(progress, assesmentViewModel)
        AnswerLegend()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xffFEFFF3))
                .padding(16.dp)
        ) {
            if (showDialog) {
                TestResultPopup(
                    score = score,
                    category = category,
                    onDismiss = { /* Kosongkan untuk mencegah dismiss */ },
                )
            }

            LazyColumn(modifier = Modifier.padding(4.dp)) {
                itemsIndexed(questions) { index, item ->
                    QuestionItemView(
                        questionNumber = index + 1,
                        item = item,
                        onAnswerSelected = { newAnswer ->
                            assesmentViewModel.updateAnswer(index, newAnswer)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Tampilkan pesan jika belum semua soal dijawab
                    if (!canSubmit) {
                        val unansweredCount = assesmentViewModel.getUnansweredQuestionsCount()
                        Text(
                            text = "Masih ada $unansweredCount soal yang belum dijawab",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            if (canSubmit) {
                                score = assesmentViewModel.getTotalScore()
                                category = assesmentViewModel.getResultCategory(score)
                                showDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canSubmit) Color(0xFF933C9F) else Color.Gray,
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = if (canSubmit) "Submit" else "Jawab Semua Soal Terlebih Dahulu",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(progress: Float, assesmentViewModel: AssesmentViewModel) {
    val navController = LocalNavController.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                modifier = Modifier
                    .clickable {
                        // Reset jawaban sebelum keluar
                        assesmentViewModel.resetAllAnswers()
                        navController.popBackStack()
                    }
            )
            Spacer(modifier = Modifier.width(86.dp))
            Text("Self-Assessment", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Test Questions", style = MaterialTheme.typography.titleMedium, color = Color(0xffD8E115))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(vertical = 8.dp, horizontal = 50.dp),
            color = Color(0xffD8E115),
            trackColor = Color(0xffFBFFA8)
        )
    }
}


@Composable
fun AnswerLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        LegendItem(Color(0xFFCEBFE6), "0", "never")
        LegendItem(Color(0xFFB79CE2), "1", "a few days")
        LegendItem(Color(0xFF9579C2), "2", "more than half of the time")
        LegendItem(Color(0xFF674D90), "3", "almost every day")
    }
}

@Composable
fun LegendItem(
    bgColor: Color,
    text: String,
    desc: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(8.dp))
                .height(32.dp)
                .width(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, style = MaterialTheme.typography.titleLarge)
        }
        Text(
            desc,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(70.dp),
            color = Color(0xFF933C9F)
        )
    }
}


@Composable
fun QuestionItemView(
    questionNumber: Int,
    item: QuestionItem,
    onAnswerSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Pertanyaan
        Text(
            text = "$questionNumber. ${item.question}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Opsi Jawaban
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (0..3).forEach { value ->
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(54.dp)
                        .background(
                            if (item.answer == value) Color(0xFF7A4ECE) else Color(0xffCCCCCC),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onAnswerSelected(value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
fun TestResultPopup(
    score: Int,
    category: String,
    onDismiss: () -> Unit,
) {
    val navController = LocalNavController.current
    Dialog(
        onDismissRequest = { /* Kosongkan untuk mencegah dismiss ketika klik di luar */ },
        properties = DialogProperties(
            dismissOnBackPress = false, // Mencegah dismiss dengan tombol back
            dismissOnClickOutside = false // Mencegah dismiss ketika klik di luar
        )
    ) {
        Box(
            modifier = Modifier
                .size(280.dp, 300.dp) // Ukuran popup
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.ass_result), // Ganti dengan gambar background Anda
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
            )

            // Overlay semi-transparan untuk meningkatkan readability text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.1f), // Overlay gelap transparan
                        RoundedCornerShape(16.dp)
                    )
            )

            // Content di atas background
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                Spacer(modifier = Modifier.height(14.dp))

                // Score Box
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Skor: $score",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF933C9F)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Box
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF933C9F),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Button
                Button(
                    onClick = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF933C9F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(120.dp)
                        .height(36.dp)
                ) {
                    Text(
                        text = "To Home",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}


@Serializable
data class QuestionItem(
    val question: String,
    var answer: Int = -1 // -1 artinya belum dijawab
)
