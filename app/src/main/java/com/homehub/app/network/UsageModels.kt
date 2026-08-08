package com.homehub.app.network

data class UsageDeviceRoom(val id: String, val name: String)

data class UsageDeviceDto(
    val deviceId: String,
    val name: String,
    val type: String,
    val room: UsageDeviceRoom?,
    val onHours: Double
)

data class UsageResponse(
    val windowDays: Int,
    val generatedAt: String,
    val totalOnHours: Double,
    val devices: List<UsageDeviceDto>
)