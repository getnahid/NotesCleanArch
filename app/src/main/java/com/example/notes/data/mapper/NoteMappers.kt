
package com.example.notes.data.mapper

import com.example.notes.data.local.NoteDto
import com.example.notes.data.remote.NoteRemoteDto
import com.example.notes.domain.model.Note

// Local DTO <-> Domain
fun NoteDto.toDomain(): Note = Note(
  id = id,
  title = title,
  body = body,
  updatedAtMs = updatedAtEpochMs
)

fun Note.toLocalDto(): NoteDto = NoteDto(
  id = id,
  title = title,
  body = body,
  updatedAtEpochMs = updatedAtMs
)

fun Note.toRemoteDto(): NoteRemoteDto = NoteRemoteDto(
  id = id,
  title = title,
  body = body,
  updatedAtEpochMs = updatedAtMs
)

// Remote DTO <-> Local DTO (for sync operations)
fun NoteRemoteDto.toLocalDto(): NoteDto = NoteDto(
  id = id,
  title = title,
  body = body,
  updatedAtEpochMs = updatedAtEpochMs
)

