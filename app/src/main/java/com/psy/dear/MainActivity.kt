package com.psy.dear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.psy.dear.presentation.main.AuthState
import com.psy.dear.presentation.main.MainViewModel
import com.psy.dear.presentation.navigation.AppNavigation
import com.psy.dear.presentation.navigation.Screen
import com.psy.dear.ui.theme.DearTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DearTheme {
                val authState by viewModel.authState.collectAsState()

                when (authState) {
                    AuthState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is AuthState.Result -> {
                        val startDestination = if ((authState as AuthState.Result).onboardingCompleted) {
                            if ((authState as AuthState.Result).isLoggedIn) Screen.MainFlow.route else Screen.AuthFlow.route
                        } else {
                            Screen.Onboarding.route
                        }
                        AppNavigation(startDestination = startDestination)
                    }
                }
            }
        }
    }
}
