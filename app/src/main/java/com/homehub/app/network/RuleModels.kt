package com.homehub.app.network

data class RuleDeviceRef(val _id: String, val name: String, val type: String)

data class ClauseDto(
    val device: RuleDeviceRef,
    val capability: String,
    val operator: String,
    val value: Any? = null
)

data class ActionDto(
    val type: String, // "device_command" | "notify"
    val device: RuleDeviceRef? = null,
    val capability: String? = null,
    val value: Any? = null,
    val message: String? = null
)

data class RuleDto(
    val _id: String,
    val name: String,
    val enabled: Boolean,
    val trigger: ClauseDto,
    val conditions: List<ClauseDto>,
    val actions: List<ActionDto>,
    val maxChainDepth: Int
)

data class RulesResponse(val rules: List<RuleDto>)
data class RuleResponse(val rule: RuleDto, val warnings: List<String>? = null)

data class CreateClauseRequest(
    val device: String,
    val capability: String,
    val operator: String,
    val value: String? = null
)

data class CreateActionRequest(
    val type: String,
    val device: String? = null,
    val capability: String? = null,
    val value: String? = null,
    val message: String? = null
)

data class CreateRuleRequest(
    val name: String,
    val trigger: CreateClauseRequest,
    val conditions: List<CreateClauseRequest> = emptyList(),
    val actions: List<CreateActionRequest>
)

data class ToggleRuleRequest(val enabled: Boolean)
data class DeleteRuleResponse(val status: String)

val CLAUSE_OPERATORS = listOf(
    "eq" to "equals",
    "neq" to "not equals",
    "gt" to "greater than",
    "lt" to "less than",
    "gte" to "greater or equal",
    "lte" to "less or equal",
    "changed" to "changed (any)"
)