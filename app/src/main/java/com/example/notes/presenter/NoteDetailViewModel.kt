package com.example.notes.presenter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.domain.model.Note
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.GetNoteByIdUseCase
import com.example.notes.domain.usecase.UpsertNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One-time events for the Note Detail screen
 */
sealed interface NoteDetailEvent {
    data class ShowError(val message: String) : NoteDetailEvent
    data class ShowSnackbar(val message: String) : NoteDetailEvent
    data object NavigateBack : NoteDetailEvent
}

/**
 * UI state for the Note Detail screen
 */
data class NoteDetailUiState(
    val note: Note? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

/**
 * MVVM ViewModel for Note Detail screen
 *
 * Exposes UI state via StateFlow and one-time events via SharedFlow
 * Provides simple public methods for UI actions
 */
@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNoteById: GetNoteByIdUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val deleteNote: DeleteNoteUseCase
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    // UI State exposed to the view
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    // One-time events exposed to the view
    private val _events = MutableSharedFlow<NoteDetailEvent>()
    val events = _events.asSharedFlow()

    init {
        loadNote()
    }

    /**
     * Load note by ID and observe changes
     */
    fun loadNote() {
        viewModelScope.launch {
            try {
                getNoteById(noteId).collect { note ->
                    _uiState.update {
                        it.copy(
                            note = note,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(NoteDetailEvent.ShowError("Failed to load note: ${e.message}"))
            }
        }
    }

    /**
     * Update the note with new title and body
     */
    fun updateNote(title: String, body: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val currentNote = _uiState.value.note
                if (currentNote == null) {
                    _events.emit(NoteDetailEvent.ShowError("Note not found"))
                    return@launch
                }

                val updatedNote = currentNote.copy(
                    title = title,
                    body = body,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
                upsertNote(updatedNote)
                _events.emit(NoteDetailEvent.ShowSnackbar("Note updated successfully"))
            } catch (e: Exception) {
                _events.emit(NoteDetailEvent.ShowError("Failed to update note: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    /**
     * Delete the current note
     */
    fun deleteNote() {
        viewModelScope.launch {
            try {
                deleteNote.invoke(noteId)
                _events.emit(NoteDetailEvent.NavigateBack)
            } catch (e: Exception) {
                _events.emit(NoteDetailEvent.ShowError("Failed to delete note: ${e.message}"))
            }
        }
    }

    /**
     * Navigate back to previous screen
     */
    fun navigateBack() {
        viewModelScope.launch {
            _events.emit(NoteDetailEvent.NavigateBack)
        }
    }
}
