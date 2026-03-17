package com.example.notes.presenter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.notes.domain.model.Note
import com.example.notes.domain.usecase.DeleteNoteUseCase
import com.example.notes.domain.usecase.ObserveNoteByIdUseCase
import com.example.notes.domain.usecase.UpsertNoteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getNoteByIdUseCase: ObserveNoteByIdUseCase
    private lateinit var upsertNoteUseCase: UpsertNoteUseCase
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase
    private lateinit var viewModel: NoteDetailViewModel

    private val testNoteId = "123"
    private val testNote = Note(testNoteId, "Test Title", "Test Body", 123456789L)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle(mapOf("noteId" to testNoteId))
        getNoteByIdUseCase = mockk()
        upsertNoteUseCase = mockk()
        deleteNoteUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenNoteExists_whenInit_thenLoadsNoteById() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(testNote)

        // When
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )
        advanceUntilIdle()

        // Then
        assertEquals(testNote, viewModel.uiState.value.note)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun givenNoteExists_whenInit_thenSetsLoadingStateCorrectly() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(testNote)

        // When
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )

        // Then - Initially loading
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        // Then - Loading complete
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun givenUseCaseThrowsException_whenLoadNote_thenEmitsErrorEvent() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(testNote)
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )
        advanceUntilIdle()

        // Re-setup with error
        every { getNoteByIdUseCase(testNoteId) } throws RuntimeException("Error loading note")

        // When & Then
        viewModel.events.test {
            viewModel.loadNote()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NoteDetailUiEvent.ShowError)
            assertTrue((event as NoteDetailUiEvent.ShowError).message.contains("Failed to load note"))
        }
    }

    @Test
    fun givenUpdatedTitleAndBody_whenUpdateNote_thenCallsUpsertUseCaseWithUpdatedNote() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(testNote)
        coEvery { upsertNoteUseCase(any()) } returns Unit
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )
        advanceUntilIdle()

        val newTitle = "Updated Title"
        val newBody = "Updated Body"

        // When
        viewModel.updateNote(newTitle, newBody)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            upsertNoteUseCase(match {
                it.id == testNoteId && it.title == newTitle && it.body == newBody
            })
        }
    }

    @Test
    fun givenNoteIsNull_whenUpdateNote_thenEmitsErrorEvent() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(null)
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )
        advanceUntilIdle()

        // When & Then
        viewModel.events.test {
            viewModel.updateNote("Title", "Body")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NoteDetailUiEvent.ShowError)
            assertEquals("Note not found", (event as NoteDetailUiEvent.ShowError).message)
        }
    }

    @Test
    fun givenUseCaseThrowsException_whenUpdateNote_thenEmitsErrorEvent() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(testNote)
        coEvery { upsertNoteUseCase(any()) } throws RuntimeException("Update failed")
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )
        advanceUntilIdle()

        // When & Then
        viewModel.events.test {
            viewModel.updateNote("New Title", "New Body")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NoteDetailUiEvent.ShowError)
            assertTrue((event as NoteDetailUiEvent.ShowError).message.contains("Failed to update note"))
        }
    }

    @Test
    fun givenNote_whenDeleteNote_thenCallsDeleteUseCaseAndEmitsNavigateBackEvent() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(testNote)
        coEvery { deleteNoteUseCase(testNoteId) } returns Unit
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )
        advanceUntilIdle()

        // When & Then
        viewModel.events.test {
            viewModel.deleteNote()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NoteDetailUiEvent.NavigateBack)
            coVerify(exactly = 1) { deleteNoteUseCase(testNoteId) }
        }
    }

    @Test
    fun givenUseCaseThrowsException_whenDeleteNote_thenEmitsErrorEvent() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(testNote)
        coEvery { deleteNoteUseCase(testNoteId) } throws RuntimeException("Delete failed")
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )
        advanceUntilIdle()

        // When & Then
        viewModel.events.test {
            viewModel.deleteNote()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NoteDetailUiEvent.ShowError)
            assertTrue((event as NoteDetailUiEvent.ShowError).message.contains("Failed to delete note"))
        }
    }

    @Test
    fun givenNullNoteFromUseCase_whenLoadNote_thenHandlesNullNoteCorrectly() = runTest {
        // Given
        every { getNoteByIdUseCase(testNoteId) } returns flowOf(null)

        // When
        viewModel = NoteDetailViewModel(
            savedStateHandle,
            getNoteByIdUseCase,
            upsertNoteUseCase,
            deleteNoteUseCase
        )
        advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.note)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}

