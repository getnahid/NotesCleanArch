package com.example.notes.presenter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.domain.model.Note
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.GetNoteByIdUseCase
import com.example.notes.domain.usecase.UpsertNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteDetailUiState(
    val note: Note? = null,
    val error: String? = null,
    val shouldNavigateBack: Boolean = false
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNoteById: GetNoteByIdUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        loadNote()
    }

    fun loadNote() {
        viewModelScope.launch {
            try {
                getNoteById(noteId).collect { note ->
                    _uiState.update { it.copy(note = note) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load note: ${e.message}") }
            }
        }
    }

    fun updateNote(title: String, body: String) {
        viewModelScope.launch {
            try {
                val currentNote = _uiState.value.note
                if (currentNote == null) {
                    _uiState.update { it.copy(error = "Note not found") }
                    return@launch
                }

                val updatedNote = currentNote.copy(
                    title = title,
                    body = body,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
                upsertNote(updatedNote)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update note: ${e.message}") }
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            try {
                deleteNoteUseCase.invoke(noteId)
                _uiState.update { it.copy(shouldNavigateBack = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete note: ${e.message}") }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    fun onNavigateBackHandled() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }
}