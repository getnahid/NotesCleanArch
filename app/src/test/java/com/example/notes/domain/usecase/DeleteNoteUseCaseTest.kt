package com.example.notes.domain.usecase

import com.example.notes.domain.repo.NotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteNoteUseCaseTest {

    private lateinit var repository: NotesRepository
    private lateinit var useCase: DeleteNoteUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteNoteUseCase(repository)
    }

    @Test
    fun givenNoteId_whenInvoked_thenCallsRepositoryDelete() = runTest {
        // Given
        val noteId = "123"
        coEvery { repository.delete(noteId) } returns Unit

        // When
        useCase(noteId)

        // Then
        coVerify(exactly = 1) { repository.delete(noteId) }
    }

    @Test
    fun givenRepositoryThrowsException_whenInvoked_thenPropagatesException() = runTest {
        // Given
        val noteId = "123"
        val expectedException = RuntimeException("Delete failed")
        coEvery { repository.delete(noteId) } throws expectedException

        // When & Then
        try {
            useCase(noteId)
            assert(false) { "Should have thrown exception" }
        } catch (e: Exception) {
            assert(e == expectedException)
        }

        coVerify(exactly = 1) { repository.delete(noteId) }
    }
}

