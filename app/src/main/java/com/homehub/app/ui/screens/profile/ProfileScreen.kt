package com.homehub.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.ui.components.ErrorMessage
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.components.HomeHubHeader
import com.homehub.app.ui.theme.spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Profile screen (post-Phase 7). Read-mostly: name, email, member-since,
 * active household with a shortcut to the existing switcher. The one write
 * action is change password, via a dialog rather than a separate screen —
 * three fields and a submit button didn't earn its own destination.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSwitchHousehold: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeHubHeader(
                title = "Profile",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    ErrorMessage(
                        "Couldn't load profile: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center).padding(MaterialTheme.spacing.xl)
                    )
                }
                uiState.user != null -> {
                    ProfileContent(
                        viewModel = viewModel,
                        onSwitchHousehold = onSwitchHousehold
                    )
                }
            }
        }
    }

    if (uiState.isChangingPassword) {
        ChangePasswordDialog(
            error = uiState.passwordChangeError,
            onDismiss = viewModel::dismissChangePassword,
            onSubmit = viewModel::changePassword
        )
    }
}

@Composable
private fun ProfileContent(
    viewModel: ProfileViewModel,
    onSwitchHousehold: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        HomeHubCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.padding(start = MaterialTheme.spacing.sm)) {
                    Text(user.name?.takeIf { it.isNotBlank() } ?: "Unnamed", style = MaterialTheme.typography.titleMedium)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            memberSinceLabel(user.createdAt)?.let { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                )
            }
        }

        HomeHubCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f).padding(start = MaterialTheme.spacing.sm)) {
                    Text(viewModel.activeHouseholdName() ?: "No active household", style = MaterialTheme.typography.titleMedium)
                    viewModel.activeHouseholdRole()?.let {
                        Text(it.replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            OutlinedButton(onClick = onSwitchHousehold, modifier = Modifier.fillMaxWidth()) {
                Text("Switch household")
            }
        }

        HomeHubCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Password", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = MaterialTheme.spacing.sm))
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            OutlinedButton(onClick = viewModel::openChangePassword, modifier = Modifier.fillMaxWidth()) {
                Text("Change password")
            }
            if (uiState.passwordChangeSuccess) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Password changed",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = MaterialTheme.spacing.xs)
                    )
                }
            }
        }
    }
}

// createdAt comes over the wire as an ISO-8601 instant (Mongoose
// timestamps default). java.time works natively here without desugaring —
// minSdk is 26, and java.time has been built in since API 26.
private fun memberSinceLabel(createdAt: String?): String? {
    if (createdAt == null) return null
    return try {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        val date = Instant.parse(createdAt).atZone(ZoneId.systemDefault())
        "Member since ${formatter.format(date)}"
    } catch (e: Exception) {
        null // malformed/unexpected date shape — just omit the line rather than show something wrong
    }
}

@Composable
private fun ChangePasswordDialog(
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (current: String, new: String, confirm: String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Current password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text("New password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm new password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    ErrorMessage(error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(current, new, confirm) }) { Text("Change") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}