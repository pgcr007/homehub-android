package com.homehub.app.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HouseholdService {
    // Neither of these needs an X-Household-Id header — you don't know
    // which household to scope to until you've either created one or
    // listed the ones you're already in (mirrors the backend's own comment
    // in householdRoutes.js).
    @GET("api/households")
    suspend fun listMyHouseholds(): HouseholdsResponse

    @POST("api/households")
    suspend fun createHousehold(@Body request: CreateHouseholdRequest): CreateHouseholdResponse

    // These three act on "current" (the household in the X-Household-Id
    // header, attached automatically by householdInterceptor in ApiClient).
    @GET("api/households/current")
    suspend fun getCurrentHousehold(): HouseholdDetailResponse

    // manager+ only server-side (requireRole('owner', 'manager')) — a 403
    // here means the UI showed a control it shouldn't have, not a bug in
    // this call.
    @POST("api/households/current/members")
    suspend fun addMember(@Body request: AddMemberRequest): AddMemberResponse

    @DELETE("api/households/current/members/{userId}")
    suspend fun removeMember(@Path("userId") userId: String): RemoveMemberResponse
}