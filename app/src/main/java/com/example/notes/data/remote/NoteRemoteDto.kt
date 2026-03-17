package com.example.notes.data.remote

data class NoteRemoteDto(
    val id: String,
    val title: String,
    val body: String,
    val updatedAtEpochMs: Long
)