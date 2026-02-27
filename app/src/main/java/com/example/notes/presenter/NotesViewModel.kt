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
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Notes List screen
 */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val navigateToNoteId: String? = null // one-time navigation signal
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val observeNotes: ObserveNotesUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val refreshNotesUseCase: RefreshNotesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    /**
     * Load notes from repository and observe changes
     */
    fun loadNotes() {
        viewModelScope.launch {
            observeNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
    }

    /**
     * Add a new sample note
     */
    fun addSampleNote() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val note = Note(
                    id = UUID.randomUUID().toString(),
                    title = "New note",
                    body = "Created at $now",
                    updatedAtEpochMs = now
                )
                upsertNote(note)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add note: ${e.message}") }
            }
        }
    }

    /**
     * Delete a note by ID
     */
    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase.invoke(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete note: ${e.message}") }
            }
        }
    }

    /**
     * Refresh notes from remote source
     */
    fun refreshNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                refreshNotesUseCase.invoke()
            } catch (t: Throwable) {
                Log.v("NotesViewModel", t.toString())
                _uiState.update { it.copy(error = "Failed to refresh: ${t.message}") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    /**
     * Navigate to note detail screen (one-time)
     */
    fun navigateToDetail(noteId: String) {
        _uiState.update { it.copy(navigateToNoteId = noteId) }
    }

    /**
     * Call this from UI after handling navigation
     */
    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToNoteId = null) }
    }

    /**
     * Call this from UI after showing the error (snackbar/dialog)
     */
    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }
}