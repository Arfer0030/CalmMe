package com.example.calmme.pages.consultation

import android.content.ClipData.Item
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController

@Composable
fun AppointmentScreen(viewModel: ConsultationViewModel) {
    val navController = LocalNavController.current
    val psychologist by viewModel.selectedPsychologist.collectAsState()


    var selectedMethod by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {
        item {
            AppointmentTopBar(navController)
        }
        item {
            PsychologistImage(viewModel)
        }
        item {
            AppointmentInfoCard(
                onDatePickerClick = { showDatePicker = true },
                onSubmitClick = { /* Handle Submission */ },
                selectedMethod = selectedMethod,
                onMethodSelect = { selectedMethod = it },
                viewModel = viewModel
            )
        }
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
            painter = painterResource(id = R.drawable.ic_arrow),
            contentDescription = "Back",
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    navController.popBackStack()
                }
        )
        Text(
            text = "Consultation",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Profile",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun PsychologistImage(viewModel: ConsultationViewModel) {
    val psychologist by viewModel.selectedPsychologist.collectAsState()
    Image(
        painter = painterResource(id = psychologist!!.image),
        contentDescription = "Psychologist",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun AppointmentInfoCard(
    onDatePickerClick: () -> Unit,
    onSubmitClick: () -> Unit,
    selectedMethod: String,
    onMethodSelect: (String) -> Unit,
    viewModel: ConsultationViewModel
) {
    val psychologist by viewModel.selectedPsychologist.collectAsState()
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
        AppointmentDetails(viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        AppointmentStats()
        Spacer(modifier = Modifier.height(24.dp))
        MakeAppointmentButton(onClick = onDatePickerClick)
        Spacer(modifier = Modifier.height(24.dp))
        ConsultationMethodSelector(selectedMethod, onMethodSelect)
        Spacer(modifier = Modifier.height(24.dp))
        SubmitButton(onClick = onSubmitClick)
    }
}

@Composable
fun AppointmentDetails(viewModel: ConsultationViewModel) {
    val psychologist by viewModel.selectedPsychologist.collectAsState()
    // Bagian pertama di tengah
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally // Align ke tengah
    ) {
        Text("Visit Time", fontSize = 18.sp, fontWeight = FontWeight.Bold )
        Spacer(modifier = Modifier.height(8.dp))
        Text(psychologist!!.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(psychologist!!.description, fontSize = 14.sp)
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Bagian kedua di kiri
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start // Align ke kiri (default)
    ) {
        Text("About", fontWeight = FontWeight.Bold)
        Text(psychologist!!.about, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text("Working Hours", fontWeight = FontWeight.Bold)
        Text(psychologist!!.schedule, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text("Stats", fontWeight = FontWeight.Bold)
    }
}


@Composable
fun AppointmentStats() {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        StatsItem("300+", "Patients")
        StatsItem("10 years", "Experience")
        StatsItem("10", "Certifications")
    }
}

@Composable
fun MakeAppointmentButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(50.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFB8F7FD), Color(0xFFCEBFE6))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp)) // Ini memastikan sudut tetap rounded
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = "Gradient Button", color = Color(0xff933C9F))
        }
    }
}

@Composable
fun ConsultationMethodSelector(selectedMethod: String, onMethodSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ConsultationMethodButton(R.drawable.ic_chat, "Chat", selectedMethod, onMethodSelect)
        ConsultationMethodButton(R.drawable.ic_phone, "Call", selectedMethod, onMethodSelect)
        ConsultationMethodButton(R.drawable.ic_cam, "Video", selectedMethod, onMethodSelect)
    }
}

@Composable
fun ConsultationMethodButton(icon: Int, label: String, selectedMethod: String, onClick: (String) -> Unit) {
    val backgroundColor = if (selectedMethod == label) Color(0xFF933C9F) else Color.White
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable { onClick(label) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = if (selectedMethod == label) Color.White else Color.Gray
        )
    }
}

@Composable
fun SubmitButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        Button (
            onClick = {},
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff933C9F), contentColor = Color.Black),
            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Submit",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp))
            }
        }
    }
}



@Composable
fun StatsItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ConsultationMethodButton(icon: Int, label: String, selectedMethod: String, onClick: () -> Unit) {
    val backgroundColor = if (selectedMethod == label) Color(0xFF933C9F) else Color(0xFFF5F5F5)
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = if (selectedMethod == label) Color.White else Color.Gray
        )
    }
}