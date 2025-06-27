package com.psy.dear.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.psy.dear.presentation.auth.login.LoginScreen
import com.psy.dear.presentation.auth.register.RegisterScreen
import com.psy.dear.presentation.main.MainScreen
import com.psy.dear.presentation.onboarding.OnboardingScreen
import java.net.URLEncoder

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Onboarding : Screen("onboarding")
    object AuthFlow : Screen("auth_flow")
    object MainFlow : Screen("main_flow")

    object Login : Screen("login")
    object Register : Screen("register")

    // --- Main Screens (Bottom Navigation) ---
    object Home : Screen("home", "Beranda", Icons.Outlined.Home)
    object Chat : Screen("chat", "Chat", Icons.Outlined.ChatBubbleOutline)
    object Growth : Screen("growth", "Pertumbuhan", Icons.AutoMirrored.Outlined.ShowChart)
    object Services : Screen("services", "Layanan", Icons.Outlined.FavoriteBorder)
    object Profile : Screen("profile", "Profil", Icons.Outlined.PersonOutline)

    // --- Detail & Editor Screens ---
    object JournalEditor : Screen("journal_editor?journalId={journalId}") {
        fun createRoute(journalId: String?): String = "journal_editor?journalId=$journalId"
    }
    object JournalDetail : Screen("journal_detail/{journalId}") {
        fun createRoute(journalId: String): String = "journal_detail/$journalId"
    }

    // --- Content List Screens ---
    object ArticleList : Screen("article_list", "Semua Artikel")
    object AudioList : Screen("audio_list", "Semua Audio")
    object JournalList : Screen("journal_list", "Semua Jurnal")

    // --- Content Detail Screens ---
    object ArticleDetail : Screen("article_detail/{articleUrl}/{articleTitle}") {
        fun createRoute(url: String, title: String): String {
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            return "article_detail/$encodedUrl/$encodedTitle"
        }
    }
    object AudioPlayer : Screen("audio_player/{trackUrl}/{trackTitle}") {
        fun createRoute(url: String, title: String): String {
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            return "audio_player/$encodedUrl/$encodedTitle"
        }
    }
}

@Composable
fun AppNavigation(startDestination: String) {
    val navController: NavHostController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.AuthFlow.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        navigation(startDestination = Screen.Login.route, route = Screen.AuthFlow.route) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Screen.MainFlow.route) { popUpTo(Screen.AuthFlow.route) { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.MainFlow.route) { popUpTo(Screen.AuthFlow.route) { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.MainFlow.route) {
            MainScreen(navController)
        }
    }
}