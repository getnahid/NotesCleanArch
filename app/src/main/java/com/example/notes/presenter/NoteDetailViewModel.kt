package com.example.notes.presenter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.domain.model.Note
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.ObserveNoteByIdUseCase
import com.example.notes.domain.usecase.UpsertNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
 * One-time UI events for Note Detail screen
 */
sealed interface NoteDetailUiEvent {
    data class ShowError(val message: String) : NoteDetailUiEvent
    data object NavigateBack : NoteDetailUiEvent
}

data class NoteDetailUiState(
    val note: Note? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNoteById: ObserveNoteByIdUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NoteDetailUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<NoteDetailUiEvent> = _events.asSharedFlow()

    init {
        loadNote()
    }

    fun loadNote() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getNoteById(noteId).collect { note ->
                    _uiState.update { it.copy(note = note, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.tryEmit(
                    NoteDetailUiEvent.ShowError("Failed to load note: ${e.message ?: "Unknown error"}")
                )
            }
        }
    }

    fun updateNote(title: String, body: String) {
        viewModelScope.launch {
            try {
                val currentNote = _uiState.value.note
                if (currentNote == null) {
                    _events.tryEmit(NoteDetailUiEvent.ShowError("Note not found"))
                    return@launch
                }

                val updatedNote = currentNote.copy(
                    title = title,
                    body = body,
                    updatedAtMs = System.currentTimeMillis()
                )
                upsertNote(updatedNote)
            } catch (e: Exception) {
                _events.tryEmit(
                    NoteDetailUiEvent.ShowError("Failed to update note: ${e.message ?: "Unknown error"}")
                )
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            try {
                deleteNoteUseCase.invoke(noteId)
                _events.tryEmit(NoteDetailUiEvent.NavigateBack)
            } catch (e: Exception) {
                _events.tryEmit(
                    NoteDetailUiEvent.ShowError("Failed to delete note: ${e.message ?: "Unknown error"}")
                )
            }
        }
    }
}