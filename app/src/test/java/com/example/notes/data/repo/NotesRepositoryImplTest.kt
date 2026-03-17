package com.example.notes.data.repo

import com.example.notes.data.local.NoteDto
import com.example.notes.data.local.NotesDao
import com.example.notes.data.remote.NotesApi
import com.example.notes.data.remote.NoteRemoteDto
import com.example.notes.domain.model.Note
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NotesRepositoryImplTest {

    private lateinit var dao: NotesDao
    private lateinit var api: NotesApi
    private lateinit var repository: NotesRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        api = mockk(relaxed = true)
        repository = NotesRepositoryImpl(dao, api)
    }

    @Test
    fun givenNotesInDao_whenObserveNotes_thenReturnsMappedNotes() = runTest {
        // Given
        val localNotes = listOf(
            NoteDto("1", "Title 1", "Body 1", 123456789L),
            NoteDto("2", "Title 2", "Body 2", 123456790L)
        )
        every { dao.observeNotes() } returns flowOf(localNotes)

        // When
        val result = repository.observeNotes().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
        assertEquals("1", result[0][0].id)
        assertEquals("Title 1", result[0][0].title)
        verify(exactly = 1) { dao.observeNotes() }
    }

    @Test
    fun givenNoteInDao_whenObserveNoteById_thenReturnsMappedNote() = runTest {
        // Given
        val noteId = "123"
        val localNote = NoteDto(noteId, "Title", "Body", 123456789L)
        every { dao.observeNoteById(noteId) } returns flowOf(localNote)

        // When
        val result = repository.observeNoteById(noteId).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(noteId, result[0]?.id)
        assertEquals("Title", result[0]?.title)
        verify(exactly = 1) { dao.observeNoteById(noteId) }
    }

    @Test
    fun givenNoteDoesNotExist_whenObserveNoteById_thenReturnsNull() = runTest {
        // Given
        val noteId = "999"
        every { dao.observeNoteById(noteId) } returns flowOf(null)

        // When
        val result = repository.observeNoteById(noteId).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(null, result[0])
        verify(exactly = 1) { dao.observeNoteById(noteId) }
    }

    @Test
    fun givenNote_whenUpsert_thenSavesToLocalDaoAndSyncsWithRemoteApi() = runTest {
        // Given
        val note = Note("1", "Title", "Body", 123456789L)
        coEvery { dao.upsert(any()) } returns Unit
        coEvery { api.updateNote(any(), any()) } returns mockk()

        // When
        repository.upsert(note)

        // Then
        coVerify(exactly = 1) { dao.upsert(any()) }
        coVerify(exactly = 1) { api.updateNote(note.id, any()) }
    }

    @Test
    fun givenRemoteSyncFails_whenUpsert_thenStillSavesToLocalDao() = runTest {
        // Given
        val note = Note("1", "Title", "Body", 123456789L)
        coEvery { dao.upsert(any()) } returns Unit
        coEvery { api.updateNote(any(), any()) } throws RuntimeException("Network error")

        // When
        repository.upsert(note)

        // Then
        coVerify(exactly = 1) { dao.upsert(any()) }
        coVerify(exactly = 1) { api.updateNote(note.id, any()) }
    }

    @Test
    fun givenNoteId_whenDelete_thenRemovesFromLocalDaoAndSyncsWithRemoteApi() = runTest {
        // Given
        val noteId = "123"
        coEvery { dao.delete(noteId) } returns Unit
        coEvery { api.deleteNote(noteId) } returns mockk()

        // When
        repository.delete(noteId)

        // Then
        coVerify(exactly = 1) { dao.delete(noteId) }
        coVerify(exactly = 1) { api.deleteNote(noteId) }
    }

    @Test
    fun givenRemoteSyncFails_whenDelete_thenStillRemovesFromLocalDao() = runTest {
        // Given
        val noteId = "123"
        coEvery { dao.delete(noteId) } returns Unit
        coEvery { api.deleteNote(noteId) } throws RuntimeException("Network error")

        // When
        repository.delete(noteId)

        // Then
        coVerify(exactly = 1) { dao.delete(noteId) }
        coVerify(exactly = 1) { api.deleteNote(noteId) }
    }

    @Test
    fun givenRemoteNotes_whenRefreshFromServer_thenFetchesFromApiAndSavesToLocalDao() = runTest {
        // Given
        val remoteNotes = listOf(
            NoteRemoteDto("1", "Title 1", "Body 1", 123456789L),
            NoteRemoteDto("2", "Title 2", "Body 2", 123456790L)
        )
        coEvery { api.getNotes() } returns remoteNotes
        coEvery { dao.replaceAll(any()) } returns Unit

        // When
        repository.refreshFromServer()

        // Then
        coVerify(exactly = 1) { api.getNotes() }
        coVerify(exactly = 1) { dao.replaceAll(any()) }
    }

    @Test
    fun givenApiCallFails_whenRefreshFromServer_thenDoesNotCrash() = runTest {
        // Given
        coEvery { api.getNotes() } throws RuntimeException("Network error")

        // When
        repository.refreshFromServer()

        // Then
        coVerify(exactly = 1) { api.getNotes() }
        coVerify(exactly = 0) { dao.replaceAll(any()) }
    }
}

