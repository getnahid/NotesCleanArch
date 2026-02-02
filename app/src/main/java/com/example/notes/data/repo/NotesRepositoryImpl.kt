package com.example.notes.data.repo

import android.util.Log
import com.example.notes.data.local.NotesDao
import com.example.notes.data.mapper.toDomain
import com.example.notes.data.mapper.toLocalDto
import com.example.notes.data.mapper.toRemoteDto
import com.example.notes.data.remote.NotesApi
import com.example.notes.domain.model.Note
import com.example.notes.domain.repo.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val dao: NotesDao,
    private val api: NotesApi
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeNotes().map { list -> list.map { it.toDomain() } }

    override fun observeNoteById(id: String): Flow<Note?> =
        dao.observeNoteById(id).map { it?.toDomain() }

    override suspend fun upsert(note: Note) {
        // Save to local database first
        dao.upsert(note.toLocalDto())

        // Try to sync with remote
        try {
            api.updateNote(note.id, note.toRemoteDto())
        } catch (_: Exception) {
            // Handle sync error - could implement retry logic or queue for later sync
            // For now, we just log and continue (offline-first approach)
        }
    }

    override suspend fun delete(id: String) {
        // Delete from local database first
        dao.delete(id)

        // Try to sync with remote
        try {
            api.deleteNote(id)
        } catch (_: Exception) {
            // Handle sync error - could implement retry logic or queue for later sync
        }
    }

    override suspend fun refreshFromServer() {
        try {
            val remoteNotes = api.getNotes()
            val localDtos = remoteNotes.map { it.toLocalDto() }
            dao.replaceAll(localDtos)
        } catch (e: Exception) {
            Log.v("", e.toString());
            // Handle network error - could throw custom exception or log
            // For now, we silently fail and keep local data
        }
    }
}

