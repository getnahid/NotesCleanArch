
package com.example.notes.domain.model

data class Note(
  val id: String,
  val title: String,
  val body: String,
  val updatedAtEpochMs: Long
)