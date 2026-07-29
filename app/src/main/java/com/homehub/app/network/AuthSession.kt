package com.homehub.app.network

import com.homehub.app.realtime.SocketManager

/**
 * Phase 7 Step 6, updated post-Phase 7 once SessionStore landed. "Logout"
 * is clearing the three in-memory holders that together define "signed in"
 * (token, active household, user id), tearing down the live socket
 * connection (keyed to exactly that state — see SocketManager.connect(),
 * which reads TokenHolder.token and HouseholdHolder.activeHouseholdId
 * directly), and clearing the on-disk persisted session so a killed/
 * restarted process doesn't restore straight back into an account the
 * user explicitly signed out of.
 *
 * No server-side call — there's no session/refresh-token to revoke
 * server-side (see authController.js: register/login just hand back a JWT,
 * nothing stateful is created), so clearing local state is sufficient.
 *
 * Centralized here rather than inlined at the one call site (Dashboard's
 * logout button) so a second entry point later (e.g. an eventual Profile
 * screen, or an interceptor-driven auto-logout on a 401) doesn't have to
 * remember every piece of state that needs clearing.
 */
object AuthSession {
    fun logout() {
        SocketManager.disconnect()
        TokenHolder.token = null
        HouseholdHolder.activeHouseholdId = null
        HouseholdHolder.activeHouseholdName = null
        HouseholdHolder.activeHouseholdRole = null
        UserHolder.userId = null
        SessionStore.clear()
    }
}