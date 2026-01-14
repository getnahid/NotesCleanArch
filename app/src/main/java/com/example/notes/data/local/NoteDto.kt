package com.example.notes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteDto(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val updatedAtEpochMs: Long
)