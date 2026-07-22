package com.homehub.app.ui.screens.household

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.HouseholdDto

/**
 * Phase 6 Step 4. Lists every household the signed-in user belongs to,
 * lets them switch which one is active (device dashboard/rules/events all
 * scope to whichever is active), create a new one (e.g. a property manager
 * adding another unit), and jump into that household's member list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdSwitcherScreen(
    onBack: () -> Unit,
    onSelected: () -> Unit,
    onManageMembers: () -> Unit,
    viewModel: HouseholdSwitcherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Households") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New household")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    if (uiState.errorMessage != null) {
                        Text(
                            uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.households, key = { it._id }) { household ->
                            HouseholdRow(
                                household = household,
                                isActive = household._id == uiState.activeHouseholdId,
                                onSelect = {
                                    viewModel.select(household)
                                    onSelected()
                                },
                                onManageMembers = {
                                    viewModel.select(household)
                                    onManageMembers()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateHouseholdDialog(
            isCreating = uiState.isCreating,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, type ->
                viewModel.createHousehold(name, type)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun HouseholdRow(
    household: HouseholdDto,
    isActive: Boolean,
    onSelect: () -> Unit,
    onManageMembers: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(household.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    household.myRole ?: "member",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onManageMembers) { Text("Members") }
            }
        }
    }
}

@Composable
private fun CreateHouseholdDialog(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val typeOptions = listOf("residential", "unit")
    var type by remember { mutableStateOf(typeOptions.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New household") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Type", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    typeOptions.forEach { option ->
                        val selected = option == type
                        TextButton(onClick = { type = option }) {
                            Text(
                                option,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && !isCreating,
                onClick = { onConfirm(name.trim(), type) }
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}