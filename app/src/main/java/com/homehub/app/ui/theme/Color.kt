package com.homehub.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Phase 7 Step 2. HomeHub's brand palette, hand-picked rather than generated
 * from a per-device Material You seed, because this is a B2B product: a
 * property manager or host should see the same HomeHub blue on every
 * device, not a palette that shifts with each user's wallpaper. Dynamic
 * color is still wired up in Theme.kt and can be switched back on if
 * per-device personalization ever matters more than brand consistency.
 */

// Primary — a confident, techy blue. App bar, primary buttons, FAB, and
// "selected" states (active household, selected role in Add Member, etc).
private val Blue10 = Color(0xFF00174B)
private val Blue20 = Color(0xFF002873)
private val Blue30 = Color(0xFF043C99)
private val Blue40 = Color(0xFF1552B8)
private val Blue80 = Color(0xFFAFC6FF)
private val Blue90 = Color(0xFFD8E2FF)

// Secondary — a warm slate, so supporting UI (secondary buttons, outlines)
// doesn't read as "everything is blue".
private val Slate10 = Color(0xFF191C22)
private val Slate20 = Color(0xFF2E313A)
private val Slate30 = Color(0xFF454851)
private val Slate40 = Color(0xFF5D6069)
private val Slate80 = Color(0xFFC5C6D0)
private val Slate90 = Color(0xFFE1E2EC)

// Tertiary — a muted teal accent, reserved for "something happened" moments
// (e.g. a rule firing in the Activity Feed) distinct from plain device
// state changes.
private val Teal10 = Color(0xFF00201C)
private val Teal20 = Color(0xFF00352E)
private val Teal30 = Color(0xFF004E44)
private val Teal40 = Color(0xFF196A5D)
private val Teal80 = Color(0xFF83D5C4)
private val Teal90 = Color(0xFF9FF2DF)

private val Error20 = Color(0xFF690005)
private val Error40 = Color(0xFFBA1A1A)
private val Error80 = Color(0xFFFFB4AB)
private val Error90 = Color(0xFFFFDAD6)

val HomeHubLightColors: ColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Slate40,
    onSecondary = Color.White,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate10,
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Teal90,
    onTertiaryContainer = Teal10,
    error = Error40,
    onError = Color.White,
    errorContainer = Error90,
    onErrorContainer = Error20,
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1B20),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1B20),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Slate30,
    outline = Color(0xFF74777F),
)

val HomeHubDarkColors: ColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Slate80,
    onSecondary = Slate20,
    secondaryContainer = Slate30,
    onSecondaryContainer = Slate90,
    tertiary = Teal80,
    onTertiary = Teal20,
    tertiaryContainer = Teal30,
    onTertiaryContainer = Teal90,
    error = Error80,
    onError = Error20,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Error90,
    background = Color(0xFF121317),
    onBackground = Color(0xFFE3E2E9),
    surface = Color(0xFF121317),
    onSurface = Color(0xFFE3E2E9),
    surfaceVariant = Slate30,
    onSurfaceVariant = Slate80,
    outline = Color(0xFF8E9099),
)

/**
 * Semantic colors Material3's ColorScheme has no slot for: device
 * connectivity status and household-member role. Centralizing these means
 * every screen that shows a status dot or a role badge (Dashboard, Members,
 * and eventually Rules/Activity Feed) agrees on what "online" or "manager"
 * looks like, instead of each screen picking its own `Color(0xFF...)`
 * inline — which is how DashboardScreen's pre-Phase-7 StatusDot worked.
 */
@Immutable
data class HomeHubExtendedColors(
    val statusOnline: Color,
    val statusOffline: Color,
    val statusUnknown: Color,
    val roleOwner: Color,
    val roleManager: Color,
    val roleMember: Color,
)

val LightExtendedColors = HomeHubExtendedColors(
    statusOnline = Color(0xFF2E7D32),
    statusOffline = Color(0xFFC62828),
    statusUnknown = Color(0xFF757780),
    roleOwner = Blue40,
    roleManager = Teal40,
    roleMember = Slate40,
)

val DarkExtendedColors = HomeHubExtendedColors(
    statusOnline = Color(0xFF81C995),
    statusOffline = Color(0xFFFFB4AB),
    statusUnknown = Color(0xFF9A9CA5),
    roleOwner = Blue80,
    roleManager = Teal80,
    roleMember = Slate80,
)

val LocalHomeHubColors = staticCompositionLocalOf { LightExtendedColors }