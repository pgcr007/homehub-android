package com.homehub.app.network

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.homehub.app.HomeHubApplication
import org.json.JSONObject

private const val PREFS_NAME = "homehub_session"
private const val KEY_TOKEN = "token"
private const val KEY_USER_ID = "user_id"
private const val KEY_HOUSEHOLD_ID = "household_id"
private const val KEY_HOUSEHOLD_NAME = "household_name"
private const val KEY_HOUSEHOLD_ROLE = "household_role"

/**
 * Persisted login sessions (post-Phase 7). TokenHolder/UserHolder/
 * HouseholdHolder were in-memory only — every process death (device
 * restart, app swiped away, or just Android reclaiming memory in the
 * background) forced a re-login, which was flagged as worth fixing before
 * Phase 8 since it'd otherwise affect QA test scenarios.
 *
 * EncryptedSharedPreferences rather than plain SharedPreferences (unlike
 * OnboardingPrefs' one boolean flag) because this holds an auth token —
 * worth the extra dependency for at-rest encryption on the token itself.
 *
 * This mirrors AuthSession's own centralization: one object owns writing
 * to and reading from disk, so every call site (login, register, the
 * household switcher, logout) just calls persist()/clear() without needing
 * to know the storage mechanism.
 */
object SessionStore {
    private val prefs by lazy {
        val context = HomeHubApplication.appContext
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Snapshots whatever's currently in TokenHolder/UserHolder/
     * HouseholdHolder to disk. Called from applyActiveHousehold() rather
     * than from each individual call site (login/register success,
     * household switch) — by the time applyActiveHousehold runs in every
     * one of those flows, token and user id are already set, so this one
     * spot captures the complete session state regardless of which flow
     * got it there.
     */
    fun persist() {
        try {
            prefs.edit()
                .putString(KEY_TOKEN, TokenHolder.token)
                .putString(KEY_USER_ID, UserHolder.userId)
                .putString(KEY_HOUSEHOLD_ID, HouseholdHolder.activeHouseholdId)
                .putString(KEY_HOUSEHOLD_NAME, HouseholdHolder.activeHouseholdName)
                .putString(KEY_HOUSEHOLD_ROLE, HouseholdHolder.activeHouseholdRole)
                .apply()
        } catch (e: Exception) {
            // Persistence failing shouldn't break the session that's already
            // live in memory for this process — worst case, this particular
            // process death won't be recoverable, same as before this feature
            // existed.
            Log.e("SessionStore", "Failed to persist session", e)
        }
    }

    /**
     * Loads a previously persisted session back into TokenHolder/
     * UserHolder/HouseholdHolder. Returns true if a valid, non-expired
     * session was restored (caller can go straight to Dashboard), false
     * otherwise (caller should show Login).
     *
     * Checks the JWT's own "exp" claim locally rather than only finding out
     * via a 401 on the first Dashboard load — restoring a dead token would
     * otherwise land the user on a broken Dashboard with a generic load
     * error instead of the login screen they actually need.
     */
    fun restore(): Boolean {
        val token = try {
            prefs.getString(KEY_TOKEN, null)
        } catch (e: Exception) {
            Log.e("SessionStore", "Failed to read persisted session", e)
            null
        } ?: return false

        if (isJwtExpired(token)) {
            clear()
            return false
        }

        TokenHolder.token = token
        UserHolder.userId = prefs.getString(KEY_USER_ID, null)
        HouseholdHolder.activeHouseholdId = prefs.getString(KEY_HOUSEHOLD_ID, null)
        HouseholdHolder.activeHouseholdName = prefs.getString(KEY_HOUSEHOLD_NAME, null)
        HouseholdHolder.activeHouseholdRole = prefs.getString(KEY_HOUSEHOLD_ROLE, null)

        // A token without a household means bootstrap/switch never
        // completed (e.g. process died mid-flow) — every household-scoped
        // call would 400 without one, so this isn't a restorable session.
        return HouseholdHolder.activeHouseholdId != null
    }

    fun clear() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            Log.e("SessionStore", "Failed to clear persisted session", e)
        }
    }

    private fun isJwtExpired(token: String): Boolean {
        return try {
            val payload = token.split(".")[1]
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val exp = JSONObject(String(decoded, Charsets.UTF_8)).optLong("exp", 0L)
            exp != 0L && exp * 1000 < System.currentTimeMillis()
        } catch (e: Exception) {
            // Can't parse it — don't block restoration on this check alone;
            // a genuinely dead token will still fail on the first real
            // request and the user can log out/back in from there.
            false
        }
    }
}