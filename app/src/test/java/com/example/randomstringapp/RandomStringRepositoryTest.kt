package com.example.randomstringapp.data.repository

import android.content.ContentResolver
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import com.example.randomstringapp.data.db.RandomStringDao
import com.example.randomstringapp.data.model.RandomStringData
import com.example.randomstringapp.data.model.RandomTextResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.capture
import java.lang.Exception

class RandomStringRepositoryTest {

    // Mocks
    private lateinit var contentResolver: ContentResolver
    private lateinit var randomStringDao: RandomStringDao
    private lateinit var gson: Gson

    // System under test
    private lateinit var repository: RandomStringRepository

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

    private val testJsonResponse = """
        {
            "randomText": {
                "value": "testString",
                "length": 10,
                "created": "2024-10-01T12:00:00Z"
            }
        }
    """.trimIndent()

    @Before
    fun setup() {
        contentResolver = mock(ContentResolver::class.java)
        randomStringDao = mock(RandomStringDao::class.java)
        gson = Gson()

        repository = RandomStringRepository(contentResolver, randomStringDao, gson)

        // Setup default mock behavior
        `when`(randomStringDao.getAllStrings()).thenReturn(flowOf(testRandomStrings))
    }

    @Test
    fun `getAllStrings returns flow from DAO`() = runBlocking {
        // When
        val result = repository.getAllStrings()

        // Then
        verify(randomStringDao).getAllStrings()
        assertEquals(testRandomStrings, result.single())
    }

    @Test
    fun `generateRandomString queries content provider with correct params`() = runTest {
        // Given
        val cursor = createMockCursor(testJsonResponse)
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        `when`(contentResolver.query(
            any(Uri::class.java),
            isNull(),
            capture(bundleCaptor),
            isNull()
        )).thenReturn(cursor)

        val stringLength = 10
        `when`(randomStringDao.insertStrings(any())).thenReturn(1L)

        // When
        val result = repository.generateRandomString(stringLength)

        // Then
        verify(contentResolver).query(
            any(Uri::class.java),
            isNull(),
            capture(bundleCaptor),
            isNull()
        )

        // Verify the length parameter was set correctly
        assertEquals(stringLength, bundleCaptor.value.getInt(ContentResolver.QUERY_ARG_LIMIT))

        // Verify the result
        assertTrue(result.isSuccess)
        assertEquals(testRandomString.value, result.getOrNull()?.value)
        assertEquals(testRandomString.length, result.getOrNull()?.length)
        assertEquals(testRandomString.created, result.getOrNull()?.created)
    }

    @Test
    fun `generateRandomString returns failure when content provider returns no data`() = runTest {
        // Given
        val emptyCursor = mock(Cursor::class.java)
        `when`(emptyCursor.moveToFirst()).thenReturn(false)

        `when`(contentResolver.query(
            any(Uri::class.java),
            isNull(),
            any(),
            isNull()
        )).thenReturn(emptyCursor)

        // When
        val result = repository.generateRandomString(10)

        // Then
        assertTrue(result.isFailure)
        assertEquals("No data returned from content provider", result.exceptionOrNull()?.message)
    }


    @Test
    fun `generateRandomString returns failure when JSON parsing fails`() = runTest {
        // Given
        val invalidJsonCursor = createMockCursor("invalid json")
        `when`(contentResolver.query(
            any(Uri::class.java),
            isNull(),
            any(),
            isNull()
        )).thenReturn(invalidJsonCursor)

        // When
        val result = repository.generateRandomString(10)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed to parse response", result.exceptionOrNull()?.message)
    }

    @Test
    fun `generateRandomString returns failure when content provider throws exception`() = runTest {
        // Given
        `when`(contentResolver.query(
            any(Uri::class.java),
            isNull(),
            any(),
            isNull()
        )).thenThrow(RuntimeException("Test exception"))

        // When
        val result = repository.generateRandomString(10)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Test exception", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteString calls DAO with correct parameter`() = runTest {
        // When
        repository.deleteString(testRandomString)

        // Then
        verify(randomStringDao).deleteString(testRandomString)
        verifyNoMoreInteractions(randomStringDao)
    }

    @Test
    fun `deleteAllStrings calls DAO method`() = runTest {
        // When
        repository.deleteAllStrings()

        // Then
        verify(randomStringDao).deleteAllStrings()
        verifyNoMoreInteractions(randomStringDao)
    }

    // Helper methods to create mock cursors
    private fun createMockCursor(jsonData: String): Cursor {
        val cursor = MatrixCursor(arrayOf("data"))
        cursor.addRow(arrayOf(jsonData))
        return cursor
    }

    private fun createEmptyCursor(): Cursor {
        return MatrixCursor(arrayOf("data"))
    }
}