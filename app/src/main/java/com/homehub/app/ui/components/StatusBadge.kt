package com.homehub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.homehub.app.ui.theme.homeHubColors
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 2. Shared connectivity-status indicator — replaces the
 * private `StatusDot` that lived inside DashboardScreen.kt through Phase 6
 * (three `Color(0xFF...)` literals only that one screen knew about). Any
 * screen showing a device's online/offline/unknown state should use this
 * instead, so the three colors stay defined in exactly one place
 * (Color.kt's `HomeHubExtendedColors`).
 *
 * @param status raw device status string ("online" | "offline" | anything else -> unknown)
 * @param showLabel when true, renders the status word next to the dot — useful in denser
 *   list rows where a bare dot reads as too subtle. Dot-only (false) matches the existing
 *   Dashboard device-card layout.
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
) {
    val colors = MaterialTheme.homeHubColors
    val (color, label) = when (status) {
        "online" -> colors.statusOnline to "Online"
        "offline" -> colors.statusOffline to "Offline"
        else -> colors.statusUnknown to "Unknown"
    }

    if (!showLabel) {
        Box(
            modifier = modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
    } else {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.xs))
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}