package dev.zlagi.application.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
    override val status: State,
    override val message: String,
    val id: Int? = null,
    val username: String? = null,
    val email: String? = null
) : Response {
    companion object {

        fun unauthorized(message: String) = AccountResponse(
            State.UNAUTHORIZED,
            message
        )

        fun failed(message: String) = AccountResponse(
            State.FAILED,
            message
        )

        fun notFound(message: String) = AccountResponse(
            State.NOT_FOUND,
            message
        )

        fun success(message: String, userId: Int, email: String, username: String) = AccountResponse(
            State.SUCCESS,
            message,
            userId,
            email,
            username
        )
    }
}