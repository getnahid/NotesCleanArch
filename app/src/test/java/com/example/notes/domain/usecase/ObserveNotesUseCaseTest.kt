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
import org.junit.Before
import org.junit.Test

class ObserveNotesUseCaseTest {

    private lateinit var repository: NotesRepository
    private lateinit var useCase: ObserveNotesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ObserveNotesUseCase(repository)
    }

    @Test
    fun givenNotesInRepository_whenInvoked_thenReturnsFlowOfNotes() = runTest {
        // Given
        val expectedNotes = listOf(
            Note("1", "Title 1", "Body 1", 123456789L),
            Note("2", "Title 2", "Body 2", 123456790L)
        )
        every { repository.observeNotes() } returns flowOf(expectedNotes)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(expectedNotes, result[0])
        verify(exactly = 1) { repository.observeNotes() }
    }

    @Test
    fun givenEmptyRepository_whenInvoked_thenReturnsEmptyFlow() = runTest {
        // Given
        every { repository.observeNotes() } returns flowOf(emptyList())

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyList<Note>(), result[0])
        verify(exactly = 1) { repository.observeNotes() }
    }
}

