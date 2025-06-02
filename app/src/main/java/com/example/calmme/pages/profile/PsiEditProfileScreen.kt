package com.example.calmme.pages.profile

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.data.TimeSlot
import kotlinx.coroutines.launch

@Composable
fun PsiEditProfileScreen(editPsikologViewModel: EditPsikologViewModel = viewModel()) {
    val navController = LocalNavController.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Profile", "Schedule")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF3E7FE), Color(0xFFF7F2F9))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.width(60.dp))
                Text(
                    text = "Psychologist Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF8E44AD),
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF8E44AD)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF8E44AD) else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> ProfileTab(editPsikologViewModel)
                1 -> ScheduleTab(editPsikologViewModel)
            }
        }
    }
}

@Composable
fun ProfileTab(editPsikologViewModel: EditPsikologViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val psychologistData by editPsikologViewModel.psychologistData.collectAsState()
    val isLoading by editPsikologViewModel.isLoading.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var selectedSpecializations by remember { mutableStateOf(setOf<String>()) }

    val availableSpecializations = listOf(
        "children and adolescents", "Adult", "elderly",
        "relationship & Family", "trauma", "addiction",
    )

    LaunchedEffect(psychologistData) {
        name = psychologistData.name
        description = psychologistData.description
        experience = psychologistData.experience
        education = psychologistData.education
        license = psychologistData.license
        selectedSpecializations = psychologistData.specialization.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8E44AD),
                focusedLabelColor = Color(0xFF8E44AD)
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8E44AD),
                focusedLabelColor = Color(0xFF8E44AD)
            ),
            maxLines = 4
        )

        OutlinedTextField(
            value = experience,
            onValueChange = { experience = it },
            label = { Text("Experience (e.g., '10 years')") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8E44AD),
                focusedLabelColor = Color(0xFF8E44AD)
            )
        )

        OutlinedTextField(
            value = education,
            onValueChange = { education = it },
            label = { Text("Education") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8E44AD),
                focusedLabelColor = Color(0xFF8E44AD)
            )
        )

        OutlinedTextField(
            value = license,
            onValueChange = { license = it },
            label = { Text("License Number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8E44AD),
                focusedLabelColor = Color(0xFF8E44AD)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Specializations",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column {
            availableSpecializations.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { specialization ->
                        SpecializationChip(
                            text = specialization,
                            isSelected = selectedSpecializations.contains(specialization),
                            onToggle = {
                                selectedSpecializations = if (selectedSpecializations.contains(specialization)) {
                                    selectedSpecializations - specialization
                                } else {
                                    selectedSpecializations + specialization
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (selectedSpecializations.isEmpty()) {
                    Toast.makeText(context, "Please select at least one specialization", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                coroutineScope.launch {
                    editPsikologViewModel.updatePsychologistData(
                        name = name,
                        specialization = selectedSpecializations.toList(),
                        description = description,
                        experience = experience,
                        education = education,
                        license = license,
                        onSuccess = {
                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8E44AD)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Save Changes",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ScheduleTab(editPsikologViewModel: EditPsikologViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val schedules by editPsikologViewModel.schedules.collectAsState()
    val psychologistData by editPsikologViewModel.psychologistData.collectAsState()
    val isLoading by editPsikologViewModel.isLoading.collectAsState()

    val daysOfWeek = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
    val timeSlots = listOf(
        "09:00-10:00", "10:00-11:00", "11:00-12:00", "13:00-14:00",
        "14:00-15:00", "15:00-16:00", "16:00-17:00"
    )
    var selectedDay by remember { mutableStateOf("monday") }
    var selectedTimeSlots by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(selectedDay, schedules, psychologistData.psychologistId) {
        if (psychologistData.psychologistId.isNotEmpty()) {
            editPsikologViewModel.loadSchedules()
        }
        val daySchedule = schedules.find { it.dayOfWeek == selectedDay }
        selectedTimeSlots = daySchedule?.timeSlots?.map { "${it.startTime}-${it.endTime}" }?.toSet() ?: emptySet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Manage Schedule",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Select Day",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(daysOfWeek) { day ->
                DayChip(
                    day = day,
                    isSelected = selectedDay == day,
                    onSelect = { selectedDay = day }
                )
            }
        }
        Text(
            text = "Available Time Slots",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column {
            timeSlots.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { timeSlot ->
                        TimeSlotChip(
                            timeSlot = timeSlot,
                            isSelected = selectedTimeSlots.contains(timeSlot),
                            onToggle = {
                                selectedTimeSlots = if (selectedTimeSlots.contains(timeSlot)) {
                                    selectedTimeSlots - timeSlot
                                } else {
                                    selectedTimeSlots + timeSlot
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val timeSlotObjects = selectedTimeSlots.map { slot ->
                        val times = slot.split("-")
                        TimeSlot(
                            startTime = times[0],
                            endTime = times[1],
                            isAvailable = true
                        )
                    }
                    editPsikologViewModel.updateSchedule(
                        dayOfWeek = selectedDay,
                        timeSlots = timeSlotObjects,
                        onSuccess = {
                            Toast.makeText(context, "Schedule updated for $selectedDay", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8E44AD)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Save Schedule for ${selectedDay.replaceFirstChar { it.uppercase() }}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Schedule Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                schedules.forEach { schedule ->
                    Text(
                        text = "${schedule.dayOfWeek.replaceFirstChar { it.uppercase() }}: ${schedule.timeSlots.size} slots",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                if (schedules.isEmpty()) {
                    Text(
                        text = "No schedule set yet",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun DayChip(
    day: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onSelect() }
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF8E44AD) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF8E44AD) else Color.LightGray
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = day.take(3).replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TimeSlotChip(
    timeSlot: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onToggle() }
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF8E44AD) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF8E44AD) else Color.LightGray
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = timeSlot,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SpecializationChip(
    text: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onToggle() }
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF8E44AD) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF8E44AD) else Color.LightGray
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 12.sp
        )
    }
}