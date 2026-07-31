package com.homehub.app.ui.screens.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.CLAUSE_OPERATORS
import com.homehub.app.network.DeviceDto
import com.homehub.app.ui.components.ErrorMessage
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.components.HomeHubHeader
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 2 (polish pass): shared `HomeHubHeader`; clause/action rows
 * and the conflict-warning callout now use `HomeHubCard` instead of a bare
 * `Card`. No logic changed from Phase 5/6 — same ViewModel, same rule
 * validation/conflict-check flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRuleScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateRuleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeHubHeader(
                title = "New rule",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.created) {
            CreatedConfirmation(warnings = uiState.warnings, onDone = onDone, modifier = Modifier.padding(padding))
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                RulePreviewCard(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm)
                )
                RuleForm(uiState = uiState, viewModel = viewModel, modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Live plain-language readout of the rule being assembled — mirrors the
 * summary line shown on each row in RulesListScreen, but computed from the
 * in-progress form state instead of a saved RuleDto, and rendered as it's
 * built rather than only after creation. Kept outside the scrollable
 * RuleForm so it stays pinned at the top while the picker fields below it
 * scroll — the whole point is to see the sentence update without losing
 * your place in the form.
 */
@Composable
private fun RulePreviewCard(
    uiState: CreateRuleUiState,
    modifier: Modifier = Modifier
) {
    val preview = buildRulePreview(uiState)
    HomeHubCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Filled.Bolt,
                contentDescription = null,
                tint = if (preview != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                preview ?: "Fill in a trigger and an action to see a plain-language preview here",
                style = MaterialTheme.typography.bodyMedium,
                color = if (preview != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = MaterialTheme.spacing.sm)
            )
        }
    }
}

private fun buildRulePreview(uiState: CreateRuleUiState): String? {
    val triggerText = clausePreviewText(uiState.trigger, uiState.devices) ?: return null

    // A half-filled condition (device picked, capability not yet) shouldn't
    // block the trigger+action preview from showing — just leave it out
    // until it's complete, same as an empty conditions list.
    val conditionTexts = uiState.conditions.mapNotNull { clausePreviewText(it, uiState.devices) }

    val actionTexts = uiState.actions.mapNotNull { actionPreviewText(it, uiState.devices) }
    if (actionTexts.isEmpty()) return null

    val conditionPart = if (conditionTexts.isNotEmpty()) {
        " and " + conditionTexts.joinToString(" and ")
    } else ""

    return "When $triggerText$conditionPart, ${actionTexts.joinToString(", and ")}"
}

private fun clausePreviewText(form: ClauseForm, devices: List<DeviceDto>): String? {
    val device = devices.find { it._id == form.deviceId } ?: return null
    if (form.capability.isBlank()) return null
    if (form.operator != "changed" && form.value.isBlank()) return null

    return if (form.operator == "changed") {
        "${device.name}'s ${form.capability} changes"
    } else {
        val opLabel = CLAUSE_OPERATORS.find { it.first == form.operator }?.second ?: form.operator
        "${device.name}'s ${form.capability} $opLabel ${form.value}"
    }
}

private fun actionPreviewText(form: ActionForm, devices: List<DeviceDto>): String? {
    return if (form.type == "device_command") {
        val device = devices.find { it._id == form.deviceId } ?: return null
        if (form.capability.isBlank() || form.value.isBlank()) return null
        "set ${device.name}'s ${form.capability} to ${form.value}"
    } else {
        if (form.message.isBlank()) return null
        "send a notification saying \"${form.message.trim()}\""
    }
}

