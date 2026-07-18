package com.homehub.app.ui.screens.adddevice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.DeviceTypeOption
import com.homehub.app.network.DEVICE_TYPE_OPTIONS
import com.homehub.app.network.RoomDto

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
            TopAppBar(
                title = { Text("Add Device") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                .menuAnchor()
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                .menuAnchor()
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("\"$deviceName\" added", style = MaterialTheme.typography.titleLarge)

        if (webhookUrl != null && webhookSecret != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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