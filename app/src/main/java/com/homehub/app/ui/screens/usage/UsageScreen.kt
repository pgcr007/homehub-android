package com.homehub.app.ui.screens.usage

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.UsageDeviceDto
import com.homehub.app.ui.components.DeviceIcon
import com.homehub.app.ui.components.EmptyState
import com.homehub.app.ui.components.ErrorMessage
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.components.HomeHubHeader
import com.homehub.app.ui.theme.spacing

/**
 * Usage insights — derived from EventLog state-change history (see backend
 * usageService.js), not a live device poll. Shows roughly how many hours
 * each power-capable device has been "on" over a trailing window, so a
 * property manager can spot the plug/bulb that's been running nonstop.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(
    onBack: () -> Unit,
    viewModel: UsageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeHubHeader(
                title = "Usage",
                subtitle = "Last ${uiState.windowDays} days \u00b7 ${uiState.totalOnHours}h total",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                extraContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                    ) {
                        listOf(7, 14, 30).forEach { days ->
                            FilterChip(
                                selected = uiState.windowDays == days,
                                onClick = { viewModel.load(days) },
                                label = { Text("${days}d") }
                            )
                        }
                    }
                }
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
            uiState.devices.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.Bolt,
                    message = "No usage data yet \u2014 power-capable devices will show up here once they report state changes",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(MaterialTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    items(uiState.devices, key = { it.deviceId }) { device ->
                        UsageRow(device = device, maxOnHours = uiState.devices.first().onHours)
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
}

@Composable
private fun UsageRow(device: UsageDeviceDto, maxOnHours: Double) {
    HomeHubCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DeviceIcon(capabilities = listOf("power"))
            Column(modifier = Modifier.padding(start = MaterialTheme.spacing.sm).weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    device.room?.name ?: "Unassigned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${device.onHours}h",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // Simple relative bar so the busiest device is scannable at a glance
        // without reading every number in the list.
        val fraction = if (maxOnHours > 0) (device.onHours / maxOnHours).toFloat().coerceIn(0f, 1f) else 0f
        androidx.compose.material3.LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.xs)
        )
    }
}