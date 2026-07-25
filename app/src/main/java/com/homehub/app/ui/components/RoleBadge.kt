package com.homehub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.homehub.app.ui.theme.homeHubColors
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 2. Small colored pill for a household member's role
 * (owner/manager/member). MembersScreen previously rendered the role as
 * plain gray `bodySmall` text — functional, but in a multi-tenant B2B app
 * where "who can do what" is load-bearing information (it drives every
 * add/remove-member permission check on both the client and the backend),
 * it earns a visual identity distinct from ordinary secondary text.
 */
@Composable
fun RoleBadge(role: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.homeHubColors
    val color = when (role) {
        "owner" -> colors.roleOwner
        "manager" -> colors.roleManager
        else -> colors.roleMember
    }
    Text(
        text = role.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs)
    )
}