package com.homehub.app.network

/**
 * Phase 6 broke the app against the live backend: every household-scoped
 * endpoint (rooms/devices/events/rules) now requires an X-Household-Id
 * header, and nothing on the Android side was ever setting one. This is
 * the minimal fix to unbreak day-to-day use — pick *some* active household
 * right after login — not the full household-switcher UI (that's Step 4).
 *
 * Priority order:
 *   1. The `household` convenience pointer on the logged-in user, set by
 *      the backend the moment a household is created (see
 *      householdController.createHousehold) — no extra round trip needed.
 *   2. The first household from GET /api/households, for an account whose
 *      pointer wasn't set (e.g. added as a member of someone else's
 *      household rather than having created their own).
 *   3. Create a new "My Home" household — a first-time user with no
 *      household at all yet.
 *
 * Call this once, right after a successful login/register, before
 * navigating into any household-scoped screen.
 */
suspend fun bootstrapActiveHousehold(userHouseholdId: String?) {
    if (userHouseholdId != null) {
        HouseholdHolder.activeHouseholdId = userHouseholdId
        return
    }

    val existing = ApiClient.householdService.listMyHouseholds().households
    if (existing.isNotEmpty()) {
        HouseholdHolder.activeHouseholdId = existing.first()._id
        return
    }

    val created = ApiClient.householdService.createHousehold(CreateHouseholdRequest(name = "My Home"))
    HouseholdHolder.activeHouseholdId = created.household._id
}