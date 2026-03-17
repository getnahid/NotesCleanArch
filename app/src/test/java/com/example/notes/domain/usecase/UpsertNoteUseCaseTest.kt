package com.example.notes.domain.usecase

import com.example.notes.domain.model.Note
import com.example.notes.domain.repo.NotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpsertNoteUseCaseTest {

    private lateinit var repository: NotesRepository
    private lateinit var useCase: UpsertNoteUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UpsertNoteUseCase(repository)
    }

    @Test
    fun givenNote_whenInvoked_thenCallsRepositoryUpsert() = runTest {
        // Given
        val note = Note("1", "Test Title", "Test Body", 123456789L)
        coEvery { repository.upsert(note) } returns Unit

        // When
        useCase(note)

        // Then
        coVerify(exactly = 1) { repository.upsert(note) }
    }

    @Test
    fun givenRepositoryThrowsException_whenInvoked_thenPropagatesException() = runTest {
        // Given
        val note = Note("1", "Test Title", "Test Body", 123456789L)
        val expectedException = RuntimeException("Database error")
        coEvery { repository.upsert(note) } throws expectedException

        // When & Then
        try {
            useCase(note)
            assert(false) { "Should have thrown exception" }
        } catch (e: Exception) {
            assert(e == expectedException)
        }

        coVerify(exactly = 1) { repository.upsert(note) }
    }
}

