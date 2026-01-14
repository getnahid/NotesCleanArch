package com.example.notes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteDto::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}

