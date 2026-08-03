package com.homehub.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.homehub.app.R

/**
 * Phase 8 polish: HomeHub now bundles an actual display typeface — Space
 * Grotesk (SIL OFL, res/font/space_grotesk_*.ttf) — instead of relying on
 * whatever Roboto build happens to ship on a given device. Its squared-off,
 * slightly geometric letterforms (and unusually good tabular figures) suit
 * a technical, numbers-heavy dashboard — device counts, percentages,
 * temperatures — and reads as a deliberate brand choice the moment you see
 * the "HomeHub" wordmark or a screen title, rather than default-Android
 * text.
 *
 * It's used only for headline/title styles — the handful of large,
 * infrequent, "this is a heading" moments where personality earns its
 * keep. Body/label copy (device names, list rows, helper text) stays on
 * `FontFamily.Default` (Roboto): at small sizes and high density, a
 * platform-tuned system font remains the safer choice for legibility, and
 * pairing one display face against the system body face is a deliberate,
 * common pattern (not an oversight) — it's how most of Space Grotesk's own
 * real-world pairings are actually built.
 */
private val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
)

/**
 * An explicit type scale rather than Material3's bare `Typography()`
 * default that Theme.kt shipped with through Phase 6. Sizes and
 * line-heights still match the M3 baseline (no existing layout reflows),
 * but headline/title tracking is pulled in slightly negative — Space
 * Grotesk's wide default spacing needs it at display sizes, and tight
 * tracking on large text is itself a large part of what makes a heading
 * read as "designed" instead of "default".
 */
val HomeHubTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
)