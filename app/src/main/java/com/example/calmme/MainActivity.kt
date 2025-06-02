package com.example.calmme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.calmme.commons.Application
import com.example.calmme.pages.authentication.AuthViewModel
import com.example.calmme.pages.consultation.ConsultationViewModel
import com.example.calmme.pages.dailymood.DailyMoodViewModel
import com.example.calmme.pages.subscribe.SubscribeViewModel
import com.example.calmme.ui.theme.CalmMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val consultationViewModel : ConsultationViewModel by viewModels()
        val dailyMoodViewModel : DailyMoodViewModel by viewModels()
        val subscribeViewModel : SubscribeViewModel by viewModels()
        setContent {
            CalmMeTheme {
                Application(
                    authViewModel = authViewModel,
                    consultationViewModel = consultationViewModel,
                    dailyMoodViewModel = dailyMoodViewModel,
                    subscribeViewModel = subscribeViewModel,
                )
            }
        }
    }
}