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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.homehub.app.network.CLAUSE_OPERATORS
import com.homehub.app.network.DeviceDto

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
            TopAppBar(
                title = { Text("New Rule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.created) {
            CreatedConfirmation(warnings = uiState.warnings, onDone = onDone, modifier = Modifier.padding(padding))
        } else {
            RuleForm(uiState = uiState, viewModel = viewModel, modifier = Modifier.padding(padding))
        }
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Rule name") },
            placeholder = { Text("e.g. Motion turns on hallway light") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

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
            Text(uiState.error, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = viewModel::submit,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                onSelect = { onChange(form.copy(deviceId = it._id, capability = "")) }
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (trailing != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    trailing()
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    onSelect = { onChange(form.copy(deviceId = it._id, capability = "")) }
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
                .menuAnchor()
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Rule created", style = MaterialTheme.typography.titleLarge)

        if (!warnings.isNullOrEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Heads up — this rule may conflict with an existing one:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    warnings.forEach { warning ->
                        Text("• $warning", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}