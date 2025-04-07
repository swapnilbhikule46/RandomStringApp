package com.example.randomstringapp.ui.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.randomstringapp.data.model.RandomStringData
import com.example.randomstringapp.data.repository.RandomStringRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RandomStringViewModelTest {

    // Executes each task synchronously using Architecture Components
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: RandomStringRepository

    private lateinit var viewModel: RandomStringViewModel

    // Test data
    private val testRandomString = RandomStringData(
        id = 1L,
        value = "testString",
        length = 10,
        created = "2024-10-01T12:00:00Z"
    )

    private val testRandomStrings = listOf(
        testRandomString,
        RandomStringData(
            id = 2L,
            value = "anotherString",
            length = 13,
            created = "2024-10-01T13:00:00Z"
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() = runBlocking {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        repository = mock(RandomStringRepository::class.java)

        // Mock suspend function correctly
        doReturn(Result.success(testRandomStrings.first()))
            .whenever(repository)
            .generateRandomString(10)

        // Mock flow function
        doReturn(flowOf(testRandomStrings))
            .whenever(repository)
            .getAllStrings()

        viewModel = RandomStringViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial generation state is Idle`() = runTest {
        // Verify that the initial state is Idle
        assertEquals(GenerationState.Idle, viewModel.generationState.value)
    }

    @Test
    fun `repository returns random strings`() = runTest {
        whenever(repository.getAllStrings()).thenReturn(flowOf(testRandomStrings))

        val collectedStrings = mutableListOf<List<RandomStringData>>()

        val job = launch {
            viewModel.randomStrings.toList(collectedStrings)
        }

        advanceUntilIdle() // let coroutine finish

        assertEquals(testRandomStrings, collectedStrings.last())

        job.cancel()
    }


    @Test
    fun `generateRandomString with valid length sets Loading then Success states`() = runTest {
        // Given
        val length = 10
        `when`(repository.generateRandomString(length)).thenReturn(Result.success(testRandomString))

        // When
        viewModel.generateRandomString(length)

        // First state should be Loading
        assertEquals(GenerationState.Loading, viewModel.generationState.value)

        // Advance coroutines to complete the operation
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(GenerationState.Success(testRandomString), viewModel.generationState.value)
    }

    @Test
    fun `generateRandomString with invalid length returns Error state`() = runTest {
        // When
        viewModel.generateRandomString(0)

        // Then
        val state = viewModel.generationState.value
        assertTrue(state is GenerationState.Error)
        assertEquals("Length must be greater than 0", (state as GenerationState.Error).message)
    }

    @Test
    fun `generateRandomString repository failure sets Error state`() = runTest {
        // Given
        val length = 10
        val errorMessage = "Network error"
        `when`(repository.generateRandomString(length)).thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.generateRandomString(length)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.generationState.value
        assertTrue(state is GenerationState.Error)
        assertEquals(errorMessage, (state as GenerationState.Error).message)
    }

    @Test
    fun `generateRandomString with unknown exception sets Error state with default message`() = runTest {
        // Given
        val length = 10
        // Return an exception with null message
        `when`(repository.generateRandomString(length)).thenReturn(Result.failure(Exception()))

        // When
        viewModel.generateRandomString(length)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.generationState.value
        assertTrue(state is GenerationState.Error)
        assertEquals("Unknown error occurred", (state as GenerationState.Error).message)
    }

    @Test
    fun `deleteRandomString calls repository method`() = runTest {
        // When
        viewModel.deleteRandomString(testRandomString)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(repository).deleteString(testRandomString)
    }

    @Test
    fun `deleteAllRandomStrings calls repository method`() = runTest {
        // When
        viewModel.deleteAllRandomStrings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(repository).deleteAllStrings()
    }

    @Test
    fun `resetGenerationState sets state to Idle`() = runTest {
        // Given - set to error state first
        viewModel.generateRandomString(0) // This will set an Error state

        // When
        viewModel.resetGenerationState()

        // Then
        assertEquals(GenerationState.Idle, viewModel.generationState.value)
    }
}