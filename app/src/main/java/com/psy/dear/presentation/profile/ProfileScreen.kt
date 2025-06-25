package com.psy.dear.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.dear.domain.model.User
import com.psy.dear.presentation.navigation.Screen
import com.psy.dear.core.asString
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            if (event == ProfileEvent.LogoutSuccess) {
                navController.navigate(Screen.Login.route) {
                    popUpTo("main_flow") { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil") }) }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.user != null -> {
                    ProfileContent(user = state.user!!, onLogout = viewModel::logout)
                }
            }
            state.error?.let { error ->
                Text(
                    text = error.asString(),
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ProfileContent(user: User, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = user.username, style = MaterialTheme.typography.headlineMedium)
        Text(text = user.email, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileContentPreview() {
    val dummyUser = User(
        id = "123",
        username = "Odang Rodiana",
        email = "odang@example.com"
    )
    MaterialTheme {
        ProfileContent(user = dummyUser, onLogout = {})
    }
}

