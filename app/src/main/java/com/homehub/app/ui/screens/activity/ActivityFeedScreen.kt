package com.homehub.app.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.ui.components.EmptyState
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.components.HomeHubHeader
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 2 (polish pass): shared `HomeHubHeader`, `HomeHubCard` rows
 * with a source-tinted icon avatar (live socket event vs. rule-fired vs.
 * plain history) instead of a bare card, and a proper `EmptyState`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(
    onBack: () -> Unit,
    viewModel: ActivityFeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeHubHeader(
                title = "Activity",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
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
            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Couldn't load activity: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(MaterialTheme.spacing.xl)
                    )
                }
            }
            uiState.items.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.History,
                    message = "No activity yet",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(MaterialTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        ActivityRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityItem) {
    HomeHubCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActivitySourceIcon(sourceLabel = item.sourceLabel)
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.deviceName, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        item.sourceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(item.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    item.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * A "live" event (arrived over the socket while this screen is open) or a
 * rule-triggered event gets a distinct icon from a plain historical MQTT/
 * webhook reading — this feed otherwise reads as a flat list where the
 * most interesting rows (something just happened / an automation fired)
 * don't stand out from routine state history.
 */
@Composable
private fun ActivitySourceIcon(sourceLabel: String) {
    val (icon, tint) = when (sourceLabel) {
        "live" -> Icons.Filled.Sensors to MaterialTheme.colorScheme.primary
        "rule", "rule_fired" -> Icons.Filled.Bolt to MaterialTheme.colorScheme.tertiary
        else -> Icons.Filled.History to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}