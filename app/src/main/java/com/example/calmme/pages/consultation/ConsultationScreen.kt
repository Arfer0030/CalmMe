package com.example.calmme.pages.consultation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.data.PsychologistData
import com.example.calmme.data.TimeSlot

@Composable
fun ConsultationScreen(consultationViewModel: ConsultationViewModel) {
    val filteredPsychologists by consultationViewModel.filteredPsychologists.collectAsState()
    val isLoading by consultationViewModel.isLoading.collectAsState()
    val errorMessage by consultationViewModel.errorMessage.collectAsState()
    val currentUserProfilePicture by consultationViewModel.currentUserProfilePicture.collectAsState()
    val psychologistProfilePictures by consultationViewModel.psychologistProfilePictures.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var selectedSpecialization by remember { mutableStateOf("") }

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
        ConsulHeader(currentUserProfilePicture = currentUserProfilePicture)
        Spacer(modifier = Modifier.height(12.dp))
        SearchBar(
            searchText = searchText,
            onSearchTextChange = {
                searchText = it
                consultationViewModel.searchPsychologists(it)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        SpecializationFilter(
            selectedSpecialization = selectedSpecialization,
            onSpecializationSelected = { specialization ->
                selectedSpecialization = specialization
                consultationViewModel.filterPsychologistsBySpecialization(specialization)
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        TopPsychologistsSection(
            psychologists = filteredPsychologists,
            consultationViewModel = consultationViewModel,
            isLoading = isLoading,
            errorMessage = errorMessage,
            psychologistProfilePictures = psychologistProfilePictures
        )
    }
}

@Composable
fun ConsulHeader(currentUserProfilePicture: String?) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentUserProfilePicture)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.profile),
                    error = painterResource(id = R.drawable.profile),
                    fallback = painterResource(id = R.drawable.profile),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(85.dp))
                Text(
                    text = "Consultation",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Let's find your psychologist!",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}


@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        placeholder = {
            Text("Search psychologist name...", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
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
fun SpecializationFilter(
    selectedSpecialization: String,
    onSpecializationSelected: (String) -> Unit
) {
    val specializations = listOf(
        "All", "Children", "Adult", "Relationship", "Family", "Addiction", "Trauma", "Elderly"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(specializations) { specialization ->
            FilterChip(
                selected = selectedSpecialization == specialization ||
                        (selectedSpecialization.isEmpty() && specialization == "All"),
                onClick = {
                    onSpecializationSelected(
                        if (specialization == "All") "" else specialization.lowercase()
                    )
                },
                label = { Text(specialization) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF933C9F),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun TopPsychologistsSection(
    psychologists: List<PsychologistData>,
    consultationViewModel: ConsultationViewModel,
    isLoading: Boolean,
    errorMessage: String?,
    psychologistProfilePictures: Map<String, String?>
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

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF933C9F))
                }
            }
            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { consultationViewModel.refreshPsychologists() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF933C9F))
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }
            psychologists.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Text(
                        text = "No psychologists found",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(psychologists) { psychologist ->
                        PsychologistCard(
                            psychologist = psychologist,
                            consultationViewModel = consultationViewModel,
                            profilePictureUrl = psychologistProfilePictures[psychologist.psychologistId]
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PsychologistCard(
    psychologist: PsychologistData,
    consultationViewModel: ConsultationViewModel,
    profilePictureUrl: String?
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val todayTimeSlots = consultationViewModel.getTodayTimeSlotsForPsychologist(psychologist.psychologistId)

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
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(profilePictureUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Psychologist Profile",
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.profile),
                    error = painterResource(id = R.drawable.profile),
                    fallback = painterResource(id = R.drawable.profile),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(psychologist.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        psychologist.getSpecializationText(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clock),
                            contentDescription = "Available today",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TodayTimeSlotsDisplay(timeSlots = todayTimeSlots)
                    }

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


@Composable
fun TodayTimeSlotsDisplay(timeSlots: List<TimeSlot>) {
    when {
        timeSlots.isEmpty() -> {
            Text(
                "No slots today",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        timeSlots.size == 1 -> {
            Text(
                "${timeSlots.first().startTime}-${timeSlots.first().endTime}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        timeSlots.size <= 3 -> {
            val displaySlots = timeSlots.take(2)
            val slotsText = displaySlots.joinToString(", ") { "${it.startTime}-${it.endTime}" }
            val remainingCount = timeSlots.size - displaySlots.size

            Text(
                text = if (remainingCount > 0) "$slotsText +$remainingCount more" else slotsText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        else -> {
            val firstSlot = timeSlots.first()
            val lastSlot = timeSlots.last()
            Text(
                "${timeSlots.size} slots (${firstSlot.startTime}-${lastSlot.endTime})",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

fun PsychologistData.getSpecializationText(): String {
    return when {
        specialization.isEmpty() -> "General Psychology"
        specialization.size == 1 -> specialization.first().replaceFirstChar { it.uppercase() }
        else -> specialization.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}

