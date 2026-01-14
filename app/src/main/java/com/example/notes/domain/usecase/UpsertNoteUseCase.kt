
package com.example.notes.domain.usecase

import com.example.notes.domain.model.Note
import com.example.notes.domain.repo.NotesRepository
import javax.inject.Inject

class UpsertNoteUseCase @Inject constructor(
  private val repo: NotesRepository
) {
  suspend operator fun invoke(note: Note) = repo.upsert(note)
}

