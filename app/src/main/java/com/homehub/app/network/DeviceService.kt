package com.homehub.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
}