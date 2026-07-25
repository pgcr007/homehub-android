package com.homehub.app.ui.screens.onboarding

import android.content.Context

private const val PREFS_NAME = "homehub_prefs"
private const val KEY_ONBOARDING_SEEN = "onboarding_seen"

/**
 * Phase 7 Step 3. Plain `SharedPreferences` rather than DataStore — this is
 * a single boolean flag with no migration/versioning needs, so DataStore
 * would be more machinery than the problem calls for. Gates the onboarding
 * flow to first-launch-ever; every subsequent app open (even after the
 * process is killed, since login itself isn't persisted yet — see
 * TokenHolder) skips straight to Login.
 */
object OnboardingPrefs {
    fun hasSeenOnboarding(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_SEEN, false)

    fun markOnboardingSeen(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_SEEN, true)
            .apply()
    }
}