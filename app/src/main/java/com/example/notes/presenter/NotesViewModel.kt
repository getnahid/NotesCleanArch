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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * User intents/actions for the Notes List screen
 */
sealed interface NotesIntent {
    data object LoadNotes : NotesIntent
    data object RefreshNotes : NotesIntent
    data object AddSampleNote : NotesIntent
    data class DeleteNote(val noteId: String) : NotesIntent
    data class NavigateToDetail(val noteId: String) : NotesIntent
}

/**
 * Side effects for the Notes List screen (one-time events)
 */
sealed interface NotesEffect {
    data class ShowError(val message: String) : NotesEffect
    data class NavigateToDetail(val noteId: String) : NotesEffect
}

/**
 * UI state for the Notes List screen
 */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val observeNotes: ObserveNotesUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val refreshNotes: RefreshNotesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NotesEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(NotesIntent.LoadNotes)
    }

    fun handleIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.LoadNotes -> loadNotes()
            is NotesIntent.RefreshNotes -> refresh()
            is NotesIntent.AddSampleNote -> addSampleNote()
            is NotesIntent.DeleteNote -> delete(intent.noteId)
            is NotesIntent.NavigateToDetail -> navigateToDetail(intent.noteId)
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            observeNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
    }

    private fun addSampleNote() {
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
                _effect.send(NotesEffect.ShowError("Failed to add note: ${e.message}"))
            }
        }
    }

    private fun delete(id: String) {
        viewModelScope.launch {
            try {
                deleteNote(id)
            } catch (e: Exception) {
                _effect.send(NotesEffect.ShowError("Failed to delete note: ${e.message}"))
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                refreshNotes()
            } catch (t: Throwable) {
                Log.v("NotesViewModel", t.toString())
                _effect.send(NotesEffect.ShowError("Failed to refresh: ${t.message}"))
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun navigateToDetail(noteId: String) {
        viewModelScope.launch {
            _effect.send(NotesEffect.NavigateToDetail(noteId))
        }
    }
}