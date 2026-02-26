package com.example.notes.presenter

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notes.presenter.navigation.Screen

@Composable
fun NotesAppRoot() {
    val navController = rememberNavController()

    MaterialTheme {
        Surface {
            NavHost(
                navController = navController,
                startDestination = Screen.NotesList.route
            ) {
                composable(Screen.NotesList.route) {
                    NotesScreen(
                        onNoteClick = { noteId ->
                            navController.navigate(Screen.NoteDetail.createRoute(noteId))
                        }
                    )
                }

                composable(Screen.NoteDetail.route) {
                    NoteDetailScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}