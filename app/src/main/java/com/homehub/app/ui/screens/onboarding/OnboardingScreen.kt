package com.homehub.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.homehub.app.ui.theme.spacing
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Filled.Home,
        title = "Every unit, one dashboard",
        description = "See and control devices across all your properties from a single screen — no more juggling one app per vendor."
    ),
    OnboardingPage(
        icon = Icons.Filled.Bolt,
        title = "Automate with rules",
        description = "Set up triggers, conditions, and actions once — HomeHub handles the rest, with built-in protection against automation loops."
    ),
    OnboardingPage(
        icon = Icons.Filled.NotificationsActive,
        title = "Stay in the loop",
        description = "Get notified the moment a device goes offline or a rule fires, so nothing needs a manual check-in."
    ),
)

/**
 * Phase 7 Step 3. First-run-only intro shown before Login (gated by
 * `OnboardingPrefs`, see `HomeHubNavHost`). Mirrors LoginScreen's gradient
 * treatment so it doesn't look like a bolted-on separate flow.
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.lg),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDone) {
                Text("Skip", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                OnboardingPageContent(pages[pageIndex])
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
            PageIndicator(pageCount = pages.size, currentPage = pagerState.currentPage)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))

            TextButton(
                onClick = {
                    val isLastPage = pagerState.currentPage == pages.lastIndex
                    if (isLastPage) {
                        onDone()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }
            ) {
                Text(
                    if (pagerState.currentPage == pages.lastIndex) "Get started" else "Next",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                page.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
        Text(
            page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        Text(
            page.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (isActive) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = if (isActive) 1f else 0.4f)
                    )
            )
        }
    }
}