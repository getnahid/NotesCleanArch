package com.example.notes.domain.usecase

import com.example.notes.domain.model.Note
import com.example.notes.domain.repo.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNoteByIdUseCase @Inject constructor(
    private val repository: NotesRepository
) {
    operator fun invoke(id: String): Flow<Note?> =
        repository.observeNoteById(id)
}

