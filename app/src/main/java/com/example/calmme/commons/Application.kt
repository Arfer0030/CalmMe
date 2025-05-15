package com.example.calmme.commons

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calmme.R
import com.example.calmme.pages.community.CommunityScreen
import com.example.calmme.pages.consultation.ConsultationScreen
import com.example.calmme.pages.history.HistoryScreen
import com.example.calmme.pages.home.HomeScreen

data class NavigationItem(
    val route: String,
    val icon: Int,
    val label: String
)

@Composable
fun Application() {
    // Inisialisasi NavController
    val navController = rememberNavController()

    // Daftar item navigasi di Bottom Navigation
    val navigationItems = listOf(
        NavigationItem(Routes.Home, R.drawable.ic_home, "Home"),
        NavigationItem(Routes.Consultation, R.drawable.ic_consul, "Portofolio"),
        NavigationItem(Routes.History, R.drawable.ic_history, "History"),
        NavigationItem(Routes.Consultation, R.drawable.ic_community, "Consultation"),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xffFAF0FB),
                tonalElevation = 0.dp,
                modifier = Modifier.padding(2.dp).height(100.dp)
            ) {
                navigationItems.forEach { item ->
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val selected = backStackEntry?.destination?.route == item.route

                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.Home) { HomeScreen() }
            composable(Routes.Consultation) { ConsultationScreen() }
            composable(Routes.History) { HistoryScreen() }
            composable(Routes.Community) { CommunityScreen() }
        }
    }
}