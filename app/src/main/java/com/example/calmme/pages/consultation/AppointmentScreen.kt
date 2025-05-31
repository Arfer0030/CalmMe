package com.example.calmme.pages.consultation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.commons.Routes
import com.example.calmme.data.PsychologistData
import com.example.calmme.commons.Resource
import com.example.calmme.data.ScheduleData
import com.example.calmme.data.TimeSlot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    var showDatePicker by remember { mutableStateOf(false) }

    // Check if psychologist is selected
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

    // Load time slots when date is selected
    LaunchedEffect(selectedDate) {
        if (selectedDate.isNotEmpty() && psychologist != null) {
            appointmentViewModel.loadAvailableTimeSlots(psychologist!!.psychologistId, selectedDate)
        }
    }

    // Handle error messages
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
            AppointmentTopBar(navController)
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
            PsychologistImageSection(psychologist!!)
            Spacer(modifier = Modifier.height(50.dp))
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
                // Di AppointmentScreen.kt - Modifikasi bagian onSubmitClick
                // Di AppointmentScreen.kt - Modifikasi bagian onSubmitClick
                // Di AppointmentScreen.kt - Menggunakan fungsi alternatif
                onSubmitClick = {
                    if (selectedDate.isNotEmpty() && selectedTimeSlot != null && selectedMethod.isNotEmpty()) {
                        coroutineScope.launch {
                            appointmentViewModel.createAppointmentDirectly(
                                psychologistId = psychologist!!.psychologistId,
                                onSuccess = { appointmentId, needsPayment ->
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

    // Date Picker Dialog
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

@Composable
fun AppointmentTopBar(navController: NavController) {
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
            text = "Consultation",
            style = MaterialTheme.typography.headlineSmall
        )
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Profile",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun PsychologistImageSection(psychologist: PsychologistData) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder for psychologist image
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFF8E44AD), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = psychologist.getInitials(),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

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
        // Visit Time Header
        Text(
            text = "Visit Time",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Psychologist Name and Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = psychologist.name,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = psychologist.getSpecializationText(),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = psychologist.description.ifEmpty { "Experienced psychologist providing professional mental health support." },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Working Hours Section
        WorkingHoursSection(
            scheduleData = scheduleData,
            workingHours = workingHours
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Section
        StatsSection(psychologist)

        Spacer(modifier = Modifier.height(24.dp))

        // Make Appointment Button
        MakeAppointmentButton(onClick = onDatePickerClick)

        // Selected Date and Time Slots
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

        // Consultation Method Selector
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally // Ini akan menengahkan children-nya
        ) {
            ConsultationMethodButton(R.drawable.ic_chat, "Chat", selectedMethod, onMethodSelect)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        SubmitButton(onClick = onSubmitClick)
    }
}

@Composable
fun WorkingHoursSection(
    scheduleData: List<ScheduleData>,
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
            // Tampilkan working hours berdasarkan schedule
            WorkingHoursDisplay(workingHours)
        }
    }
}

@Composable
fun WorkingHoursDisplay(workingHours: Map<String, String>) {
    // Group days with same working hours
    val groupedHours = workingHours.entries
        .groupBy { it.value }
        .map { (hours, days) ->
            val dayNames = days.map { it.key.getDayDisplayName() }
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

// Helper functions
fun String.getDayDisplayName(): String {
    return when (this.lowercase()) {
        "monday" -> "Mon"
        "tuesday" -> "Tue"
        "wednesday" -> "Wed"
        "thursday" -> "Thu"
        "friday" -> "Fri"
        "saturday" -> "Sat"
        "sunday" -> "Sun"
        else -> this.replaceFirstChar { it.uppercase() }
    }
}

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
                    modifier = Modifier.height(200.dp)
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

                // Generate next 7 days as available dates
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

// Helper function
fun generateAvailableDates(): List<Pair<String, String>> {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    val dates = mutableListOf<Pair<String, String>>()

    // Generate next 7 days
    for (i in 1..7) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dateString = dateFormat.format(calendar.time)
        val dayString = dayFormat.format(calendar.time)
        dates.add(dateString to dayString)
    }

    return dates
}

fun formatDayRange(days: List<String>): String {
    return when {
        days.size == 1 -> days.first()
        days.size == 2 -> "${days.first()} & ${days.last()}"
        isConsecutiveDays(days) -> "${days.first()} - ${days.last()}"
        else -> days.joinToString(", ")
    }
}

fun isConsecutiveDays(days: List<String>): Boolean {
    val dayOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val indices = days.mapNotNull { dayOrder.indexOf(it) }.sorted()

    if (indices.size <= 1) return false

    for (i in 1 until indices.size) {
        if (indices[i] != indices[i-1] + 1) return false
    }
    return true
}

