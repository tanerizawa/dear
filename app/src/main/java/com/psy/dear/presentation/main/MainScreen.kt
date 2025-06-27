package com.psy.dear.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.psy.dear.presentation.chat.ChatScreen
import com.psy.dear.presentation.content.ArticleDetailScreen
import com.psy.dear.presentation.content.ArticleListScreen
import com.psy.dear.presentation.content.AudioListScreen
import com.psy.dear.presentation.content.AudioPlayerScreen
import com.psy.dear.presentation.growth.GrowthScreen
import com.psy.dear.presentation.home.HomeScreen
import com.psy.dear.presentation.home.HomeViewModel
import com.psy.dear.presentation.journal.JournalListScreen
import com.psy.dear.presentation.journal_detail.JournalDetailScreen
import com.psy.dear.presentation.journal_editor.JournalEditorScreen
import com.psy.dear.presentation.navigation.Screen
import com.psy.dear.presentation.profile.ProfileScreen
import com.psy.dear.presentation.services.ServicesScreen
import java.net.URLDecoder

val mainScreens = listOf(Screen.Home, Screen.Chat, Screen.Growth, Screen.Services, Screen.Profile)

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val mainNavController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
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
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    onNavigateToLogin = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.MainFlow.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Growth.route) { GrowthScreen() }
            composable(Screen.Services.route) { ServicesScreen() }
            composable(Screen.Profile.route) { ProfileScreen(navController = mainNavController) }

            composable(Screen.JournalEditor.route) { JournalEditorScreen(navController = mainNavController) }
            composable(Screen.JournalDetail.route) { JournalDetailScreen(navController = mainNavController) }

            composable(Screen.ArticleList.route) {
                val homeViewModel: HomeViewModel = hiltViewModel(mainNavController.getBackStackEntry(Screen.Home.route))
                ArticleListScreen(navController = mainNavController, homeViewModel = homeViewModel)
            }
            composable(Screen.JournalList.route) {
                val homeViewModel: HomeViewModel = hiltViewModel(mainNavController.getBackStackEntry(Screen.Home.route))
                JournalListScreen(navController = mainNavController, homeViewModel = homeViewModel)
            }
            composable(Screen.AudioList.route) {
                val homeViewModel: HomeViewModel = hiltViewModel(mainNavController.getBackStackEntry(Screen.Home.route))
                AudioListScreen(navController = mainNavController, homeViewModel = homeViewModel)
            }

            composable(
                route = Screen.ArticleDetail.route,
                arguments = listOf(
                    navArgument("articleUrl") { type = NavType.StringType },
                    navArgument("articleTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("articleUrl") ?: ""
                val title = backStackEntry.arguments?.getString("articleTitle") ?: "Artikel"
                val decodedUrl = URLDecoder.decode(url, "UTF-8")
                val decodedTitle = URLDecoder.decode(title, "UTF-8")
                ArticleDetailScreen(
                    navController = mainNavController,
                    title = decodedTitle,
                    url = decodedUrl
                )
            }
            composable(
                route = Screen.AudioPlayer.route,
                arguments = listOf(
                    navArgument("trackUrl") { type = NavType.StringType },
                    navArgument("trackTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("trackUrl") ?: ""
                val title = backStackEntry.arguments?.getString("trackTitle") ?: "Audio Track"
                val decodedUrl = URLDecoder.decode(url, "UTF-8")
                val decodedTitle = URLDecoder.decode(title, "UTF-8")
                AudioPlayerScreen(
                    navController = mainNavController,
                    trackTitle = decodedTitle,
                    trackUrl = decodedUrl
                )
            }
        }
    }
}