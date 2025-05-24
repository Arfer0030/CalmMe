package com.example.calmme.commons

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.calmme.pages.assesment.AssesmentScreen
import com.example.calmme.pages.assesment.AssesmentViewModel
import com.example.calmme.pages.authentication.AuthScreen
import com.example.calmme.pages.authentication.AuthViewModel
import com.example.calmme.pages.consultation.AppointmentScreen
import com.example.calmme.pages.consultation.ConsultationScreen
import com.example.calmme.pages.consultation.ConsultationViewModel
import com.example.calmme.pages.history.HistoryScreen
import com.example.calmme.pages.home.HomeScreen
import com.example.calmme.pages.meditate.MeditateScreen
import com.example.calmme.pages.profile.ProfileScreen
import com.example.calmme.pages.subscribe.SubscribeScreen

data class NavigationItem(
    val route: String,
    val icon: Int,
    val label: String
)

@Composable
fun Application(
    authViewModel: AuthViewModel,
    consultationViewModel: ConsultationViewModel,
    assesmentViewModel: AssesmentViewModel,
    ) {
    val navController = rememberNavController()

    // Menyediakan NavController ke seluruh composable di dalamnya
    CompositionLocalProvider(LocalNavController provides navController) {
        // Daftar item navigasi di Bottom Navigation
        val navigationItems = listOf(
            NavigationItem(Routes.Home.route, R.drawable.ic_home, "Home"),
            NavigationItem(Routes.Consultation.route, R.drawable.ic_consul, "Consultation"),
            NavigationItem(Routes.History.route, R.drawable.ic_history, "History"),
            NavigationItem(Routes.Profile.route, R.drawable.ic_community, "Profile")
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (shouldShowBottomBar()) {
                    NavigationBar(
                        containerColor = Color(0xffFAF0FB),
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .padding(2.dp)
                            .height(100.dp)
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
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.Authentication.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Routes.Authentication.route) { AuthScreen(authViewModel) }
                composable(Routes.Home.route) { HomeScreen(authViewModel = authViewModel) }
                composable(Routes.Consultation.route) {
                    ConsultationScreen(consultationViewModel)
                }
                composable(Routes.Appointment.route) {
                    AppointmentScreen(consultationViewModel)
                }
                composable(Routes.History.route) { HistoryScreen() }
                composable(Routes.Profile.route) { ProfileScreen() }
                composable(Routes.Assesment.route) { AssesmentScreen(assesmentViewModel) }
                composable(Routes.Subscribe.route) { SubscribeScreen() }
                composable (Routes.Meditate.route) { MeditateScreen() }
            }
        }
    }
}

@Composable
fun shouldShowBottomBar(): Boolean {
    val navController = LocalNavController.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val noBottomBarScreens = listOf(
        Routes.Authentication.route,
        Routes.Appointment.route,
        Routes.Assesment.route,
    )

    // Jika `currentRoute` tidak null dan tidak termasuk dalam daftar layar tanpa BottomBar
    return currentRoute != null && currentRoute !in noBottomBarScreens
}
