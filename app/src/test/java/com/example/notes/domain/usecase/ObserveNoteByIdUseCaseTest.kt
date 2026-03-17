package com.example.notes.domain.usecase

import com.example.notes.domain.model.Note
import com.example.notes.domain.repo.NotesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObserveNoteByIdUseCaseTest {

    private lateinit var repository: NotesRepository
    private lateinit var useCase: ObserveNoteByIdUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ObserveNoteByIdUseCase(repository)
    }

    @Test
    fun givenNoteExists_whenInvoked_thenReturnsFlowOfNote() = runTest {
        // Given
        val noteId = "123"
        val expectedNote = Note(noteId, "Title", "Body", 123456789L)
        every { repository.observeNoteById(noteId) } returns flowOf(expectedNote)

        // When
        val result = useCase(noteId).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(expectedNote, result[0])
        verify(exactly = 1) { repository.observeNoteById(noteId) }
    }

    @Test
    fun givenNoteDoesNotExist_whenInvoked_thenReturnsNull() = runTest {
        // Given
        val noteId = "999"
        every { repository.observeNoteById(noteId) } returns flowOf(null)

        // When
        val result = useCase(noteId).toList()

        // Then
        assertEquals(1, result.size)
        assertNull(result[0])
        verify(exactly = 1) { repository.observeNoteById(noteId) }
    }
}

