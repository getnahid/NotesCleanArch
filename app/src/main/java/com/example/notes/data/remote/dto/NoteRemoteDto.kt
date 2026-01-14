package com.example.notes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteRemoteDto(
    val id: String,
    val title: String,
    val body: String,
    val updatedAtEpochMs: Long
)

