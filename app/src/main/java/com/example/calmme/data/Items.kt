package com.example.calmme.data

import androidx.compose.ui.graphics.Color
import com.example.calmme.R
import com.example.calmme.commons.Routes

// Daftar mood dan ikon masing-masing
val moods = listOf(
    "calm" to R.drawable.md_calm,
    "happy" to R.drawable.md_happy,
    "disappoint" to R.drawable.md_diss,
    "frustrated" to R.drawable.md_frustrated,
    "surprised" to R.drawable.md_surprised,
    "sad" to R.drawable.md_sad,
    "bored" to R.drawable.md_bored,
    "angry" to R.drawable.md_angry,
    "worried" to R.drawable.md_worried,
)

// Data buat bikin card categories di home
data class CategoryData(
    val name: String,
    val icon: Int,
    val color1: Color,
    val color2: Color,
    val route: String
)

val categoryList = listOf(
    CategoryData(
        "Meditate Time",
        R.drawable.ct_medi,
        Color(0xffDBE1E2),
        Color(0xffB8F7FD),
        Routes.Meditate.route),
    CategoryData(
        "Self-Assessment Test",
        R.drawable.ct_test,
        Color(0xffF7E8F8),
        Color(0xffCEBFE6),
        Routes.InitAssesment.route
    ),
    CategoryData(
        "Consultation",
        R.drawable.foryou_1,
        Color(0xffDBE1E2),
        Color(0xffB8F7FD),
        Routes.Consultation.route
    ),
    CategoryData(
        "Daily Mood Tracker",
        R.drawable.ct_mood,
        Color(0xffFDFFDC),
        Color(0xffFBFFA8),
        Routes.DailyMood.route
    ),
)