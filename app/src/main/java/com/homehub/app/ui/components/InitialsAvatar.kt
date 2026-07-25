package com.homehub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Phase 7 Step 2 (polish pass). A colored circular initials avatar for a
 * household member, tinted by role so it doubles as an at-a-glance role
 * indicator even before you read the RoleBadge text next to it.
 */
@Composable
fun InitialsAvatar(
    displayName: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tint),
        contentAlignment = Alignment.Center
    ) {
        Text(initial, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}