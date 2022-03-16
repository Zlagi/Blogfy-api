package dev.zlagi.application.model.response

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(val access_token: String, val refresh_token: String)
