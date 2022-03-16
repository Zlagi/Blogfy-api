package dev.zlagi.application.route

import dev.zlagi.application.controller.auth.AuthController
import dev.zlagi.application.model.request.*
import dev.zlagi.application.model.response.generateHttpResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Route.authApi() {

    val authController by inject<AuthController>()

    route("/auth") {

        authenticate {

            route("/idp") {

                post("/google") {
                    val idpAuthenticationRequest = call.receive<IdpAuthenticationRequest>()
                    val idpAuthenticationResponse = authController.idpAuthentication(idpAuthenticationRequest)
                    val response = generateHttpResponse(idpAuthenticationResponse)
                    call.respond(response.code, response.body)
                }
            }
        }

        post("/signin") {
            val signInRequest = call.receive<SignInRequest>()
            val signInResponse = authController.signIn(signInRequest)
            val response = generateHttpResponse(signInResponse)
            call.respond(response.code, response.body)
        }

        post("/signup") {
            val signUpRequest = call.receive<SignUpRequest>()
            val signUpResponse = authController.signUp(signUpRequest)
            val response = generateHttpResponse(signUpResponse)
            call.respond(response.code, response.body)
        }

        post("/sign-out") {
            val revokeTokenRequest = call.receive<RevokeTokenRequest>()
            val revokeTokenResponse =
                authController.signOut(revokeTokenRequest)
            val response = generateHttpResponse(revokeTokenResponse)
            call.respond(response.code, response.body)
        }

        post("/refresh-token") {
            val refreshTokenRequest = call.receive<RefreshTokenRequest>()
            val refreshTokenResponse =
                authController.refreshToken(refreshTokenRequest)
            val response = generateHttpResponse(refreshTokenResponse)
            call.respond(response.code, response.body)
        }

        route("/account") {

            authenticate("jwt") {

                get {
                    val accountResponse = authController.getAccountById(this.context)
                    val response = generateHttpResponse(accountResponse)
                    call.respond(response.code, response.body)
                }

                put("/password") {
                    val updatePasswordRequest = call.receive<UpdatePasswordRequest>()
                    val updatePasswordResponse =
                        authController.updateAccountPassword(
                            updatePasswordRequest,
                            this.context
                        )
                    val response = generateHttpResponse(updatePasswordResponse)
                    call.respond(response.code, response.body)
                }
            }
        }

        post("/reset-password") {
            val resetPasswordRequest = call.receive<ResetPasswordRequest>()
            val passwordResetResponse =
                authController.resetPassword(resetPasswordRequest.email)
            val response = generateHttpResponse(passwordResetResponse)
            call.respond(response.code, response.body)
        }

        post("confirm-reset-password") {
            val tokenParameters = call.request.queryParameters
            val resetPasswordRequest = call.receive<UpdatePasswordRequest>()
            val resetPasswordResponse =
                authController.confirmPasswordReset(
                    tokenParameters,
                    resetPasswordRequest
                )
            val response = generateHttpResponse(resetPasswordResponse)
            call.respond(response.code, response.body)
        }
    }

}