package com.example.notes.domain.usecase

import com.example.notes.domain.repo.NotesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RefreshNotesUseCaseTest {

    private lateinit var repository: NotesRepository
    private lateinit var useCase: RefreshNotesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = RefreshNotesUseCase(repository)
    }

    @Test
    fun givenRepository_whenInvoked_thenCallsRefreshFromServer() = runTest {
        // Given
        coEvery { repository.refreshFromServer() } returns Unit

        // When
        useCase()

        // Then
        coVerify(exactly = 1) { repository.refreshFromServer() }
    }

    @Test
    fun givenRepositoryThrowsException_whenInvoked_thenPropagatesException() = runTest {
        // Given
        val expectedException = RuntimeException("Network error")
        coEvery { repository.refreshFromServer() } throws expectedException

        // When & Then
        try {
            useCase()
            assert(false) { "Should have thrown exception" }
        } catch (e: Exception) {
            assert(e == expectedException)
        }

        coVerify(exactly = 1) { repository.refreshFromServer() }
    }
}

