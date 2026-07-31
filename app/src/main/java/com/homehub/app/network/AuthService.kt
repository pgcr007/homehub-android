package com.homehub.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val name: String?)
data class UserDto(
    val _id: String,
    val email: String,
    val name: String?,
    val household: String? = null,
    val createdAt: String? = null
)
data class AuthResponse(val token: String, val user: UserDto)

// Profile screen: the backend wraps GET /me's payload as { "user": {...} },
// same shape as every other single-resource response in this API (see
// CreateDeviceResponse, RoomsResponse, etc.) — me() previously declared its
// return type as a bare UserDto, which would have thrown a JSON parse
// mismatch the first time anything actually called it. Nothing did until
// the Profile screen, which is what surfaced it.
data class MeResponse(val user: UserDto)

data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
data class ChangePasswordResponse(val status: String)

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @PATCH("api/auth/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): ChangePasswordResponse
}