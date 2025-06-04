package com.example.calmme.pages.consultation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.data.PsychologistData
import com.example.calmme.data.ScheduleData
import com.example.calmme.data.TimeSlot
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AppointmentScreen(
    consultationViewModel: ConsultationViewModel,
    appointmentViewModel: AppointmentViewModel = viewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val psychologist by consultationViewModel.selectedPsychologist.collectAsState()
    val workingHours by appointmentViewModel.workingHours.collectAsState()
    val scheduleData by appointmentViewModel.scheduleData.collectAsState()
    val availableTimeSlots by appointmentViewModel.availableTimeSlots.collectAsState()
    val selectedDate by appointmentViewModel.selectedDate.collectAsState()
    val selectedTimeSlot by appointmentViewModel.selectedTimeSlot.collectAsState()
    val selectedMethod by appointmentViewModel.selectedConsultationMethod.collectAsState()
    val isLoading by appointmentViewModel.isLoading.collectAsState()
    val errorMessage by appointmentViewModel.errorMessage.collectAsState()
    val selectedPsychologist by consultationViewModel.selectedPsychologist.collectAsState()
    val selectedPsychologistProfilePicture by appointmentViewModel.selectedPsychologistProfilePicture.collectAsState()
    val currentUserProfilePicture by appointmentViewModel.currentUserProfilePicture.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    // Cek psikolog yg dipilih
    if (psychologist == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "No psychologist selected", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        return
    } else LaunchedEffect(psychologist) {
        psychologist?.let { psych ->
            appointmentViewModel.loadPsychologistSchedule(psych.psychologistId)
        }
    }

    // Load selected psikolognya
    LaunchedEffect(selectedPsychologist) {
        selectedPsychologist?.let { psychologist ->
            appointmentViewModel.setSelectedPsychologist(psychologist)
        }
    }

    // Load time slot yang tersedia
    LaunchedEffect(selectedDate) {
        if (selectedDate.isNotEmpty()) {
            appointmentViewModel.loadAvailableTimeSlots(psychologist!!.psychologistId, selectedDate)
        }
    }

    // Handel error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            appointmentViewModel.clearError()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        item {
            AppointmentHeader(
                currentUserProfilePicture = currentUserProfilePicture
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PsychologistImageSection(
                psychologist = psychologist!!,
                profilePictureUrl = selectedPsychologistProfilePicture
            )
        }
        item {
            AppointmentInfoCard(
                psychologist = psychologist!!,
                scheduleData = scheduleData,
                workingHours = workingHours,
                availableTimeSlots = availableTimeSlots,
                selectedDate = selectedDate,
                selectedTimeSlot = selectedTimeSlot,
                selectedMethod = selectedMethod,
                isLoading = isLoading,
                onDatePickerClick = { showDatePicker = true },
                onTimeSlotSelect = { appointmentViewModel.setSelectedTimeSlot(it) },
                onMethodSelect = { appointmentViewModel.setConsultationMethod(it) },
                onSubmitClick = {
                    if (selectedDate.isNotEmpty() && selectedTimeSlot != null && selectedMethod.isNotEmpty()) {
                        coroutineScope.launch {
                            appointmentViewModel.createAppointmentDirectly(
                                psychologistId = psychologist!!.psychologistId,
                                onSuccess = { _, needsPayment ->
                                    if (needsPayment) {
                                        Toast.makeText(context, "Appointment created! Please complete payment to confirm.", Toast.LENGTH_LONG).show()
                                        navController.navigate(Routes.Subscribe.route)
                                    } else {
                                        Toast.makeText(context, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    } else {
                        Toast.makeText(context, "Please select date, time, and consultation method", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // Dialog buat milih tanggal
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                appointmentViewModel.setSelectedDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

// Bagian topbar
@Composable
fun AppointmentHeader(currentUserProfilePicture: String?) {
    val navController = LocalNavController.current
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Back",
            modifier = Modifier
                .size(24.dp)
                .clickable { navController.popBackStack() }
        )
        Text(
            text = "Book Appointment",
            style = MaterialTheme.typography.headlineSmall
        )
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(currentUserProfilePicture)
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


// Bagian gambar psikolog
@Composable
fun PsychologistImageSection(psychologist: PsychologistData,  profilePictureUrl: String?) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(profilePictureUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Psychologist Profile",
            modifier = Modifier
                .size(200.dp),
            placeholder = painterResource(id = R.drawable.profile),
            error = painterResource(id = R.drawable.profile),
            fallback = painterResource(id = R.drawable.profile),
            contentScale = ContentScale.Crop
        )
    }
}

// Bagian info psikolog
@Composable
fun AppointmentInfoCard(
    psychologist: PsychologistData,
    scheduleData: List<ScheduleData>,
    workingHours: Map<String, String>,
    availableTimeSlots: List<TimeSlot>,
    selectedDate: String,
    selectedTimeSlot: TimeSlot?,
    selectedMethod: String,
    isLoading: Boolean,
    onDatePickerClick: () -> Unit,
    onTimeSlotSelect: (TimeSlot) -> Unit,
    onMethodSelect: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFDFBFE), Color(0xFFF7E9F8))
                )
            )
            .padding(24.dp)
    ) {
        Text(
            text = "Visit Time",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bagian nama dan title psikolog
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = psychologist.name,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,

            )
            Text(
                text = psychologist.getSpecializationText(),
                style = MaterialTheme.typography.bodyLarge,

            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bagian about
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = psychologist.description.ifEmpty { "Experienced psychologist providing professional mental health support." },
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bagian working hours
        WorkingHoursSection(
            workingHours = workingHours
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bagian stats
        StatsSection(psychologist)

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol make appointment
        MakeAppointmentButton(onClick = onDatePickerClick)

        if (selectedDate.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SelectedDateCard(selectedDate, selectedTimeSlot)

            Spacer(modifier = Modifier.height(16.dp))
            TimeSlotSelection(
                availableTimeSlots = availableTimeSlots,
                selectedTimeSlot = selectedTimeSlot,
                onTimeSlotSelect = onTimeSlotSelect,
                isLoading = isLoading
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bagian pemilihan metode konsultasi
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConsultationMethodButton(R.drawable.ic_chat, "Chat", selectedMethod, onMethodSelect)
        }

        Spacer(modifier = Modifier.height(24.dp))

        SubmitButton(onClick = onSubmitClick)
    }
}

// Buat bikin bagian working hours
@Composable
fun WorkingHoursSection(
    workingHours: Map<String, String>
) {
    Column {
        Text(
            text = "Working Hours",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (workingHours.isEmpty()) {
            Text(
                text = "Working hours not available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            WorkingHoursDisplay(workingHours)
        }
    }
}

// Buat nampilin working hours dan ngelompokin berdasarkan hari
@Composable
fun WorkingHoursDisplay(workingHours: Map<String, String>) {
    val groupedHours = workingHours.entries
        .groupBy { it.value }
        .map { (hours, days) ->
            val dayNames = days.map {it.key}
            val dayRange = formatDayRange(dayNames)
            dayRange to hours
        }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        groupedHours.forEach { (dayRange, hours) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dayRange,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = hours,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

// Buat bikin bagian stats isinya eucation, experience, license
@Composable
fun StatsSection(psychologist: PsychologistData) {
    Text(
        text = "Stats",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatsItem(
            value = psychologist.education.ifEmpty { " - " },
            label = "Education"
        )
        StatsItem(
            value = psychologist.experience.ifEmpty { " - " },
            label = "Experience"
        )
        StatsItem(
            value = if (psychologist.license.isNotEmpty()) "SIPP"
                else " - ",
            label = "License"
        )
    }
}

// Buat bikin item di stats
@Composable
fun StatsItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

// Buat bikin bagian card selected date
@Composable
fun SelectedDateCard(selectedDate: String, selectedTimeSlot: TimeSlot?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Selected Appointment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Date: $selectedDate", style = MaterialTheme.typography.bodyMedium)
            selectedTimeSlot?.let { timeSlot ->
                Text(
                    "Time: ${timeSlot.startTime} - ${timeSlot.endTime}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Buat bikin bagian pilihan time slot
@Composable
fun TimeSlotSelection(
    availableTimeSlots: List<TimeSlot>,
    selectedTimeSlot: TimeSlot?,
    onTimeSlotSelect: (TimeSlot) -> Unit,
    isLoading: Boolean
) {
    Column {
        Text(
            "Available Time Slots",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF933C9F))
                }
            }
            availableTimeSlots.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Text(
                        text = "No available time slots for this date",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFE65100)
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(availableTimeSlots) { timeSlot ->
                        TimeSlotCard(
                            timeSlot = timeSlot,
                            isSelected = selectedTimeSlot == timeSlot,
                            onSelect = { onTimeSlotSelect(timeSlot) }
                        )
                    }
                }
            }
        }
    }
}

// Buat bikin card tampilan timeslot
@Composable
fun TimeSlotCard(
    timeSlot: TimeSlot,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF933C9F) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF933C9F) else Color.LightGray
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${timeSlot.startTime} - ${timeSlot.endTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// Buat bikin tombol make appointment
@Composable
fun MakeAppointmentButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFB8F7FD), Color(0xFFCEBFE6))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Make an appointment",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xff933C9F)
            )
        }
    }
}

// Buat bikin tombol pilihan method konsultasi
@Composable
fun ConsultationMethodButton(
    icon: Int,
    label: String,
    selectedMethod: String,
    onClick: (String) -> Unit
) {
    val backgroundColor = if (selectedMethod == label) Color(0xFF933C9F) else Color.White
    val iconColor = if (selectedMethod == label) Color.White else Color.Gray

    Box(
        modifier = Modifier
            .size(60.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable { onClick(label) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

// Buat bikin tombol submit
@Composable
fun SubmitButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff933C9F),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(16.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "Submit",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Buat bikin dialog pilih tanggal dan ditampilin 7 hari terakhir
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column {
                Text("Available dates:")
                Spacer(modifier = Modifier.height(8.dp))

                val availableDates = generateAvailableDates()

                availableDates.forEach { date ->
                    TextButton(
                        onClick = { onDateSelected(date.first) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(date.second) // Day name
                            Text(date.first) // Date
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Fungsi helper buat nload tanggal yang tersedia
fun generateAvailableDates(): List<Pair<String, String>> {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    val dates = mutableListOf<Pair<String, String>>()

    // Generate 7 hari
    for (i in 1..7) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dateString = dateFormat.format(calendar.time)
        val dayString = dayFormat.format(calendar.time)
        dates.add(dateString to dayString)
    }

    return dates
}

// Fungsi helper buat ngelompokin range hari kerja yang tersedia
fun formatDayRange(days: List<String>): String {
    return when {
        days.size == 1 -> days.first()
        days.size == 2 -> "${days.first()} & ${days.last()}"
        isConsecutiveDays(days) -> "${days.first()} - ${days.last()}"
        else -> days.joinToString(", ")
    }
}

// Fungsi helper buat cek daftar hari merupakan hari yang berurutan
fun isConsecutiveDays(days: List<String>): Boolean {
    val dayOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val indices = days.map { dayOrder.indexOf(it) }.sorted()

    if (indices.size <= 1) return false

    for (i in 1 until indices.size) {
        if (indices[i] != indices[i-1] + 1) return false
    }
    return true
}