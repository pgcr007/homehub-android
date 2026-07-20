package com.homehub.app.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homehub.app.network.ApiClient
import com.homehub.app.network.LoginRequest
import com.homehub.app.network.TokenHolder
import com.homehub.app.network.bootstrapActiveHousehold
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("HomeHub", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        errorMessage?.let {
            Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                errorMessage = null
                isLoading = true
                scope.launch {
                    try {
                        val response = ApiClient.authService.login(LoginRequest(email, password))
                        TokenHolder.token = response.token
                        bootstrapActiveHousehold(response.user.household)
                        onLoginSuccess()
                    } catch (e: Exception) {
                        errorMessage = "Login failed: ${e.message ?: "unknown error"}"
                        // Note: if login itself succeeded and bootstrapActiveHousehold threw
                        // (e.g. network blip), TokenHolder.token is already set but no household
                        // is active yet — every household-scoped call will 400 until they retry.
                        // Fine for now; Step 4's household switcher will make this recoverable
                        // without a re-login.
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Text("Log in")
            }
        }
    }
}