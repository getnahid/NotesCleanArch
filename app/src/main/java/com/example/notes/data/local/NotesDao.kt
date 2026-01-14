package com.example.notes.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes ORDER BY updatedAtEpochMs DESC")
    fun observeNotes(): Flow<List<NoteDto>>

    @Upsert
    suspend fun upsert(entity: NoteDto)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM notes")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(newItems: List<NoteDto>) {
        clear()
        newItems.forEach { upsert(it) }
    }
}

