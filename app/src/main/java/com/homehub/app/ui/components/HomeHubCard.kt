package com.homehub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.homehub.app.ui.theme.spacing

/**
 * Phase 7 Step 2. Thin wrapper around Material3's `Card` standardizing the
 * inner padding every card-shaped row in this app already wanted (device
 * cards, member rows, rule rows each hardcoded `12.dp` independently,
 * inline, through Phase 6). Shape and elevation still come from
 * `MaterialTheme` (HomeHubShapes / default tonal elevation) — this only
 * takes padding off each screen's hands, so a future padding tweak is one
 * line here instead of a grep-and-replace across five files.
 *
 * Phase 8 polish: added an optional `accentColor` left-edge stripe. A flat
 * white/surface rectangle is the one thing every generic Material app
 * looks like out of the box; a thin color-coded edge — the device's own
 * tint from `deviceAccentColor`, a rule's trigger color, whatever's
 * relevant per screen — turns a plain card into a small piece of
 * information (glanceable category/state) and gives HomeHub's card grid a
 * recognizable signature instead of a wall of identical white boxes.
 * Purely additive: omitting it renders exactly as before.
 *
 * @param contentPadding override the default `MaterialTheme.spacing.md` padding
 *   for a card that needs something different (rare — most callers can omit this).
 * @param containerColor override the card's background — used for "this one
 *   is the selected/active item" states (e.g. HouseholdSwitcher's active
 *   household) instead of a plain surface color for every row.
 * @param accentColor optional color for a 4dp left-edge stripe (e.g. a
 *   device's capability tint). Omitted entirely when null.
 * @param verticalArrangement spacing between direct children — most cards are a
 *   single Row/Column and don't need this, but multi-field forms (Create Rule's
 *   clause/action editors) do.
 */
@Composable
fun HomeHubCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
    containerColor: Color? = null,
    accentColor: Color? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = if (containerColor != null) {
            CardDefaults.cardColors(containerColor = containerColor)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        if (accentColor != null) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(accentColor)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(contentPadding ?: PaddingValues(MaterialTheme.spacing.md)),
                    verticalArrangement = verticalArrangement
                ) {
                    content()
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(contentPadding ?: PaddingValues(MaterialTheme.spacing.md)),
                verticalArrangement = verticalArrangement
            ) {
                content()
            }
        }
    }
}