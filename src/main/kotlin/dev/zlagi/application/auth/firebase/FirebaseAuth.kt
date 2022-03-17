package dev.zlagi.application.auth.firebase

import com.google.firebase.ErrorCode
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dev.zlagi.application.model.response.AuthResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val firebaseAuthLogger: Logger = LoggerFactory.getLogger("dev.zlagi.application.auth.firebase")

class FirebaseAuthenticationProvider internal constructor(config: Configuration) : AuthenticationProvider(config) {

    internal val token: (ApplicationCall) -> String? = config.token
    internal val principle: ((email: String) -> Principal?)? = config.principal

    class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name) {

        internal var token: (ApplicationCall) -> String? = { call -> call.request.parseAuthorizationToken() }

        internal var principal: ((email: String) -> Principal?)? = null

        internal fun build() = FirebaseAuthenticationProvider(this)
    }
}

fun Authentication.Configuration.firebase(
    name: String? = null,
    configure: FirebaseAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = FirebaseAuthenticationProvider.Configuration(name).apply(configure).build()
    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->

        try {
            val token = provider.token(call) ?: throw FirebaseAuthException(
                FirebaseException(
                    ErrorCode.UNAUTHENTICATED,
                    "Authentication failed: Firebase token not found",
                    null
                )
            )

            val uid = FirebaseAuth.getInstance().verifyIdToken(token).email

            provider.principle?.let { it.invoke(uid)?.let { principle -> context.principal(principle) } }

        } catch (cause: Throwable) {
            val message = when (cause) {
                is FirebaseAuthException -> {
                    "Authentication failed: Failed to parse Firebase ID token"
                }
                else -> return@intercept
            }

            firebaseAuthLogger.trace(message)
            call.respond(HttpStatusCode.Unauthorized, AuthResponse.failed(message))
            context.challenge.complete()
            finish()
        }
    }
    register(provider)
}

fun ApplicationRequest.parseAuthorizationToken(): String? = authorization()?.let {
    it.split(" ")[1]
}
