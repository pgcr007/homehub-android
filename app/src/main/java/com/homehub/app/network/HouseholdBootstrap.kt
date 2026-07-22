package com.homehub.app.network

/**
 * Phase 6 broke the app against the live backend: every household-scoped
 * endpoint (rooms/devices/events/rules) requires an X-Household-Id header,
 * and nothing on the Android side was ever setting one. Step 4 replaces
 * the original one-shot fix with a real household switcher (see
 * HouseholdSwitcherScreen) — this function is now just the *initial* pick
 * right after login; switching households later goes through
 * applyActiveHousehold() directly instead.
 *
 * Priority order:
 *   1. The `household` convenience pointer on the logged-in user, set by
 *      the backend the moment a household is created (see
 *      householdController.createHousehold) — matched against the fetched
 *      list so activeHouseholdRole/Name get filled in too, not just the id.
 *   2. The first household from GET /api/households, for an account whose
 *      pointer wasn't set (e.g. added as a member of someone else's
 *      household rather than having created their own).
 *   3. Create a new "My Home" household — a first-time user with no
 *      household at all yet.
 */
suspend fun bootstrapActiveHousehold(userHouseholdId: String?) {
    val households = ApiClient.householdService.listMyHouseholds().households
    val active = households.find { it._id == userHouseholdId } ?: households.firstOrNull()

    if (active != null) {
        applyActiveHousehold(active)
        return
    }

    val created = ApiClient.householdService.createHousehold(CreateHouseholdRequest(name = "My Home"))
    // Creator is always made 'owner' server-side (householdController.createHousehold),
    // but the create endpoint doesn't echo myRole back (only listMyHouseholds does) —
    // safe to set it directly here rather than doing a second round trip.
    applyActiveHousehold(created.household.copy(myRole = "owner"))
}

/**
 * Switches the app's active household — used both by the initial bootstrap
 * above and by the household switcher screen when the user picks a
 * different one. Purely local/in-memory (no network call): the caller
 * already has the HouseholdDto, complete with myRole, from a prior
 * listMyHouseholds() response.
 */
fun applyActiveHousehold(household: HouseholdDto) {
    HouseholdHolder.activeHouseholdId = household._id
    HouseholdHolder.activeHouseholdName = household.name
    HouseholdHolder.activeHouseholdRole = household.myRole
}