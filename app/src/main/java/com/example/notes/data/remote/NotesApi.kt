package com.example.notes.data.remote

import com.example.notes.data.remote.NoteRemoteDto
import retrofit2.http.*

interface NotesApi {
    @GET("notes")
    suspend fun getNotes(): List<NoteRemoteDto>

    @GET("notes/{id}")
    suspend fun getNoteById(@Path("id") id: String): NoteRemoteDto

    @POST("notes")
    suspend fun createNote(@Body note: NoteRemoteDto): NoteRemoteDto

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body note: NoteRemoteDto): NoteRemoteDto

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String)
}

