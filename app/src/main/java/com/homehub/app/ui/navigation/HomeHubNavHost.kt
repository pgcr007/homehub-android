package com.homehub.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.homehub.app.ui.screens.activity.ActivityFeedScreen
import com.homehub.app.ui.screens.adddevice.AddDeviceScreen
import com.homehub.app.ui.screens.dashboard.DashboardScreen
import com.homehub.app.ui.screens.household.HouseholdSwitcherScreen
import com.homehub.app.ui.screens.household.MembersScreen
import com.homehub.app.ui.screens.login.LoginScreen
import com.homehub.app.ui.screens.onboarding.OnboardingPrefs
import com.homehub.app.ui.screens.onboarding.OnboardingScreen
import com.homehub.app.ui.screens.rules.CreateRuleScreen
import com.homehub.app.ui.screens.rules.RulesListScreen

sealed class Destination(val route: String) {
    // Phase 7 Step 3
    data object Onboarding : Destination("onboarding")
    data object Login : Destination("login")
    data object Dashboard : Destination("dashboard")
    data object AddDevice : Destination("add_device")
    data object ActivityFeed : Destination("activity_feed")
    data object RulesList : Destination("rules_list")
    data object CreateRule : Destination("create_rule")
    // Phase 6 Step 4
    data object HouseholdSwitcher : Destination("household_switcher")
    data object Members : Destination("members")
}

// Phase 7 Step 4: shared transition specs so every screen slides in the same
// way instead of the default hard cut — 250ms, standard forward/back slide
// with a fade, matching the platform's own feel without pulling in a full
// custom animation per screen.
private const val TRANSITION_MS = 250
private val enterTransition = slideInHorizontally(animationSpec = tween(TRANSITION_MS)) { it / 4 } +
        fadeIn(animationSpec = tween(TRANSITION_MS))
private val exitTransition = slideOutHorizontally(animationSpec = tween(TRANSITION_MS)) { -it / 4 } +
        fadeOut(animationSpec = tween(TRANSITION_MS))
private val popEnterTransition = slideInHorizontally(animationSpec = tween(TRANSITION_MS)) { -it / 4 } +
        fadeIn(animationSpec = tween(TRANSITION_MS))
private val popExitTransition = slideOutHorizontally(animationSpec = tween(TRANSITION_MS)) { it / 4 } +
        fadeOut(animationSpec = tween(TRANSITION_MS))

/**
 * Phase 7 bugfix: root cause of "double-tap anything nav-related -> screen
 * goes blank white, nothing in Logcat". Compose doesn't debounce onClick —
 * a fast double-tap on a button/row fires its lambda twice before the first
 * navigation transition finishes composing. That's harmless for a second
 * `navigate()` (fixed earlier with `launchSingleTop`), but a second
 * `popBackStack()` is not harmless: it pops again, and since login removes
 * `Login` from the stack (`popUpTo(Login) { inclusive = true }`), a second
 * pop from Dashboard empties the back stack completely. An empty stack has
 * no current destination for NavHost to render — blank screen, no
 * exception thrown anywhere, so Logcat stays clean. This is exactly what
 * was reported: no crash, just white.
 *
 * Fix: a single timestamp guard here, wrapping every `navigate()`/
 * `popBackStack()` call site below, so a second tap within 500ms of the
 * first is a no-op — this covers every button/row across the whole app
 * from one place instead of debouncing each screen's onClick individually.
 */
@Composable
fun HomeHubNavHost(navController: NavHostController = rememberNavController()) {
    var lastNavActionAt by remember { mutableLongStateOf(0L) }
    fun debounced(action: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastNavActionAt > 500) {
            lastNavActionAt = now
            action()
        }
    }

    // Phase 7 Step 3: onboarding only shows once per install. Read the flag
    // once at first composition (`remember`, not on every recomposition) —
    // this is intentionally NOT reactive to the flag changing later in this
    // same process, since the only writer (OnboardingScreen's onDone) also
    // immediately navigates away, so nothing here needs to react afterward.
    val context = LocalContext.current
    val startDestination = remember {
        if (OnboardingPrefs.hasSeenOnboarding(context)) Destination.Login.route else Destination.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        composable(Destination.Onboarding.route) {
            OnboardingScreen(
                onDone = {
                    debounced {
                        OnboardingPrefs.markOnboardingSeen(context)
                        navController.navigate(Destination.Login.route) {
                            popUpTo(Destination.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Destination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    debounced {
                        navController.navigate(Destination.Dashboard.route) {
                            popUpTo(Destination.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Destination.Dashboard.route) {
            DashboardScreen(
                onAddDevice = { debounced { navController.navigate(Destination.AddDevice.route) { launchSingleTop = true } } },
                onViewActivity = { debounced { navController.navigate(Destination.ActivityFeed.route) { launchSingleTop = true } } },
                onViewRules = { debounced { navController.navigate(Destination.RulesList.route) { launchSingleTop = true } } },
                onSwitchHousehold = { debounced { navController.navigate(Destination.HouseholdSwitcher.route) { launchSingleTop = true } } }
            )
        }
        composable(Destination.AddDevice.route) {
            AddDeviceScreen(
                onDone = { debounced { navController.popBackStack() } },
                onBack = { debounced { navController.popBackStack() } }
            )
        }
        composable(Destination.ActivityFeed.route) {
            ActivityFeedScreen(
                onBack = { debounced { navController.popBackStack() } }
            )
        }
        composable(Destination.RulesList.route) {
            RulesListScreen(
                onBack = { debounced { navController.popBackStack() } },
                onCreateRule = { debounced { navController.navigate(Destination.CreateRule.route) { launchSingleTop = true } } }
            )
        }
        composable(Destination.CreateRule.route) {
            CreateRuleScreen(
                onDone = { debounced { navController.popBackStack() } },
                onBack = { debounced { navController.popBackStack() } }
            )
        }
        composable(Destination.HouseholdSwitcher.route) {
            HouseholdSwitcherScreen(
                onBack = { debounced { navController.popBackStack() } },
                onSelected = { debounced { navController.popBackStack() } },
                onManageMembers = { debounced { navController.navigate(Destination.Members.route) { launchSingleTop = true } } }
            )
        }
        composable(Destination.Members.route) {
            MembersScreen(
                onBack = { debounced { navController.popBackStack() } }
            )
        }
    }
}