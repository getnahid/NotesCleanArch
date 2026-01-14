
package com.example.notes.domain.repo

import com.example.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
  fun observeNotes(): Flow<List<Note>>
  suspend fun upsert(note: Note)
  suspend fun delete(id: String)
  suspend fun refreshFromServer()
}