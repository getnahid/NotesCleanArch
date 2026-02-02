package com.example.notes.presenter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.domain.model.Note
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.ObserveNotesUseCase
import com.example.notes.domain.usecase.RefreshNotesUseCase
import com.example.notes.domain.usecase.UpsertNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    observeNotes: ObserveNotesUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val refreshNotes: RefreshNotesUseCase
) : ViewModel() {

    private var refreshing = false

    val uiState: StateFlow<NotesUiState> =
        observeNotes()
            .map { NotesUiState(notes = it, isRefreshing = refreshing) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun addSampleNote() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val note = Note(
                id = UUID.randomUUID().toString(),
                title = "New note",
                body = "Created at $now",
                updatedAtEpochMs = now
            )
            upsertNote(note)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { deleteNote(id) }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing = true
            try {
                refreshNotes()
            } catch (t: Throwable) {
                Log.v("", t.toString())
                // No-op; for sample simplicity
            } finally {
                refreshing = false
            }
        }
    }
}