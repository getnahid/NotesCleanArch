
package com.example.notes.domain.usecase

import com.example.notes.domain.repo.NotesRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
  private val repo: NotesRepository
) {
  suspend operator fun invoke(id: String) = repo.delete(id)
}