@Composable
private fun RuleForm(
    uiState: CreateRuleUiState,
    viewModel: CreateRuleViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl)
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Rule name") },
            placeholder = { Text("e.g. Motion turns on hallway light") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Rule templates (post-Phase 7): one tap fills in the trigger/action
        // shape for a common pattern, leaving only device selection — see
        // CreateRuleViewModel.applyTemplate() for why only these three
        // patterns are offered (not "time-based" or "offline-alert" from
        // the original brainstorm, neither of which the rule engine
        // actually supports today).
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            Text("Start from a template (optional)", style = MaterialTheme.typography.labelLarge)
            RULE_TEMPLATES.forEach { template ->
                OutlinedButton(
                    onClick = { viewModel.applyTemplate(template) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(template.label, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            template.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Text("Trigger", style = MaterialTheme.typography.titleMedium)
        ClauseEditor(
            devices = uiState.devices,
            form = uiState.trigger,
            onChange = viewModel::updateTrigger
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Conditions (optional)", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = viewModel::addCondition) { Text("+ Add") }
        }
        uiState.conditions.forEachIndexed { index, form ->
            ClauseEditor(
                devices = uiState.devices,
                form = form,
                onChange = { viewModel.updateCondition(index, it) },
                trailing = {
                    TextButton(onClick = { viewModel.removeCondition(index) }) { Text("Remove") }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Actions", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = viewModel::addAction) { Text("+ Add") }
        }
        uiState.actions.forEachIndexed { index, form ->
            ActionEditor(
                devices = uiState.devices,
                form = form,
                onChange = { viewModel.updateAction(index, it) },
                trailing = if (uiState.actions.size > 1) {
                    { TextButton(onClick = { viewModel.removeAction(index) }) { Text("Remove") } }
                } else null
            )
        }

        if (uiState.error != null) {
            ErrorMessage(uiState.error)
        }

        Button(
            onClick = viewModel::submit,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(end = MaterialTheme.spacing.sm))
            }
            Text("Create Rule")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClauseEditor(
    devices: List<DeviceDto>,
    form: ClauseForm,
    onChange: (ClauseForm) -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    val selectedDevice = devices.find { it._id == form.deviceId }

    HomeHubCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
        if (trailing != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                trailing()
            }
        }
        LabeledDropdown(
            label = "Device",
            selectedLabel = selectedDevice?.name ?: "Select a device",
            options = devices,
            optionLabel = { it.name },
            // Keep the current capability if the newly picked device still
            // supports it (e.g. a template pre-filled "motion" before any
            // device was chosen) — only clear it if it's no longer valid
            // for this device. Without this, applying a template and then
            // picking a device would silently wipe the very capability the
            // template just set.
            onSelect = { device ->
                val keptCapability = form.capability.takeIf { it.isNotBlank() && device.capabilities.contains(it) } ?: ""
                onChange(form.copy(deviceId = device._id, capability = keptCapability))
            }
        )
        LabeledDropdown(
            label = "Capability",
            selectedLabel = form.capability.ifBlank { "Select a capability" },
            options = selectedDevice?.capabilities ?: emptyList(),
            optionLabel = { it },
            onSelect = { onChange(form.copy(capability = it)) }
        )
        LabeledDropdown(
            label = "Operator",
            selectedLabel = CLAUSE_OPERATORS.find { it.first == form.operator }?.second ?: form.operator,
            options = CLAUSE_OPERATORS,
            optionLabel = { it.second },
            onSelect = { onChange(form.copy(operator = it.first)) }
        )
        if (form.operator != "changed") {
            OutlinedTextField(
                value = form.value,
                onValueChange = { onChange(form.copy(value = it)) },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionEditor(
    devices: List<DeviceDto>,
    form: ActionForm,
    onChange: (ActionForm) -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    val selectedDevice = devices.find { it._id == form.deviceId }

    HomeHubCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
        if (trailing != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                trailing()
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            listOf("device_command" to "Control Device", "notify" to "Send Notification").forEach { (value, label) ->
                if (form.type == value) {
                    Button(onClick = { onChange(form.copy(type = value)) }) { Text(label) }
                } else {
                    OutlinedButton(onClick = { onChange(form.copy(type = value)) }) { Text(label) }
                }
            }
        }
        if (form.type == "device_command") {
            LabeledDropdown(
                label = "Device",
                selectedLabel = selectedDevice?.name ?: "Select a device",
                options = devices,
                optionLabel = { it.name },
                // Same reasoning as ClauseEditor's device dropdown above —
                // keep a template-prefilled capability if it's still valid
                // for the newly picked device.
                onSelect = { device ->
                    val keptCapability = form.capability.takeIf { it.isNotBlank() && device.capabilities.contains(it) } ?: ""
                    onChange(form.copy(deviceId = device._id, capability = keptCapability))
                }
            )
            LabeledDropdown(
                label = "Capability",
                selectedLabel = form.capability.ifBlank { "Select a capability" },
                options = selectedDevice?.capabilities ?: emptyList(),
                optionLabel = { it },
                onSelect = { onChange(form.copy(capability = it)) }
            )
            OutlinedTextField(
                value = form.value,
                onValueChange = { onChange(form.copy(value = it)) },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            OutlinedTextField(
                value = form.message,
                onValueChange = { onChange(form.copy(message = it)) },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> LabeledDropdown(
    label: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CreatedConfirmation(
    warnings: List<String>?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                "Rule created",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = MaterialTheme.spacing.sm)
            )
        }

        if (!warnings.isNullOrEmpty()) {
            HomeHubCard(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
            ) {
                Text(
                    "Heads up — this rule may conflict with an existing one:",
                    style = MaterialTheme.typography.bodyMedium
                )
                warnings.forEach { warning ->
                    Text("• $warning", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}