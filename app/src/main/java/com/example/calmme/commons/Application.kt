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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calmme.R
import com.example.calmme.pages.assesment.AssesmentScreen
import com.example.calmme.pages.assesment.InitAssestScreen
import com.example.calmme.pages.authentication.AuthScreen
import com.example.calmme.pages.authentication.AuthViewModel
import com.example.calmme.pages.authentication.EmailVerificationScreen
import com.example.calmme.pages.chat.ChatScreen
import com.example.calmme.pages.consultation.AppointmentScreen
import com.example.calmme.pages.consultation.ConsultationScreen
import com.example.calmme.pages.consultation.ConsultationViewModel
import com.example.calmme.pages.dailymood.DailyMoodScreen
import com.example.calmme.pages.dailymood.DailyMoodViewModel
import com.example.calmme.pages.history.HistoryScreen
import com.example.calmme.pages.home.HomeScreen
import com.example.calmme.pages.meditate.MeditateScreen
import com.example.calmme.pages.meditate.MusicScreen
import com.example.calmme.pages.profile.AboutScreen
import com.example.calmme.pages.profile.ProfileScreen
import com.example.calmme.pages.subscribe.SubscribeScreen
import com.example.calmme.pages.profile.EditProfileScreen
import com.example.calmme.pages.profile.EditSecurityScreen
import com.example.calmme.pages.profile.HelpScreen
import com.example.calmme.pages.profile.PaymentHistoryScreen
import com.example.calmme.pages.profile.PsiEditProfileScreen
import com.example.calmme.pages.subscribe.ConfirmationScreen
import com.example.calmme.pages.subscribe.PaymentScreen
import com.example.calmme.pages.subscribe.SubscribeViewModel


data class NavigationItem(
    val route: String,
    val icon: Int,
    val label: String
)

@Composable
fun Application(
    authViewModel: AuthViewModel,
    consultationViewModel: ConsultationViewModel,
    dailyMoodViewModel: DailyMoodViewModel,
    subscribeViewModel: SubscribeViewModel,
) {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavController provides navController) {
        val navigationItems = listOf(
            NavigationItem(Routes.Home.route, R.drawable.ic_home, "Home"),
            NavigationItem(Routes.Consultation.route, R.drawable.ic_consul, "Consultation"),
            NavigationItem(Routes.History.route, R.drawable.ic_chat, "History"),
            NavigationItem(Routes.Profile.route, R.drawable.ic_profile, "Profile")
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (shouldShowBottomBar()) {
                    NavigationBar(
                        containerColor = Color(0xffFAF0FB),
                        tonalElevation = 2.dp,
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
                                        contentDescription = item.label,
                                        tint = if (selected) Color(0xFF8E44AD) else Color.Gray
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
                composable(Routes.Home.route) { HomeScreen(authViewModel = authViewModel, dailyMoodViewModel = dailyMoodViewModel) }
                composable(Routes.Consultation.route) { ConsultationScreen(consultationViewModel) }
                composable(Routes.Appointment.route) { AppointmentScreen(consultationViewModel) }
                composable(Routes.History.route) { HistoryScreen() }
                composable(Routes.Profile.route) { ProfileScreen(authViewModel) }
                composable(Routes.Assesment.route) { AssesmentScreen() }
                composable(Routes.InitAssesment.route) { InitAssestScreen(authViewModel) }
                composable(Routes.DailyMood.route) { DailyMoodScreen(dailyMoodViewModel, authViewModel) }
                composable(Routes.Subscribe.route) { SubscribeScreen(subscribeViewModel, authViewModel) }
                composable(Routes.Payment.route) { PaymentScreen(subscribeViewModel, authViewModel)}
                composable(Routes.Confirmation.route) { ConfirmationScreen(subscribeViewModel, authViewModel) }
                composable(Routes.Meditate.route) { MeditateScreen(navController, authViewModel) }
                composable(
                    route = "music/{audioResId}",
                    arguments = listOf(navArgument("audioResId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val audioResId = backStackEntry.arguments?.getInt("audioResId") ?: 0
                    MusicScreen(navController, audioResId, authViewModel)
                }
                composable(Routes.EditProfile.route) {EditProfileScreen(authViewModel)}
                composable(Routes.PsiEditProfile.route) { PsiEditProfileScreen() }
                composable(Routes.EditSecurity.route) { EditSecurityScreen(authViewModel) }
                composable(
                    route = "email_verification/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    EmailVerificationScreen(
                        authViewModel = authViewModel,
                        email = email
                    )
                }
                composable(
                    route = "chat/{chatRoomId}",
                    arguments = listOf(navArgument("chatRoomId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
                    ChatScreen(chatRoomId = chatRoomId)
                }
                composable(Routes.Help.route) { HelpScreen() }
                composable(Routes.About.route) { AboutScreen() }
                composable(Routes.PaymentHistory.route) { PaymentHistoryScreen() }
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
        Routes.InitAssesment.route,
        Routes.DailyMood.route,
        Routes.EditProfile.route,
        Routes.Confirmation.route,
        Routes.Payment.route,
        Routes.Subscribe.route,
        Routes.EditSecurity.route,
        "email_verification/{email}",
        Routes.Chat.route,
        Routes.PsiEditProfile.route,
        Routes.Help.route,
        Routes.About.route,
        Routes.PaymentHistory.route,
        Routes.Meditate.route,
        "music/{audioResId}",
    )

    return currentRoute != null && currentRoute !in noBottomBarScreens
}