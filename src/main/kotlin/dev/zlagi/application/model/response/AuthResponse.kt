package dev.zlagi.application.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    override val status: State,
    override val message: String,
    val accessToken: String? = null,
    val refreshToken: String? = null
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

        fun success(message: String, access_token: String?, refresh_token: String?) = AuthResponse(
            State.SUCCESS,
            message,
            access_token,
            refresh_token
        )
    }
}
