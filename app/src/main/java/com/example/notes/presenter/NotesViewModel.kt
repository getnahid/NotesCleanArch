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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One-time UI events for Notes screen (Snackbar, Navigation, etc.)
 */
sealed interface NotesUiEvent {
    data class ShowError(val message: String) : NotesUiEvent
}

/**
 * UI state for the Notes List screen
 */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
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

    private val _events = MutableSharedFlow<NotesUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<NotesUiEvent> = _events.asSharedFlow()

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
                    updatedAtMs = now
                )
                upsertNote(note)
            } catch (e: Exception) {
                _events.tryEmit(
                    NotesUiEvent.ShowError("Failed to add note: ${e.message ?: "Unknown error"}")
                )
            }
        }
    }

    /**
     * Delete a note by ID
     */
    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(id)
            } catch (e: Exception) {
                _events.tryEmit(
                    NotesUiEvent.ShowError("Failed to delete note: ${e.message ?: "Unknown error"}")
                )
            }
        }
    }

    /**
     * Refresh notes from remote source
     */
    fun refreshNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                refreshNotesUseCase()
            } catch (t: Throwable) {
                Log.v("NotesViewModel", t.toString())
                _events.tryEmit(
                    NotesUiEvent.ShowError("Failed to refresh: ${t.message ?: "Unknown error"}")
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}