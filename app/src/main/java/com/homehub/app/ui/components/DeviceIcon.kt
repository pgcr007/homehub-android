package com.homehub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Phase 7 Step 2 (polish pass). Gives each device card a glanceable identity
 * instead of just a name string — a tinted circular avatar with an icon
 * chosen from the device's capability list. Picks the first capability
 * match in this priority order (a plug that also reports temperature shows
 * as a lightbulb/switch, not a thermostat, since "can I turn it on/off" is
 * the primary thing a property manager cares about for that device).
 */
@Composable
fun DeviceIcon(
    capabilities: List<String>,
    modifier: Modifier = Modifier,
    avatarSize: androidx.compose.ui.unit.Dp = 40.dp,
) {
    val icon = deviceIconGlyphFor(capabilities)
    val tint = deviceAccentColor(capabilities)
    Box(
        modifier = modifier
            .size(avatarSize)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(avatarSize * 0.55f)
        )
    }
}

/**
 * The tint half of [deviceIconGlyphFor]'s capability mapping, exposed on its
 * own so anything wanting "this device's color" — not just its icon avatar —
 * can key off the same priority order instead of re-deriving it. Used by
 * DashboardScreen's device cards for a per-device accent stripe.
 */
@Composable
fun deviceAccentColor(capabilities: List<String>): androidx.compose.ui.graphics.Color {
    val scheme = MaterialTheme.colorScheme
    return when {
        capabilities.contains("power") || capabilities.contains("brightness") -> scheme.primary
        capabilities.contains("temperature") -> scheme.error
        capabilities.contains("motion") -> scheme.tertiary
        capabilities.contains("contact") -> scheme.secondary
        else -> scheme.onSurfaceVariant
    }
}

private fun deviceIconGlyphFor(capabilities: List<String>): ImageVector = when {
    capabilities.contains("power") || capabilities.contains("brightness") -> Icons.Filled.Lightbulb
    capabilities.contains("temperature") -> Icons.Filled.Thermostat
    capabilities.contains("motion") -> Icons.Filled.Sensors
    capabilities.contains("contact") -> Icons.Filled.Lock
    else -> Icons.Filled.Power
}