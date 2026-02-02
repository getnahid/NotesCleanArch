package com.example.notes.presenter.navigation

sealed class Screen(val route: String) {
    data object NotesList : Screen("notes_list")
    data object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
}

