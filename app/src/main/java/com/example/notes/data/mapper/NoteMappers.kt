
package com.example.notes.data.mapper

import com.example.notes.data.local.NoteDto
import com.example.notes.data.remote.dto.NoteRemoteDto
import com.example.notes.domain.model.Note

// Local DTO <-> Domain
fun NoteDto.toDomain(): Note = Note(
  id = id,
  title = title,
  body = body,
  updatedAtEpochMs = updatedAtEpochMs
)

fun Note.toLocalDto(): NoteDto = NoteDto(
  id = id,
  title = title,
  body = body,
  updatedAtEpochMs = updatedAtEpochMs
)

// Remote DTO <-> Domain
fun NoteRemoteDto.toDomain(): Note = Note(
  id = id,
  title = title,
  body = body,
  updatedAtEpochMs = updatedAtEpochMs
)

fun Note.toRemoteDto(): NoteRemoteDto = NoteRemoteDto(
  id = id,
  title = title,
  body = body,
  updatedAtEpochMs = updatedAtEpochMs
)

// Remote DTO <-> Local DTO (for sync operations)
fun NoteRemoteDto.toLocalDto(): NoteDto = NoteDto(
  id = id,
  title = title,
  body = body,
  updatedAtEpochMs = updatedAtEpochMs
)

// Deprecated - keeping for backward compatibility, will be removed
@Deprecated("Use toLocalDto() instead", ReplaceWith("toLocalDto()"))
fun Note.toEntity(): NoteDto = toLocalDto()

