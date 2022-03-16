package dev.zlagi.application.model.request

import kotlinx.serialization.Serializable

@Serializable
data class RevokeTokenRequest(
    val token: String
)