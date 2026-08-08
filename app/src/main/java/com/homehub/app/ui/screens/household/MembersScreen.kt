package com.homehub.app.ui.screens.household

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homehub.app.network.HouseholdMemberDto
import com.homehub.app.network.UserHolder
import com.homehub.app.ui.components.ErrorMessage
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.components.HomeHubHeader
import com.homehub.app.ui.components.InitialsAvatar
import com.homehub.app.ui.components.RoleBadge
import com.homehub.app.ui.theme.homeHubColors
import com.homehub.app.ui.theme.spacing
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.graphics.Color

/**
 * Phase 6 Step 4. Shows the active household's members + roles. Add/remove
 * controls only render for manager+ (mirrors requireRole('owner',
 * 'manager') on the backend's member routes) — this is UI convenience
 * only, not the actual enforcement; the backend re-checks and 403s
 * regardless of what this screen shows.
 *
 * Phase 7 Step 2 (polish pass): shared `HomeHubHeader` instead of a plain
 * `TopAppBar`, with a member-count subtitle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    onBack: () -> Unit,
    viewModel: MembersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingRemove by remember { mutableStateOf<HouseholdMemberDto?>(null) }

    val canManageMembers = uiState.myRole == "owner" || uiState.myRole == "manager"
    val memberCount = uiState.household?.members?.size ?: 0

    Scaffold(
        topBar = {
            HomeHubHeader(
                title = uiState.household?.name ?: "Members",
                subtitle = if (memberCount > 0) "$memberCount member${if (memberCount == 1) "" else "s"}" else null,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (canManageMembers) {
                ExtendedFloatingActionButton(
                    text = { Text("Add member") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = { showAddDialog = true }
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    if (uiState.error != null) {
                        ErrorMessage(
                            message = uiState.error ?: "",
                            modifier = Modifier.padding(MaterialTheme.spacing.lg)
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(MaterialTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                    ) {
                        items(uiState.household?.members ?: emptyList(), key = { it.user._id }) { member ->
                            MemberRow(
                                member = member,
                                // Mirrors requireHousehold.js's own removal
                                // rule: an owner can remove anyone; a
                                // manager can remove managers/members but
                                // not another owner. Also never show it on
                                // your own row — self-removal isn't a
                                // supported/tested flow here.
                                canRemove = canManageMembers &&
                                        member.user._id != UserHolder.userId &&
                                        (member.role != "owner" || uiState.myRole == "owner"),
                                onRemove = { pendingRemove = member }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemberDialog(
            allowOwnerRole = uiState.myRole == "owner",
            isSubmitting = uiState.isInviting,
            onDismiss = { showAddDialog = false },
            onConfirm = { email, role, expiresAtIso ->
                viewModel.addMember(email, role, expiresAtIso)
                showAddDialog = false
            }
        )
    }

    val memberToRemove = pendingRemove
    if (memberToRemove != null) {
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text("Remove ${memberToRemove.user.name ?: memberToRemove.user.email}?") },
            text = { Text("They'll lose access to this household immediately.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeMember(memberToRemove)
                    pendingRemove = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemove = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MemberRow(
    member: HouseholdMemberDto,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    val colors = MaterialTheme.homeHubColors
    val avatarTint = when (member.role) {
        "owner" -> colors.roleOwner
        "manager" -> colors.roleManager
        "guest" -> colors.roleGuest
        else -> colors.roleMember
    }
    // Parsed client-side purely for display — access enforcement is always
    // server-side (Household.roleOf), this is just so a manager sees "3
    // days left" instead of a raw ISO timestamp.
    val expiryLabel = remember(member.expiresAt) {
        member.expiresAt?.let { iso ->
            try {
                val expiry = java.time.Instant.parse(iso)
                val now = java.time.Instant.now()
                if (expiry.isBefore(now)) {
                    "Expired"
                } else {
                    val daysLeft = java.time.Duration.between(now, expiry).toDays()
                    if (daysLeft < 1) "Expires today" else "Expires in ${daysLeft}d"
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    HomeHubCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(displayName = member.user.name ?: member.user.email, tint = avatarTint)
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
                Column {
                    Text(member.user.name ?: member.user.email, style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoleBadge(role = member.role, modifier = Modifier.padding(top = MaterialTheme.spacing.xs))
                        if (expiryLabel != null) {
                            Text(
                                expiryLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (expiryLabel == "Expired") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = MaterialTheme.spacing.sm, top = MaterialTheme.spacing.xs)
                            )
                        }
                    }
                }
            }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove member")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddMemberDialog(
    allowOwnerRole: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    // role == "guest" always carries a non-null expiresAtIso; every other
    // role always passes null.
    onConfirm: (email: String, role: String, expiresAtIso: String?) -> Unit
) {
    val homeHubColors = MaterialTheme.homeHubColors
    var email by remember { mutableStateOf("") }
    // Mirrors requireHousehold.js: only an owner can grant the 'owner' role
    // to someone else. 'guest' is available to anyone who can open this
    // dialog (manager+), same as 'member'.
    val roleOptions = if (allowOwnerRole) {
        listOf("member", "manager", "owner", "guest")
    } else {
        listOf("member", "manager", "guest")
    }
    var role by remember { mutableStateOf(roleOptions.first()) }
    // Guest-only: how many days from now access expires. Fixed set of
    // durations (checkout-length stays, not arbitrary dates) matches the
    // "quick Airbnb turnover" use case this role exists for.
    var guestDurationDays by remember { mutableStateOf(3) }
    val guestDurationOptions = listOf(1, 3, 7, 14)

    fun roleColor(option: String): Color = when (option) {
        "owner" -> homeHubColors.roleOwner
        "manager" -> homeHubColors.roleManager
        "guest" -> homeHubColors.roleGuest
        else -> homeHubColors.roleMember
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add member") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "They need an existing HomeHub account — this adds them by email, it doesn't send an invite.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.md)
                )

                Text(
                    "Role",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.xs)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    roleOptions.forEach { option ->
                        val selected = option == role
                        FilterChip(
                            selected = selected,
                            onClick = { role = option },
                            label = { Text(option.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = roleColor(option),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (role == "guest") {
                    Text(
                        "Access length",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.xs)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                    ) {
                        guestDurationOptions.forEach { days ->
                            val selected = days == guestDurationDays
                            FilterChip(
                                selected = selected,
                                onClick = { guestDurationDays = days },
                                label = { Text("${days}d") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = homeHubColors.roleGuest,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Text(
                        "Access is revoked automatically after $guestDurationDays day${if (guestDurationDays == 1) "" else "s"}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = email.isNotBlank() && !isSubmitting,
                onClick = {
                    val expiresAtIso = if (role == "guest") {
                        java.time.Instant.now()
                            .plus(guestDurationDays.toLong(), java.time.temporal.ChronoUnit.DAYS)
                            .toString()
                    } else null
                    onConfirm(email.trim(), role, expiresAtIso)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}