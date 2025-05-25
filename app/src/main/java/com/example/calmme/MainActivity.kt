package com.example.calmme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.calmme.commons.Application
import com.example.calmme.pages.assesment.AssesmentViewModel
import com.example.calmme.pages.authentication.AuthViewModel
import com.example.calmme.pages.consultation.ConsultationViewModel
import com.example.calmme.ui.theme.CalmMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val consultationViewModel : ConsultationViewModel by viewModels()
        val assesmentViewModel : AssesmentViewModel by viewModels()
        setContent {
            CalmMeTheme {
                Application(
                    authViewModel = authViewModel,
                    consultationViewModel = consultationViewModel,
                    assesmentViewModel = assesmentViewModel,
                )
            }
        }
    }
}