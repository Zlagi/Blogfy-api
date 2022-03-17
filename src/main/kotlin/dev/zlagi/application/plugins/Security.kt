package dev.zlagi.application.plugins

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.JWTVerifier
import dev.zlagi.application.auth.firebase.FirebaseConfig.configure
import dev.zlagi.application.auth.firebase.firebase
import dev.zlagi.application.auth.principal.UserPrincipal
import dev.zlagi.application.model.response.GeneralResponse
import dev.zlagi.data.dao.UserDao
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*

fun Application.configureSecurity(
    userDao: UserDao,
    jwtVerifier: JWTVerifier
) {

    install(Authentication) {
        firebase { configure() }

        jwt("jwt") {
            verifier(jwtVerifier)

            // https://stackoverflow.com/questions/62377411/how-do-i-get-access-to-errors-in-custom-ktor-jwt-challenge
            challenge { _, _ ->
                // get custom error message if error exists
                val header = call.request.headers["Authorization"]
                header?.let {
                    if (it.isNotEmpty()) {
                        try {
                            if ((!it.contains("Bearer", true))) throw JWTDecodeException("")
                            val jwt = it.replace("Bearer ", "")
                            jwtVerifier.verify(jwt)
                            ""
                        } catch (e: TokenExpiredException) {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                GeneralResponse.failed("Authentication failed: Access token expired")
                            )
                        } catch (e: SignatureVerificationException) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                GeneralResponse.failed("Authentication failed: Failed to parse Access token")
                            )
                        } catch (e: JWTDecodeException) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                GeneralResponse.failed("Authentication failed: Failed to parse Access token")
                            )
                        }
                    } else call.respond(
                        HttpStatusCode.BadRequest,
                        GeneralResponse.failed("Authentication failed: Access token not found")
                    )
                } ?: call.respond(
                    HttpStatusCode.Unauthorized, GeneralResponse.unauthorized("Authentication failed: No authorization header found")
                )
                GeneralResponse.unauthorized("Unauthorized")
            }

            validate { credential ->
                credential.payload.getClaim("userId").asInt()?.let { userId ->
                    // do database query to find Principal subclass
                    val user = userDao.findByID(userId)
                    user?.let {
                        UserPrincipal(it)
                    }
                }
            }
        }
    }
}
