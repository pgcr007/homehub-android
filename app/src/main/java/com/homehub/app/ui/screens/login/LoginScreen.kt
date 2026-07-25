package com.homehub.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.homehub.app.network.ApiClient
import com.homehub.app.network.LoginRequest
import com.homehub.app.network.TokenHolder
import com.homehub.app.network.UserHolder
import com.homehub.app.network.bootstrapActiveHousehold
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.theme.spacing
import kotlinx.coroutines.launch

/**
 * Phase 7 Step 2 (polish pass). First screen anyone sees, so it's the one
 * that most needed to stop looking like unstyled Compose defaults: a full
 * gradient background (brand blue, matching HomeHubHeader elsewhere in the
 * app), a circular logo mark, a tagline, and the form itself lifted onto a
 * `HomeHubCard` instead of floating directly on the background. No logic
 * changed — same ApiClient call, same bootstrapActiveHousehold flow.
 */
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
            Text(
                "HomeHub",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                "Multi-unit smart home management",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xxl))

            HomeHubCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(MaterialTheme.spacing.xl)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                    )
                }

                Button(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        scope.launch {
                            try {
                                val response = ApiClient.authService.login(LoginRequest(email, password))
                                TokenHolder.token = response.token
                                UserHolder.userId = response.user._id
                                bootstrapActiveHousehold(response.user.household)
                                onLoginSuccess()
                            } catch (e: Exception) {
                                errorMessage = "Login failed: ${e.message ?: "unknown error"}"
                                // Note: if login itself succeeded and bootstrapActiveHousehold threw
                                // (e.g. network blip), TokenHolder.token/UserHolder.userId are already
                                // set but no household is active yet — every household-scoped call
                                // will 400 until they retry. The household switcher (Step 4) can now
                                // recover this without a re-login, but the dashboard itself would need
                                // to tolerate landing with no active household first; not handled here.
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.lg)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Text("Log in")
                    }
                }
            }
        }
    }
}