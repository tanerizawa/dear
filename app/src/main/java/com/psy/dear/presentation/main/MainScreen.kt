package com.psy.dear.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.psy.dear.presentation.chat.ChatScreen
import com.psy.dear.presentation.growth.GrowthScreen
import com.psy.dear.presentation.home.HomeScreen
import com.psy.dear.presentation.journal_editor.JournalEditorScreen
import com.psy.dear.presentation.journal_detail.JournalDetailScreen
import com.psy.dear.presentation.navigation.Screen
import com.psy.dear.presentation.profile.ProfileScreen
import com.psy.dear.presentation.services.ServicesScreen
import com.psy.dear.ui.theme.DigitalPrimary
import com.psy.dear.ui.theme.SubtleText

val mainScreens = listOf(Screen.Home, Screen.Chat, Screen.Growth, Screen.Services, Screen.Profile)

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val mainNavController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                mainScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = null) },
                        label = { Text(screen.title!!) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            mainNavController.navigate(screen.route) {
                                popUpTo(mainNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DigitalPrimary,
                            selectedTextColor = DigitalPrimary,
                            unselectedIconColor = SubtleText,
                            unselectedTextColor = SubtleText
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController = mainNavController) }
            composable(Screen.Chat.route) {
                ChatScreen(
                    navController = mainNavController,
                    onNavigateToLogin = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.MainFlow.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Growth.route) { GrowthScreen(navController = mainNavController) }
            composable(Screen.Services.route) { ServicesScreen(navController = mainNavController) }
            composable(Screen.Profile.route) { ProfileScreen(navController = mainNavController) }

            composable(Screen.JournalEditor.route) { JournalEditorScreen(navController = mainNavController) }
            composable(Screen.JournalDetail.route) { JournalDetailScreen(navController = mainNavController) }
        }
    }
}
