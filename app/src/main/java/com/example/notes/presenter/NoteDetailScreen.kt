package com.example.notes.presenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NoteDetailScreen(
    onBack: () -> Unit,
    vm: NoteDetailViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is NoteDetailUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                NoteDetailUiEvent.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    LaunchedEffect(state.note) {
        state.note?.let {
            title = it.title
            body = it.body
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onBack) { Text("Back") }
                Button(onClick = vm::deleteNote) { Text("Delete") }
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { vm.updateNote(title, body) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}