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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One-time events for the Notes List screen
 */
sealed interface NotesEvent {
    data class ShowError(val message: String) : NotesEvent
    data class NavigateToDetail(val noteId: String) : NotesEvent
}

/**
 * UI state for the Notes List screen
 */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)

/**
 * MVVM ViewModel for Notes List screen
 *
 * Exposes UI state via StateFlow and one-time events via SharedFlow
 * Provides simple public methods for UI actions
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val observeNotes: ObserveNotesUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val refreshNotes: RefreshNotesUseCase
) : ViewModel() {

    // UI State exposed to the view
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    // One-time events exposed to the view
    private val _events = MutableSharedFlow<NotesEvent>()
    val events = _events.asSharedFlow()

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
                _events.emit(NotesEvent.ShowError("Failed to add note: ${e.message}"))
            }
        }
    }

    /**
     * Delete a note by ID
     */
    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                deleteNote.invoke(id)
            } catch (e: Exception) {
                _events.emit(NotesEvent.ShowError("Failed to delete note: ${e.message}"))
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
                refreshNotes.invoke()
            } catch (t: Throwable) {
                Log.v("NotesViewModel", t.toString())
                _events.emit(NotesEvent.ShowError("Failed to refresh: ${t.message}"))
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    /**
     * Navigate to note detail screen
     */
    fun navigateToDetail(noteId: String) {
        viewModelScope.launch {
            _events.emit(NotesEvent.NavigateToDetail(noteId))
        }
    }
}