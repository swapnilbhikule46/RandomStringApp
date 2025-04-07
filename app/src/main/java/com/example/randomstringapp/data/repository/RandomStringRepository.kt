package com.example.randomstringapp.data.repository

import android.content.ContentResolver
import android.os.Bundle
import android.util.Log
import com.example.randomstringapp.data.model.RandomStringData
import com.example.randomstringapp.data.db.RandomStringDao
import com.example.randomstringapp.data.model.RandomTextResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
/**
 * Repository class that serves as a single source of truth for accessing and storing random strings
 */
@Singleton
class RandomStringRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val randomStringDao: RandomStringDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "RandomStringRepository"
        private const val AUTHORITY = "com.iav.contestdataprovider"
        private const val PATH = "text"
        private const val CONTENT_URI = "content://$AUTHORITY/$PATH"
        private const val DATA_COLUMN = "data"
    }

    /**
     * Fetches all stored random strings from the local database
     */
    fun getAllStrings(): Flow<List<RandomStringData>> {
        return randomStringDao.getAllStrings()
    }

    /**
     * Queries the content provider for a new random string with the specified length
     * and stores it in the local database
     *
     * @param length The maximum length of the random string to generate
     * @return The generated RandomString or null if an error occurred
     */
    suspend fun generateRandomString(length: Int): Result<RandomStringData> = withContext(Dispatchers.IO) {
        try {
            // Create the query arguments bundle with the length limit
            val queryArgs = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, length)
            }

            // Query the content provider
            val uri = android.net.Uri.parse(CONTENT_URI)
            val cursor = contentResolver.query(
                uri,
                null,
                queryArgs,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val jsonIndex = it.getColumnIndex(DATA_COLUMN)
                    if (jsonIndex != -1) {
                        val jsonString = it.getString(jsonIndex)
                        //Log.d(TAG, "Received JSON: $jsonString")

                        // Parse the JSON response
                        val response = try {
                            gson.fromJson(jsonString, RandomTextResponse::class.java)
                        } catch (e: JsonSyntaxException) {
                            //Log.e(TAG, "Failed to parse response: $e")
                            null
                        }



                        response?.let {
                            // Create RandomString entity and save to database
                            val randomString = RandomStringData(
                                value = response.randomText.value,
                                length = response.randomText.length,
                                created = response.randomText.created
                            )
                            val id = randomStringDao.insertStrings(randomString)
                            return@withContext Result.success(randomString.copy(id = id))
                        } ?: return@withContext Result.failure(
                            Exception("Failed to parse response")
                        )
                    }
                }
            }

            Result.failure(Exception("No data returned from content provider"))
        } catch (e: Exception) {
            //Log.e(TAG, "Error generating random string", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a specific random string from the local database
     */
    suspend fun deleteString(randomString: RandomStringData) = withContext(Dispatchers.IO) {
        randomStringDao.deleteString(randomString)
    }

    /**
     * Deletes all random strings from the local database
     */
    suspend fun deleteAllStrings() = withContext(Dispatchers.IO) {
        randomStringDao.deleteAllStrings()
    }
}