package com.homehub.app.network

data class RoomDto(
    val _id: String,
    val name: String
)

data class DeviceDto(
    val _id: String,
    val name: String,
    val room: String?,
    val type: String,
    val protocol: String,
    val identifier: String,
    val capabilities: List<String>,
    val state: Map<String, Any?>,
    val status: String, // "online" | "offline" | "unknown"
    val lastSeen: String?
)

data class RoomsResponse(val rooms: List<RoomDto>)
data class DevicesResponse(val devices: List<DeviceDto>)
data class CommandResponse(val status: String, val topic: String, val command: Map<String, Any?>)

data class EventDeviceRef(val _id: String, val name: String, val type: String, val room: String?)

data class DeviceTypeOption(
    val value: String,
    val label: String,
    val protocol: String,
    val identifierHint: String
)

val DEVICE_TYPE_OPTIONS = listOf(
    DeviceTypeOption("tasmota_plug", "Smart Plug (Tasmota)", "mqtt", "MQTT topic segment, e.g. plug-livingroom"),
    DeviceTypeOption("tasmota_bulb", "Smart Bulb, dimmable (Tasmota)", "mqtt", "MQTT topic segment, e.g. bulb-bedroom"),
    DeviceTypeOption("esphome_contact_sensor", "Door/Window Sensor (ESPHome)", "mqtt", "MQTT topic segment, e.g. frontdoor-sensor"),
    DeviceTypeOption("esphome_motion_sensor", "Motion Sensor (ESPHome)", "mqtt", "MQTT topic segment, e.g. hallway-motion"),
    DeviceTypeOption("webhook_thermostat", "Thermostat (Vendor Webhook)", "webhook", "Any label, e.g. main-thermostat")
)

data class CreateDeviceRequest(
    val name: String,
    val type: String,
    val identifier: String,
    val room: String?
)

data class CreateDeviceResponse(
    val device: DeviceDto,
    val webhookUrl: String? = null,
    val webhookSecret: String? = null,
    val note: String? = null
)

data class EventDto(
    val _id: String,
    val device: EventDeviceRef?,
    val source: String, // "mqtt" | "webhook" | "rule"
    val type: String,   // "state_change" | "online" | "offline" | "unknown" | "rule_fired"
    val normalizedState: Map<String, Any?>,
    val createdAt: String
)


data class CreateRoomRequest(val name: String)
data class CreateRoomResponse(val room: RoomDto)

data class EventsResponse(val events: List<EventDto>)
