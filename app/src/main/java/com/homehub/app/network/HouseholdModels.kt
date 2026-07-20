package com.homehub.app.network

data class HouseholdMemberDto(
    val user: String,
    val role: String
)

data class HouseholdDto(
    val _id: String,
    val name: String,
    val type: String,
    val members: List<HouseholdMemberDto> = emptyList(),
    // Only present on GET /api/households (listMyHouseholds), not on the
    // single-household create/get responses.
    val myRole: String? = null
)

data class HouseholdsResponse(val households: List<HouseholdDto>)
data class CreateHouseholdRequest(val name: String, val type: String = "residential")
data class CreateHouseholdResponse(val household: HouseholdDto)