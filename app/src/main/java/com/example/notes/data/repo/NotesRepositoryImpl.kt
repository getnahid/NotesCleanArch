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

        // Try to sync with remote and surface errors to callers so UI can react
        try {
            api.updateNote(note.id, note.toRemoteDto())
        } catch (e: Exception) {
            // Keep the message in a variable for reuse (logging and throwing)
            val errMsg = "Failed to sync note to server: ${e.message}"
            Log.w("NotesRepository", errMsg, e)
            // Rethrow a descriptive exception so ViewModel / UI can show an error and decide on retry
            throw Exception(errMsg, e)
        }
    }

    override suspend fun delete(id: String) {
        // Delete from local database first
        dao.delete(id)

        // Try to sync with remote and surface errors to callers
        try {
            api.deleteNote(id)
        } catch (e: Exception) {
            val errMsg = "Failed to delete note from server: ${e.message}"
            Log.w("NotesRepository", errMsg, e)
            throw Exception(errMsg, e)
        }
    }

    override suspend fun refreshFromServer() {
        // Fetch remote notes and replace local database; surface network errors to callers
        val remoteNotes = try {
            api.getNotes()
        } catch (e: Exception) {
            val errMsg = "Failed to refresh notes from server: ${e.message}"
            Log.w("NotesRepository", errMsg, e)
            throw Exception(errMsg, e)
        }

        val localDtos = remoteNotes.map { it.toLocalDto() }
        dao.replaceAll(localDtos)
    }
}
