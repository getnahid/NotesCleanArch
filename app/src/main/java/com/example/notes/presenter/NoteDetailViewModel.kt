package com.example.notes.presenter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.domain.model.Note
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.GetNoteByIdUseCase
import com.example.notes.domain.usecase.UpsertNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * User intents/actions for the Note Detail screen
 */
sealed interface NoteDetailIntent {
    data object LoadNote : NoteDetailIntent
    data class UpdateNote(val title: String, val body: String) : NoteDetailIntent
    data object DeleteNote : NoteDetailIntent
    data object NavigateBack : NoteDetailIntent
}

/**
 * Side effects for the Note Detail screen (one-time events)
 */
sealed interface NoteDetailState {
    data class ShowError(val message: String) : NoteDetailState
    data class ShowSnackbar(val message: String) : NoteDetailState
    data object NavigateBack : NoteDetailState
}

/**
 * UI state for the Note Detail screen
 */
data class NoteDetailUiState(
    val note: Note? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNoteById: GetNoteByIdUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val deleteNote: DeleteNoteUseCase
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NoteDetailState>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(NoteDetailIntent.LoadNote)
    }

    fun handleIntent(intent: NoteDetailIntent) {
        when (intent) {
            is NoteDetailIntent.LoadNote -> loadNote()
            is NoteDetailIntent.UpdateNote -> updateNote(intent.title, intent.body)
            is NoteDetailIntent.DeleteNote -> delete()
            is NoteDetailIntent.NavigateBack -> navigateBack()
        }
    }

    private fun loadNote() {
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
                _effect.send(NoteDetailState.ShowError("Failed to load note: ${e.message}"))
            }
        }
    }

    private fun updateNote(title: String, body: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val currentNote = _uiState.value.note
                if (currentNote == null) {
                    _effect.send(NoteDetailState.ShowError("Note not found"))
                    return@launch
                }

                val updatedNote = currentNote.copy(
                    title = title,
                    body = body,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
                upsertNote(updatedNote)
                _effect.send(NoteDetailState.ShowSnackbar("Note updated successfully"))
            } catch (e: Exception) {
                _effect.send(NoteDetailState.ShowError("Failed to update note: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun delete() {
        viewModelScope.launch {
            try {
                deleteNote(noteId)
                _effect.send(NoteDetailState.NavigateBack)
            } catch (e: Exception) {
                _effect.send(NoteDetailState.ShowError("Failed to delete note: ${e.message}"))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(NoteDetailState.NavigateBack)
        }
    }
}

