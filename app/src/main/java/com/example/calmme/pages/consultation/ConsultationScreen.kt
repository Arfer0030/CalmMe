package com.example.calmme.pages.consultation

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.data.psychologistss
import kotlinx.serialization.Serializable


@Composable
fun ConsultationScreen(consultationViewModel: ConsultationViewModel) {
    val psychologists by consultationViewModel.psychologists.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFf7e9f8), Color(0xFFfdfbfe))
                )
            )
            .padding(16.dp)
    ) {
        ConsulHeader()
        Spacer(modifier = Modifier.height(12.dp))
        SearchBar()
        Spacer(modifier = Modifier.height(24.dp))
        TopPsychologistsSection(psychologists, consultationViewModel)
    }
}

@Composable
fun ConsulHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center)
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.width(90.dp))
                Text(
                    text = "Consultation",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Let’s find your psychologist!",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    var searchText by remember { mutableStateOf("") }
    TextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = {
            Text("Search psychologist", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        },
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        )
    )
}


@Composable
fun TopPsychologistsSection(
    psychologists: List<PshycologistItem>,
    consultationViewModel: ConsultationViewModel
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Top Psychologists", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "See All",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF933C9F),
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(psychologists) { psychologist ->
                PsychologistCard(psychologist, consultationViewModel)
            }
        }
    }
}


@Composable
fun PsychologistCard(psychologist: PshycologistItem, consultationViewModel: ConsultationViewModel) {
    val navController = LocalNavController.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = psychologist.image),
                    contentDescription = "Psychologist",
                    modifier = Modifier.size(58.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(psychologist.name, style = MaterialTheme.typography.titleMedium)
                    Text(psychologist.description, style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clock),
                            contentDescription = "Clock",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(psychologist.schedule, style = MaterialTheme.typography.bodyLarge)

                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            modifier = Modifier
                                .height(30.dp)
                                .width(100.dp),
                            onClick = {
                                consultationViewModel.setSelectedPsychologist(psychologist)
                                navController.navigate(Routes.Appointment.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF933C9F)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Appointment",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}


@Serializable
data class PshycologistItem(
    val name: String,
    val image: Int,
    val description: String,
    val about: String,
    val schedule: String
)