package com.homehub.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.DeviceDto
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddDevice: () -> Unit,
    onViewActivity: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadData()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HomeHub") },
                actions = {
                    IconButton(onClick = onViewActivity) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Activity")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDevice) {
                Icon(Icons.Filled.Add, contentDescription = "Add device")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        "Couldn't load dashboard: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }
                else -> {
                    val unassigned = uiState.devices.filter { it.room == null }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(uiState.rooms) { room ->
                            val roomDevices = uiState.devices.filter { it.room == room._id }
                            if (roomDevices.isNotEmpty()) {
                                RoomSection(title = room.name, devices = roomDevices, onCommand = viewModel::sendCommand)
                            }
                        }
                        if (unassigned.isNotEmpty()) {
                            item {
                                RoomSection(title = "Unassigned", devices = unassigned, onCommand = viewModel::sendCommand)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun RoomSection(
    title: String,
    devices: List<DeviceDto>,
    onCommand: (DeviceDto, Map<String, Any>) -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))

        // Plain chunked rows instead of LazyVerticalGrid — a lazy grid needs
        // a bounded height to measure against, which it doesn't have when
        // nested inside a LazyColumn item (crashes with "measured with an
        // infinity maximum height constraints"). Each room's device list is
        // small, so it doesn't need its own laziness anyway.
        devices.chunked(2).forEach { rowDevices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowDevices.forEach { device ->
                    Box(modifier = Modifier.weight(1f)) {
                        DeviceCard(device = device, onCommand = onCommand)
                    }
                }
                if (rowDevices.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun DeviceCard(
    device: DeviceDto,
    onCommand: (DeviceDto, Map<String, Any>) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(device.name, style = MaterialTheme.typography.bodyLarge)
                StatusDot(status = device.status)
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))

            if (device.capabilities.contains("power")) {
                val isOn = device.state["power"] == "on"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isOn) "On" else "Off", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isOn,
                        onCheckedChange = { checked ->
                            onCommand(device, mapOf("power" to if (checked) "on" else "off"))
                        }
                    )
                }
            }

            if (device.capabilities.contains("brightness")) {
                val currentBrightness = (device.state["brightness"] as? Number)?.toFloat() ?: 0f
                var sliderValue by remember(device._id) { mutableFloatStateOf(currentBrightness) }
                Text("Brightness: ${sliderValue.toInt()}%", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = sliderValue,
                    valueRange = 0f..100f,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        onCommand(device, mapOf("brightness" to sliderValue.toInt()))
                    }
                )
            }

            if (device.capabilities.contains("temperature")) {
                val temp = device.state["temperature"]
                Text("Temp: ${temp ?: "—"}°", style = MaterialTheme.typography.bodyMedium)
            }

            if (device.capabilities.contains("contact")) {
                val contact = device.state["contact"]
                Text("Contact: ${contact ?: "unknown"}", style = MaterialTheme.typography.bodyMedium)
            }

            if (device.capabilities.contains("motion")) {
                val motion = device.state["motion"]
                Text("Motion: ${motion ?: "unknown"}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StatusDot(status: String) {
    val color = when (status) {
        "online" -> Color(0xFF2E7D32)
        "offline" -> Color(0xFFC62828)
        else -> Color(0xFF9E9E9E) // unknown
    }
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}