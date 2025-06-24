package com.psy.dear.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.psy.dear.presentation.auth.login.LoginScreen
import com.psy.dear.presentation.auth.register.RegisterScreen
import com.psy.dear.presentation.main.MainScreen
import com.psy.dear.presentation.onboarding.OnboardingScreen

// CATATAN: Untuk animasi, tambahkan dependency:
// implementation("com.google.accompanist:accompanist-navigation-animation:LATEST_VERSION")

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Onboarding : Screen("onboarding")
    object AuthFlow : Screen("auth_flow")
    object MainFlow : Screen("main_flow")

    object Login : Screen("login")
    object Register : Screen("register")

    object Home : Screen("home", "Beranda", Icons.Outlined.Home)
    object Chat : Screen("chat", "Chat", Icons.Outlined.ChatBubbleOutline)
    object Growth : Screen("growth", "Pertumbuhan", Icons.Outlined.ShowChart)
    object Services : Screen("services", "Layanan", Icons.Outlined.FavoriteBorder)
    object Profile : Screen("profile", "Profil", Icons.Outlined.PersonOutline)

    object JournalEditor : Screen("journal_editor?journalId={journalId}") {
        fun createRoute(journalId: String?): String = "journal_editor?journalId=$journalId"
    }

    object JournalDetail : Screen("journal_detail/{journalId}") {
        fun createRoute(journalId: String): String = "journal_detail/$journalId"
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(startDestination: String) {
    val navController: NavHostController = rememberAnimatedNavController()
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
        exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) }
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
