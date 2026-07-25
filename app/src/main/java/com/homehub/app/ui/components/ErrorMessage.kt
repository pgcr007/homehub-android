package com.homehub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 4 (polish pass). Every screen through Step 3 rendered errors
 * as bare red `Text` — functional, but easy to miss scanning a form, and
 * inconsistent in placement/wording across screens. This gives every error
 * the same tinted-container-plus-icon treatment, using HomeHubShapes'
 * medium corner radius to match cards elsewhere.
 */
@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(MaterialTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}