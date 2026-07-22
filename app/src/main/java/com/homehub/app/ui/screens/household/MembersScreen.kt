package com.homehub.app.ui.screens.household

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
import androidx.compose.material.icons.filled.Delete
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
import com.homehub.app.network.HouseholdMemberDto
import com.homehub.app.network.UserHolder

/**
 * Phase 6 Step 4. Shows the active household's members + roles. Add/remove
 * controls only render for manager+ (mirrors requireRole('owner',
 * 'manager') on the backend's member routes) — this is UI convenience
 * only, not the actual enforcement; the backend re-checks and 403s
 * regardless of what this screen shows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    onBack: () -> Unit,
    viewModel: MembersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingRemove by remember { mutableStateOf<HouseholdMemberDto?>(null) }

    val canManageMembers = uiState.myRole == "owner" || uiState.myRole == "manager"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.household?.name ?: "Members") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canManageMembers) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add member")
                }
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
                    if (uiState.error != null) {
                        Text(
                            uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.household?.members ?: emptyList(), key = { it.user._id }) { member ->
                            MemberRow(
                                member = member,
                                // Mirrors requireHousehold.js's own removal
                                // rule: an owner can remove anyone; a
                                // manager can remove managers/members but
                                // not another owner. Also never show it on
                                // your own row — self-removal isn't a
                                // supported/tested flow here.
                                canRemove = canManageMembers &&
                                        member.user._id != UserHolder.userId &&
                                        (member.role != "owner" || uiState.myRole == "owner"),
                                onRemove = { pendingRemove = member }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemberDialog(
            allowOwnerRole = uiState.myRole == "owner",
            isSubmitting = uiState.isInviting,
            onDismiss = { showAddDialog = false },
            onConfirm = { email, role ->
                viewModel.addMember(email, role)
                showAddDialog = false
            }
        )
    }

    val memberToRemove = pendingRemove
    if (memberToRemove != null) {
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text("Remove ${memberToRemove.user.name ?: memberToRemove.user.email}?") },
            text = { Text("They'll lose access to this household immediately.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeMember(memberToRemove)
                    pendingRemove = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemove = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MemberRow(
    member: HouseholdMemberDto,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(member.user.name ?: member.user.email, style = MaterialTheme.typography.bodyLarge)
                Text(member.role, style = MaterialTheme.typography.bodySmall)
            }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove member")
                }
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    allowOwnerRole: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (email: String, role: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    // Mirrors requireHousehold.js: only an owner can grant the 'owner' role
    // to someone else.
    val roleOptions = if (allowOwnerRole) listOf("member", "manager", "owner") else listOf("member", "manager")
    var role by remember { mutableStateOf(roleOptions.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add member") },
        text = {
            Column {
                Text(
                    "They need an existing HomeHub account — this adds them by email, it doesn't send an invite.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
                Text("Role", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    roleOptions.forEach { option ->
                        val selected = option == role
                        TextButton(onClick = { role = option }) {
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
                enabled = email.isNotBlank() && !isSubmitting,
                onClick = { onConfirm(email.trim(), role) }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}