package com.homehub.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PATCH
import retrofit2.http.DELETE

interface DeviceService {
    @GET("api/rooms")
    suspend fun listRooms(): RoomsResponse

    @GET("api/devices")
    suspend fun listDevices(): DevicesResponse

    @POST("api/devices/{id}/command")
    suspend fun sendCommand(@Path("id") deviceId: String, @Body command: Map<String, @JvmSuppressWildcards Any>): CommandResponse

    @POST("api/devices")
    suspend fun createDevice(@Body request: CreateDeviceRequest): CreateDeviceResponse

    @POST("api/rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): CreateRoomResponse

    @GET("api/events")
    suspend fun listEvents(@Query("limit") limit: Int = 50): EventsResponse

    @GET("api/rules")
    suspend fun listRules(): RulesResponse

    @POST("api/rules")
    suspend fun createRule(@Body request: CreateRuleRequest): RuleResponse

    @PATCH("api/rules/{id}")
    suspend fun toggleRule(
        @Path("id") id: String,
        @Body request: ToggleRuleRequest
    ): RuleResponse

    @DELETE("api/rules/{id}")
    suspend fun deleteRule(@Path("id") id: String): DeleteRuleResponse

}