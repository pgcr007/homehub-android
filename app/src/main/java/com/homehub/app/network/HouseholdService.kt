package com.homehub.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface HouseholdService {
    // Neither of these needs an X-Household-Id header — you don't know
    // which household to scope to until you've either created one or
    // listed the ones you're already in (mirrors the backend's own comment
    // in householdRoutes.js).
    @GET("api/households")
    suspend fun listMyHouseholds(): HouseholdsResponse

    @POST("api/households")
    suspend fun createHousehold(@Body request: CreateHouseholdRequest): CreateHouseholdResponse
}