package com.homehub.app.network

// Unpopulated member ref — as returned by listMyHouseholds() and
// createHousehold(), where `members.user` is a bare ObjectId string.
data class HouseholdMemberRefDto(
    val user: String,
    val role: String
)

// Household shape for GET /api/households (list) and POST /api/households
// (create) — members NOT populated.
data class HouseholdDto(
    val _id: String,
    val name: String,
    val type: String,
    val members: List<HouseholdMemberRefDto> = emptyList(),
    // Only present on the list endpoint (each household annotated with the
    // caller's own role in it), not on create.
    val myRole: String? = null
)

data class HouseholdsResponse(val households: List<HouseholdDto>)
data class CreateHouseholdRequest(val name: String, val type: String = "residential")
data class CreateHouseholdResponse(val household: HouseholdDto)

// Minimal user shape as populated into members.user by the backend's
// `household.populate('members.user', 'email name')` — see
// getCurrentHousehold/addMember in householdController.js.
data class MemberUserDto(
    val _id: String,
    val email: String,
    val name: String?
)

// Populated member — used by GET /api/households/current and
// POST /api/households/current/members, where members.user IS the full
// {_id, email, name} object rather than a bare ObjectId.
data class HouseholdMemberDto(
    val user: MemberUserDto,
    val role: String
)

data class HouseholdDetailDto(
    val _id: String,
    val name: String,
    val type: String,
    val members: List<HouseholdMemberDto> = emptyList()
)

// GET /api/households/current returns { household, myRole } — myRole sits
// alongside household, not inside it (matches householdController.getHousehold).
data class HouseholdDetailResponse(val household: HouseholdDetailDto, val myRole: String? = null)

// POST /api/households/current/members returns just { household } (no
// top-level myRole — inviting someone doesn't change your own role).
data class AddMemberRequest(val email: String, val role: String = "member")
data class AddMemberResponse(val household: HouseholdDetailDto)

data class RemoveMemberResponse(val status: String)