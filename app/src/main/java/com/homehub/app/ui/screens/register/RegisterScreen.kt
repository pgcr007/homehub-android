package com.homehub.app.ui.screens.register

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.homehub.app.R
import com.homehub.app.network.ApiClient
import com.homehub.app.network.RegisterRequest
import com.homehub.app.network.TokenHolder
import com.homehub.app.network.UserHolder
import com.homehub.app.network.bootstrapActiveHousehold
import com.homehub.app.ui.components.ErrorMessage
import com.homehub.app.ui.components.HomeHubCard
import com.homehub.app.ui.theme.spacing
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Companion to LoginScreen — the backend has always had a working
 * POST /api/auth/register (see authController.js) and AuthService already
 * declared register() in its Retrofit interface, but no screen ever called
 * it. Styling deliberately mirrors LoginScreen (same gradient, logo, card)
 * so the two feel like one flow rather than a bolted-on afterthought.
 *
 * Same post-auth path as login: store the token/userId, then run
 * bootstrapActiveHousehold(null) — a brand-new user has no household yet
 * (register doesn't return one), so this always falls to the "create a new
 * 'My Home' household" branch, giving every fresh account somewhere to land.
 */
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
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
            Image(
                painter = painterResource(R.drawable.logo_mark),
                contentDescription = "HomeHub logo",
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
            Text(
                "Create your account",
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
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
                    supportingText = { Text("At least 8 characters") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                errorMessage?.let {
                    ErrorMessage(
                        message = it,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                    )
                }

                Button(
                    onClick = {
                        errorMessage = null
                        if (password.length < 8) {
                            errorMessage = "Password must be at least 8 characters"
                            return@Button
                        }
                        isLoading = true
                        scope.launch {
                            try {
                                val response = ApiClient.authService.register(
                                    RegisterRequest(
                                        email = email,
                                        password = password,
                                        name = name.ifBlank { null }
                                    )
                                )
                                TokenHolder.token = response.token
                                UserHolder.userId = response.user._id
                                bootstrapActiveHousehold(response.user.household)
                                onRegisterSuccess()
                            } catch (e: HttpException) {
                                // 409 = email already registered (see authController.register);
                                // 400 = missing/short fields, though the client-side length
                                // check above should catch the password case first.
                                errorMessage = when (e.code()) {
                                    409 -> "An account with that email already exists"
                                    400 -> "Please check your email and password"
                                    else -> "Registration failed (server said ${e.code()}). Please try again."
                                }
                            } catch (e: SocketTimeoutException) {
                                // Same Render cold-start behavior as LoginScreen.
                                errorMessage = "The server is taking a while to respond — it may be waking up. Please try again in a moment."
                            } catch (e: IOException) {
                                errorMessage = "Can't reach the server. Check your connection and try again."
                            } catch (e: Exception) {
                                errorMessage = "Registration failed: ${e.message ?: "unknown error"}"
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
                        Text("Create account")
                    }
                }

                TextButton(
                    onClick = onBackToLogin,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already have an account? Log in")
                }
            }
        }
    }
}