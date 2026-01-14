
package com.example.notes.domain.usecase

import com.example.notes.domain.repo.NotesRepository
import javax.inject.Inject

class ObserveNotesUseCase @Inject constructor(
  private val repo: NotesRepository
) {
  operator fun invoke() = repo.observeNotes()
}

