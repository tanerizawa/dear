package com.psy.dear.presentation.journal_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.dear.presentation.navigation.Screen
import kotlinx.coroutines.flow.collectLatest
import com.psy.dear.core.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(
    navController: NavController,
    viewModel: JournalDetailViewModel = hiltViewModel()
) {
    val journal by viewModel.journal.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                DetailEvent.DeleteSuccess -> navController.navigateUp()
                is DetailEvent.ShowError -> snackbarHostState.showSnackbar(event.message.asString())
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Jurnal") },
            text = { Text("Apakah Anda yakin ingin menghapus entri ini?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteJournal()
                    showDeleteDialog = false
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(journal?.title ?: "Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.JournalEditor.createRoute(journal?.id))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }
            )
        }
    ) { padding ->
        journal?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(it.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(it.mood, style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(it.content, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
