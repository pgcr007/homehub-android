package com.homehub.app

import android.app.Application

/**
 * Persisted login sessions (post-Phase 7): SessionStore needs a Context to
 * open EncryptedSharedPreferences, but it's called from places that don't
 * naturally have one on hand — applyActiveHousehold() in particular is a
 * plain top-level function shared by login, register, bootstrap, and the
 * household switcher, and threading a Context through all of those call
 * sites just to persist a household selection would be a lot of churn for
 * what is otherwise a one-line write. An Application-scoped context is safe
 * to hold statically (it lives exactly as long as the process does, unlike
 * an Activity/Composable context) so SessionStore reads it from here
 * instead.
 */
class HomeHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: android.content.Context
            private set
    }
}