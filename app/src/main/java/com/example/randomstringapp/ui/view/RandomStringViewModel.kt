package com.example.randomstringapp.ui.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.randomstringapp.data.model.RandomStringData
import com.example.randomstringapp.data.repository.RandomStringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state representing the current state of the RandomString generation
 */
sealed class GenerationState {
    object Idle : GenerationState()
    object Loading : GenerationState()
    data class Success(val randomString: RandomStringData) : GenerationState()
    data class Error(val message: String) : GenerationState()
}

/**
 * ViewModel for the random string generator screen
 */
@HiltViewModel
class RandomStringViewModel @Inject constructor(
    private val repository: RandomStringRepository
) : ViewModel() {

    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    // Observe all stored random strings
    val randomStrings = repository.getAllStrings()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Generates a new random string with the specified length
     *
     * @param length The maximum length of the random string to generate
     */
    fun generateRandomString(length: Int) {
        if (length <= 0) {
            _generationState.value = GenerationState.Error("Length must be greater than 0")
            return
        }

        _generationState.value = GenerationState.Loading

        viewModelScope.launch {
            repository.generateRandomString(length)
                .onSuccess { randomString ->
                    _generationState.value = GenerationState.Success(randomString)
                }
                .onFailure { error ->
                    _generationState.value = GenerationState.Error(
                        error.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    /**
     * Deletes a specific random string
     */
    fun deleteRandomString(randomString: RandomStringData) {
        viewModelScope.launch {
            repository.deleteString(randomString)
        }
    }

    /**
     * Deletes all random strings
     */
    fun deleteAllRandomStrings() {
        viewModelScope.launch {
            repository.deleteAllStrings()
        }
    }

    /**
     * Resets the generation state to idle
     * Used to clear error messages or success states
     */
    fun resetGenerationState() {
        _generationState.value = GenerationState.Idle
    }
}