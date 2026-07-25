package com.homehub.app.ui.screens.adddevice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.DEVICE_TYPE_OPTIONS
import com.homehub.app.network.DeviceTypeOption
import com.homehub.app.network.RoomDto
import com.homehub.app.ui.components.ErrorMessage
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.components.HomeHubHeader
import com.homehub.app.ui.theme.spacing

private const val NEW_ROOM_SENTINEL = "__new_room__"

/**
 * Phase 7 Step 2 (polish pass): shared `HomeHubHeader`; the form fields
 * and the webhook-credentials callout both now sit inside `HomeHubCard`
 * sections instead of a bare Column / plain Card, and the success screen
 * leads with a check-circle icon instead of just a text heading. No
 * behavioral change — same ViewModel, same submit flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddDeviceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeHubHeader(
                title = "Add device",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        val result = uiState.result
        if (result != null) {
            DeviceAddedConfirmation(
                deviceName = result.device.name,
                webhookUrl = result.webhookUrl,
                webhookSecret = result.webhookSecret,
                note = result.note,
                onDone = onDone,
                modifier = Modifier.padding(padding)
            )
        } else {
            AddDeviceForm(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDeviceForm(
    uiState: AddDeviceUiState,
    viewModel: AddDeviceViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        HomeHubCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Living Room Lamp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                DeviceTypeDropdown(
                    selected = uiState.selectedType,
                    onSelect = viewModel::onTypeChange
                )

                OutlinedTextField(
                    value = uiState.identifier,
                    onValueChange = viewModel::onIdentifierChange,
                    label = { Text("Identifier") },
                    supportingText = { Text(uiState.selectedType.identifierHint) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                RoomDropdown(
                    rooms = uiState.rooms,
                    selectedRoomId = uiState.selectedRoomId,
                    isCreatingRoom = uiState.isCreatingRoom,
                    newRoomName = uiState.newRoomName,
                    onRoomSelected = viewModel::onRoomSelected,
                    onStartCreatingRoom = { viewModel.toggleCreatingRoom(true) },
                    onCancelCreatingRoom = { viewModel.toggleCreatingRoom(false) },
                    onNewRoomNameChange = viewModel::onNewRoomNameChange,
                    onCreateRoom = viewModel::createRoom
                )
            }
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
            Text("Add Device")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceTypeDropdown(
    selected: DeviceTypeOption,
    onSelect: (DeviceTypeOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Device Type") },
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
            DEVICE_TYPE_OPTIONS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomDropdown(
    rooms: List<RoomDto>,
    selectedRoomId: String?,
    isCreatingRoom: Boolean,
    newRoomName: String,
    onRoomSelected: (String?) -> Unit,
    onStartCreatingRoom: () -> Unit,
    onCancelCreatingRoom: () -> Unit,
    onNewRoomNameChange: (String) -> Unit,
    onCreateRoom: () -> Unit
) {
    if (isCreatingRoom) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            OutlinedTextField(
                value = newRoomName,
                onValueChange = onNewRoomNameChange,
                label = { Text("New room name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            TextButton(onClick = onCreateRoom) { Text("Create") }
            TextButton(onClick = onCancelCreatingRoom) { Text("Cancel") }
        }
        return
    }

    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = rooms.find { it._id == selectedRoomId }?.name ?: "Unassigned"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Room") },
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
            DropdownMenuItem(
                text = { Text("Unassigned") },
                onClick = {
                    onRoomSelected(null)
                    expanded = false
                }
            )
            rooms.forEach { room ->
                DropdownMenuItem(
                    text = { Text(room.name) },
                    onClick = {
                        onRoomSelected(room._id)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("+ Add new room") },
                onClick = {
                    expanded = false
                    onStartCreatingRoom()
                }
            )
        }
    }
}

@Composable
private fun DeviceAddedConfirmation(
    deviceName: String,
    webhookUrl: String?,
    webhookSecret: String?,
    note: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Text("\"$deviceName\" added", style = MaterialTheme.typography.titleLarge)

        if (webhookUrl != null && webhookSecret != null) {
            HomeHubCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    Text(
                        "This device uses the webhook protocol. Configure the vendor with:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LabeledValue(label = "Webhook URL", value = webhookUrl)
                    LabeledValue(label = "Signing Secret", value = webhookSecret)
                    if (note != null) {
                        Text(note, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "You can re-fetch this secret later from the device detail screen.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        SelectionContainer {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
        }
    }
}