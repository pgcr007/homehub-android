package com.homehub.app.ui.screens.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.ActionDto
import com.homehub.app.network.RuleDto
import com.homehub.app.ui.components.EmptyState
import com.homehub.app.ui.components.ErrorMessage
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.components.HomeHubHeader
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 2 (polish pass): shared `HomeHubHeader`, `HomeHubCard` rule
 * rows with a lightning-bolt icon standing in for "automation" the same way
 * `DeviceIcon` gives dashboard cards an icon identity, an extended FAB, and
 * a proper `EmptyState` instead of bare text. No logic changed from
 * Phase 5/6 — same ViewModel, same rule summarization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesListScreen(
    onBack: () -> Unit,
    onCreateRule: () -> Unit,
    viewModel: RulesListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<RuleDto?>(null) }

    Scaffold(
        topBar = {
            HomeHubHeader(
                title = "Rules",
                subtitle = if (uiState.rules.isNotEmpty()) {
                    "${uiState.rules.count { it.enabled }} of ${uiState.rules.size} active"
                } else null,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New rule") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onCreateRule
            )
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
            uiState.rules.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.Bolt,
                    message = "No rules yet — tap \"New rule\" to automate something",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(MaterialTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    items(uiState.rules, key = { it._id }) { rule ->
                        RuleRow(
                            rule = rule,
                            onToggle = { enabled -> viewModel.toggle(rule, enabled) },
                            onDelete = { pendingDelete = rule }
                        )
                    }
                }
            }
        }

        if (uiState.error != null) {
            ErrorMessage(
                message = uiState.error ?: "",
                modifier = Modifier.padding(MaterialTheme.spacing.lg)
            )
        }
    }

    val ruleToDelete = pendingDelete
    if (ruleToDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${ruleToDelete.name}\"?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(ruleToDelete)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RuleRow(
    rule: RuleDto,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    HomeHubCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RuleIcon(enabled = rule.enabled)
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(rule.name, style = MaterialTheme.typography.bodyLarge)
                Text(summarize(rule), style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = rule.enabled, onCheckedChange = onToggle)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

/** Lightning-bolt avatar mirroring DeviceIcon's tinted-circle treatment —
 *  dimmed when the rule is disabled so a glance down the list shows which
 *  automations are actually live. */
@Composable
private fun RuleIcon(enabled: Boolean) {
    val tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Bolt, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

private fun summarize(rule: RuleDto): String {
    val trigger = rule.trigger
    val triggerDesc = if (trigger.operator == "changed") {
        "${trigger.device.name} \u00b7 ${trigger.capability} changes"
    } else {
        "${trigger.device.name} \u00b7 ${trigger.capability} ${trigger.operator} ${trigger.value}"
    }
    val actionsDesc = rule.actions.joinToString(", ") { describeAction(it) }
    return "When $triggerDesc \u2192 $actionsDesc"
}

private fun describeAction(action: ActionDto): String = if (action.type == "device_command") {
    "${action.device?.name ?: "device"} \u00b7 ${action.capability} = ${action.value}"
} else {
    "notify: ${action.message}"
}