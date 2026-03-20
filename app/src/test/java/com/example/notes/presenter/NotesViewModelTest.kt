package com.example.notes.presenter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.notes.domain.model.Note
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.ObserveNotesUseCase
import com.example.notes.domain.usecase.RefreshNotesUseCase
import com.example.notes.domain.usecase.UpsertNoteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var observeNotesUseCase: ObserveNotesUseCase
    private lateinit var upsertNoteUseCase: UpsertNoteUseCase
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase
    private lateinit var refreshNotesUseCase: RefreshNotesUseCase
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        observeNotesUseCase = mockk()
        upsertNoteUseCase = mockk()
        deleteNoteUseCase = mockk()
        refreshNotesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenNotesInRepository_whenInit_thenLoadsNotesFromUseCase() = runTest {
        // Given
        val notes = listOf(
            Note("1", "Title 1", "Body 1", 123456789L),
            Note("2", "Title 2", "Body 2", 123456790L)
        )
        every { observeNotesUseCase() } returns flowOf(notes)

        // When
        viewModel = NotesViewModel(
            observeNotesUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase,
            refreshNotesUseCase
        )
        advanceUntilIdle()

        // Then
        assertEquals(notes, viewModel.uiState.value.notes)
        verify(exactly = 1) { observeNotesUseCase() }
    }

    @Test
    fun givenNoteId_whenDeleteNote_thenCallsDeleteUseCaseWithCorrectId() = runTest {
        // Given
        val noteId = "123"
        every { observeNotesUseCase() } returns flowOf(emptyList())
        coEvery { deleteNoteUseCase(noteId) } returns Unit
        viewModel = NotesViewModel(
            observeNotesUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase,
            refreshNotesUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.deleteNote(noteId)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { deleteNoteUseCase(noteId) }
    }

    @Test
    fun givenUseCaseThrowsException_whenDeleteNote_thenEmitsErrorEvent() = runTest {
        // Given
        val noteId = "123"
        every { observeNotesUseCase() } returns flowOf(emptyList())
        coEvery { deleteNoteUseCase(noteId) } throws RuntimeException("Error deleting note")
        viewModel = NotesViewModel(
            observeNotesUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase,
            refreshNotesUseCase
        )
        advanceUntilIdle()

        // When & Then
        viewModel.events.test {
            viewModel.deleteNote(noteId)
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NotesUiEvent.ShowError)
            assertTrue((event as NotesUiEvent.ShowError).message.contains("Failed to delete note"))
        }
    }

    @Test
    fun givenViewModel_whenAddSampleNote_thenCallsUpsertUseCase() = runTest {
        // Given
        every { observeNotesUseCase() } returns flowOf(emptyList())
        coEvery { upsertNoteUseCase(any()) } returns Unit
        viewModel = NotesViewModel(
            observeNotesUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase,
            refreshNotesUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.addSampleNote()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { upsertNoteUseCase(any()) }
    }

    @Test
    fun givenUseCaseThrowsException_whenAddSampleNote_thenEmitsErrorEvent() = runTest {
        // Given
        every { observeNotesUseCase() } returns flowOf(emptyList())
        coEvery { upsertNoteUseCase(any()) } throws RuntimeException("Error adding note")
        viewModel = NotesViewModel(
            observeNotesUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase,
            refreshNotesUseCase
        )
        advanceUntilIdle()

        // When & Then
        viewModel.events.test {
            viewModel.addSampleNote()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NotesUiEvent.ShowError)
            assertTrue((event as NotesUiEvent.ShowError).message.contains("Failed to add note"))
        }
    }

    @Test
    fun givenViewModel_whenRefreshNotes_thenCallsRefreshUseCaseAndUpdatesLoadingState() = runTest {
        // Given
        every { observeNotesUseCase() } returns flowOf(emptyList())
        coEvery { refreshNotesUseCase() } returns Unit
        viewModel = NotesViewModel(
            observeNotesUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase,
            refreshNotesUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.refreshNotes()
        testScheduler.runCurrent() // Process the immediate state change

        // Then - should be loading (or already done if fast)
        // Just verify the use case was called
        advanceUntilIdle()

        // Then - loading should be done
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { refreshNotesUseCase() }
    }

    @Test
    fun givenUseCaseThrowsException_whenRefreshNotes_thenEmitsErrorEvent() = runTest {
        // Given
        every { observeNotesUseCase() } returns flowOf(emptyList())
        coEvery { refreshNotesUseCase() } throws RuntimeException("Network error")
        viewModel = NotesViewModel(
            observeNotesUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase,
            refreshNotesUseCase
        )
        advanceUntilIdle()

        // When & Then
        viewModel.events.test {
            viewModel.refreshNotes()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NotesUiEvent.ShowError)
            assertTrue((event as NotesUiEvent.ShowError).message.contains("Failed to refresh"))
            assertFalse(viewModel.uiState.value.isLoading)
        }
    }

    @Test
    fun givenNotesInUseCase_whenLoadNotes_thenUpdatesUiStateWithNotes() = runTest {
        // Given
        val notes = listOf(Note("1", "Title", "Body", 123456789L))
        every { observeNotesUseCase() } returns flowOf(notes)
        viewModel = NotesViewModel(
            observeNotesUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase,
            refreshNotesUseCase
        )

        // When
        advanceUntilIdle()

        // Then
        assertEquals(notes, viewModel.uiState.value.notes)
    }
}

