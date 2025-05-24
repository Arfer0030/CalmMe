package com.example.calmme.pages.assesment

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import kotlinx.serialization.Serializable

@Composable
fun AssesmentScreen(assesmentViewModel: AssesmentViewModel = viewModel()) {
    val questions = assesmentViewModel.questions.collectAsState().value
    val progress = assesmentViewModel.getProgress()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf("") }
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
        HeaderSection(progress)
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
                    onDismiss = { showDialog = false },
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
                    Button(
                        onClick = {
                            score = assesmentViewModel.getTotalScore()
                            category = assesmentViewModel.getResultCategory(score)
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF933C9F))
                    ) {
                        Text("Submit", color = Color.White)
                    }
                }
            }
        }
    }
}



@Composable
fun HeaderSection(progress: Float) {
    val navController = LocalNavController.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                modifier = Modifier
                    .clickable {
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
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Hasil Tes Kamu", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .background(Color(0xFFE0F7FA), RoundedCornerShape(8.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Skor: $score", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Kategori: $category",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF933C9F),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Assesment) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF933C9F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Kembali ke Home", color = Color.White)
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
