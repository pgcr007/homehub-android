package com.homehub.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

/**
 * Phase 7 Step 2: HomeHub now has an actual design system — a branded color
 * scheme (Color.kt), an explicit type scale (Type.kt), named spacing steps
 * (Spacing.kt), and rounder card shapes (Shape.kt) — instead of the bare
 * Compose defaults this screen shipped with through Phase 6.
 *
 * `dynamicColor` now defaults to false: HomeHub is a B2B tool a property
 * manager or host checks across several devices, and its brand blue should
 * look the same on all of them rather than shifting with each device's
 * Material You wallpaper-derived palette. Still fully wired up below and
 * honored if a future build wants to opt in per-user.
 */
@Composable
fun HomeHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> HomeHubDarkColors
        else -> HomeHubLightColors
    }
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalHomeHubColors provides extendedColors,
        LocalHomeHubSpacing provides HomeHubSpacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HomeHubTypography,
            shapes = HomeHubShapes,
            content = content
        )
    }
}

/**
 * Extension accessors on MaterialTheme (mirrors how `MaterialTheme.colorScheme`
 * / `MaterialTheme.typography` already read) so call sites use
 * `MaterialTheme.homeHubColors.statusOnline` and `MaterialTheme.spacing.md`
 * instead of reaching for `LocalHomeHubColors.current` / `LocalHomeHubSpacing.current`
 * directly at every call site.
 */
val MaterialTheme.homeHubColors: HomeHubExtendedColors
    @Composable get() = LocalHomeHubColors.current

val MaterialTheme.spacing: HomeHubSpacing
    @Composable get() = LocalHomeHubSpacing.current