package com.homehub.app.ui.screens.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SensorDoor
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.DeviceDto
import com.homehub.app.network.HouseholdHolder
import com.homehub.app.ui.components.DeviceIcon
import com.homehub.app.ui.components.EmptyState
import com.homehub.app.ui.components.ErrorMessage
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.components.HomeHubHeader
import com.homehub.app.ui.components.StatusBadge
import com.homehub.app.ui.theme.homeHubColors
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 2 (polish pass, app-wide). Dashboard now uses the shared
 * `HomeHubHeader` (extracted from this screen's original one-off gradient
 * header) so its look matches every other top-level screen, with an
 * online/offline summary row in the header's `extraContent` slot. Device
 * cards lead with a tinted `DeviceIcon` avatar and a labeled `StatusBadge`;
 * the FAB is an extended "Add device" button instead of a bare "+".
 * Functionally unchanged from Phase 6/7 Step 1 — same ViewModel, same
 * commands — this is presentation only.
 *
 * Phase 7 Step 6: added a logout action in the header, gated behind a
 * confirm dialog. Dashboard is the natural home for it — it's the app's
 * one persistent top-level screen everything else navigates out from and
 * back to, and there's no separate Profile/Settings screen (nor a clear
 * need for one yet) to put it on instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddDevice: () -> Unit,
    onViewActivity: () -> Unit,
    onViewRules: () -> Unit,
    onSwitchHousehold: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showTurnOffAllConfirm by remember { mutableStateOf(false) }

    // Refresh whenever this screen comes back into view — e.g. returning
    // from Add Device, the rule builder, or the household switcher, so
    // newly added devices/state (or a newly switched-to household) show up
    // without a manual pull-to-refresh.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshForPossibleHouseholdChange()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            val devices = uiState.devices
            val onlineCount = devices.count { it.status == "online" }
            val offlineCount = devices.count { it.status == "offline" }
            val unknownCount = devices.size - onlineCount - offlineCount

            val summaryContent: (@Composable () -> Unit)? = if (devices.isNotEmpty()) {
                @Composable {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                        SummaryPill(count = onlineCount, label = "online", dotColor = MaterialTheme.homeHubColors.statusOnline)
                        if (offlineCount > 0) {
                            SummaryPill(count = offlineCount, label = "offline", dotColor = MaterialTheme.homeHubColors.statusOffline)
                        }
                        if (unknownCount > 0) {
                            SummaryPill(count = unknownCount, label = "unreachable", dotColor = MaterialTheme.homeHubColors.statusUnknown)
                        }
                    }
                }
            } else {
                null
            }

            HomeHubHeader(
                title = HouseholdHolder.activeHouseholdName ?: "HomeHub",
                navigationIcon = {
                    IconButton(onClick = onSwitchHousehold) {
                        Icon(Icons.Filled.Home, contentDescription = "Switch household", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    val powerDeviceIds = uiState.devices.filter { it.capabilities.contains("power") }.map { it._id }
                    if (powerDeviceIds.isNotEmpty()) {
                        IconButton(onClick = { showTurnOffAllConfirm = true }) {
                            Icon(Icons.Filled.PowerSettingsNew, contentDescription = "Turn off all devices", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    TextButton(onClick = onViewRules) {
                        Text("Rules", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onViewActivity) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Activity", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Log out", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                extraContent = summaryContent
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add device") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onAddDevice
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
                        "Couldn't load dashboard: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center).padding(MaterialTheme.spacing.xl)
                    )
                }
                uiState.devices.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.SensorDoor,
                        message = "No devices yet — tap \"Add device\" to connect your first one",
                    )
                }
                else -> {
                    val unassigned = uiState.devices.filter { it.room == null }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(MaterialTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl)
                    ) {
                        items(uiState.rooms) { room ->
                            val roomDevices = uiState.devices.filter { it.room == room._id }
                            if (roomDevices.isNotEmpty()) {
                                RoomSection(
                                    title = room.name,
                                    devices = roomDevices,
                                    onCommand = viewModel::sendCommand,
                                    onTurnOffAll = { ids -> viewModel.sendBulkCommand(ids, mapOf("power" to "off")) }
                                )
                            }
                        }
                        if (unassigned.isNotEmpty()) {
                            item {
                                RoomSection(
                                    title = "Unassigned",
                                    devices = unassigned,
                                    onCommand = viewModel::sendCommand,
                                    onTurnOffAll = { ids -> viewModel.sendBulkCommand(ids, mapOf("power" to "off")) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to manage this household.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Bulk device actions (post-Phase 7): unlike the per-room "Turn off
    // all" (small blast radius, easy to undo with one switch tap), this
    // one hits every powered device across the whole household in one go —
    // worth a confirm, same treatment as logout above.
    if (showTurnOffAllConfirm) {
        val powerDeviceIds = uiState.devices.filter { it.capabilities.contains("power") }.map { it._id }
        AlertDialog(
            onDismissRequest = { showTurnOffAllConfirm = false },
            title = { Text("Turn off all devices?") },
            text = { Text("This will turn off every powered device in this household (${powerDeviceIds.size} device${if (powerDeviceIds.size == 1) "" else "s"}).") },
            confirmButton = {
                TextButton(onClick = {
                    showTurnOffAllConfirm = false
                    viewModel.sendBulkCommand(powerDeviceIds, mapOf("power" to "off"))
                }) { Text("Turn off all") }
            },
            dismissButton = {
                TextButton(onClick = { showTurnOffAllConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SummaryPill(count: Int, label: String, dotColor: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .padding(end = MaterialTheme.spacing.xs)
                .height(8.dp)
                .width(8.dp)
                .clip(RoundedCornerShape(50))
                .background(dotColor)
        )
        Text(
            "$count $label",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun RoomSection(
    title: String,
    devices: List<DeviceDto>,
    onCommand: (DeviceDto, Map<String, Any>) -> Unit,
    onTurnOffAll: (List<String>) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.xs))
                Text(
                    "· ${devices.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bulk device actions (post-Phase 7): "turn off all in this
            // room" for the property-manager persona clearing out a unit
            // between guests, without tapping every switch one at a time.
            // Only shown when there's actually something to turn off —
            // sensors/contact devices don't have a "power" capability, so a
            // room of only those would show a button that does nothing.
            val powerDeviceIds = devices.filter { it.capabilities.contains("power") }.map { it._id }
            if (powerDeviceIds.isNotEmpty()) {
                TextButton(onClick = { onTurnOffAll(powerDeviceIds) }) {
                    Text("Turn off all")
                }
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        // Plain chunked rows instead of LazyVerticalGrid — a lazy grid needs
        // a bounded height to measure against, which it doesn't have when
        // nested inside a LazyColumn item (crashes with "measured with an
        // infinity maximum height constraints"). Each room's device list is
        // small, so it doesn't need its own laziness anyway.
        devices.chunked(2).forEach { rowDevices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
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
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
        }
    }
}

@Composable
private fun DeviceCard(
    device: DeviceDto,
    onCommand: (DeviceDto, Map<String, Any>) -> Unit
) {
    HomeHubCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DeviceIcon(capabilities = device.capabilities)
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.bodyLarge)
                StatusBadge(status = device.status, showLabel = true)
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

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