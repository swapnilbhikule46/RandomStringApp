package com.example.randomstringapp.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.randomstringapp.data.model.RandomStringData

/**
 * Main screen for the random string generator app
 * Displays input for length, generate button, and list of generated strings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomStringScreen(
    viewModel: RandomStringViewModel = hiltViewModel()
) {
    val randomStrings by viewModel.randomStrings.collectAsState()
    val generationState by viewModel.generationState.collectAsState()

    var stringLength by remember { mutableStateOf("10") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedString: RandomStringData? by remember { mutableStateOf(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle UI state updates
    LaunchedEffect(generationState) {
        when (generationState) {
            is GenerationState.Error -> {
                snackbarHostState.showSnackbar((generationState as GenerationState.Error).message)
                viewModel.resetGenerationState()
            }

            is GenerationState.Success -> {
                viewModel.resetGenerationState()
            }

            else -> {}
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random String Generator") },
                actions = {
                    IconButton(onClick = {
                        showDeleteConfirmDialog = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete all strings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input for string length
            OutlinedTextField(
                value = stringLength,
                onValueChange = { if (it.isBlank() || it.toIntOrNull() != null) stringLength = it },
                label = { Text("String Length") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Generate button
            Button(
                onClick = {
                    stringLength.toIntOrNull()?.let { length ->
                        viewModel.generateRandomString(length)
                    } ?: run {
                        // Handle invalid input
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = generationState !is GenerationState.Loading
            ) {
                if (generationState is GenerationState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Generate Random String")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title for the list
            if (randomStrings.isNotEmpty()) {
                Text(
                    text = "Generated Strings (${randomStrings.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // List of generated strings
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = randomStrings,
                        key = { it.id }
                    ) { randomString ->
                        RandomStringCard(
                            randomString = randomString,
                            onDelete = {
                                selectedString = randomString
                                showDeleteConfirmDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No strings generated yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Confirmation dialog for deletion
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                selectedString = null
            },
            title = {
                Text(
                    text = if (selectedString != null)
                        "Delete String"
                    else
                        "Delete All Strings"
                )
            },
            text = {
                Text(
                    text = if (selectedString != null)
                        "Are you sure you want to delete this string?"
                    else
                        "Are you sure you want to delete all strings? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedString != null) {
                            viewModel.deleteRandomString(selectedString!!)
                        } else {
                            viewModel.deleteAllRandomStrings()
                        }
                        showDeleteConfirmDialog = false
                        selectedString = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        selectedString = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Card component displaying a single random string with its metadata
 */
@Composable
fun RandomStringCard(
    randomString: RandomStringData,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Random string value with scrollable container
            Text(
                text = "Value: ${randomString.value}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Length: ${randomString.length}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Created: ${randomString.getFormattedDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}
