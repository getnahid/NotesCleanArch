
package com.example.notes

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.notes.presentation.notes.NotesScreen

@Composable
fun NotesAppRoot() {
  MaterialTheme {
    Surface { NotesScreen() }
  }
}