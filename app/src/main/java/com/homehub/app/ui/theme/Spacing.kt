package com.homehub.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Phase 7 Step 2. Named spacing steps instead of ad-hoc `.dp` literals
 * scattered per-screen — DashboardScreen alone used 8/12/16/20/24.dp for
 * conceptually-similar gaps before this existed. Not meant to be
 * exhaustive; a screen can still reach for a raw `.dp` for a genuine
 * one-off layout need. But the common cases — screen-edge padding, gap
 * between cards, padding inside a card — should all pull from here so a
 * future spacing tweak is a one-line change instead of a grep-and-replace.
 */
data class HomeHubSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

val LocalHomeHubSpacing = staticCompositionLocalOf { HomeHubSpacing() }