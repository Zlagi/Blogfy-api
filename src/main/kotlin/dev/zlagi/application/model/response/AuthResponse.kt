package dev.zlagi.application.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    override val status: State,
    override val message: String,
    val access_token: String? = null,
    val refresh_token: String? = null
) : Response {

    companion object {

        fun failed(message: String) = AuthResponse(
            State.FAILED,
            message
        )

        fun unauthorized(message: String) = AuthResponse(
            State.UNAUTHORIZED,
            message
        )

        fun success(message: String) = AuthResponse(
            State.SUCCESS,
            message
        )

        fun success(message: String, accessToken: String?, refreshToken: String?) = AuthResponse(
            State.SUCCESS,
            message,
            accessToken,
            refreshToken
        )
    }
}
