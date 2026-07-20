package com.homehub.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val name: String?)
data class UserDto(val _id: String, val email: String, val name: String?, val household: String? = null)
data class AuthResponse(val token: String, val user: UserDto)


interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): UserDto
}