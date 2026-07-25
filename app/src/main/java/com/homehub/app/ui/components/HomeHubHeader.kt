package com.homehub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 2 (polish pass, app-wide). The gradient "hero" header
 * originally built for DashboardScreen, generalized so every top-level
 * screen (Households, Members, Rules, Activity, Add Device) shares the
 * same panel-with-rounded-bottom-corners look instead of each screen using
 * a plain flat `TopAppBar`. Bottom corners only are rounded; the gradient
 * bleeds up behind the status bar (this app runs edge-to-edge — see
 * MainActivity's `enableEdgeToEdge()`) while title/nav/actions stay clear
 * of it via `.statusBarsPadding()` on the inner content.
 *
 * @param subtitle optional secondary line under the title (e.g. a live device count).
 * @param extraContent optional slot below the title row for something like
 *   Dashboard's online/offline summary pills — most screens omit this.
 */
@Composable
fun HomeHubHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    extraContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                navigationIcon?.invoke()
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
        extraContent?.invoke()
    }
}