package dev.zlagi.application.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import dev.zlagi.application.model.response.TokenResponse
import dev.zlagi.data.model.User
import java.util.*

object JWTController : TokenProvider {

    private const val secret = "bkFwb2xpdGE2OTk5"
    private const val issuer = "bkFwb2xpdGE2OTk5"
    private const val validityInMs: Long = 600000L // 10 Minutes
    private const val refreshValidityInMs: Long = 3600000L // 1 Hour
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    override fun verifyToken(token: String): String? {
        return verifier.verify(token).claims["userId"]?.asString()
    }

    override fun getTokenExpiration(token: String): Date {
        return verifier.verify(token).expiresAt
    }

    /**
     * Produce token and refresh token for this combination of User and Account
     */
    override fun createTokens(user: User) = TokenResponse(
        createAccessToken(user, getTokenExpiration()),
        createRefreshToken(user, getTokenExpiration(refreshValidityInMs))
    )

    override fun verifyTokenType(token: String): String {
        return verifier.verify(token).claims["tokenType"]!!.asString()
    }

    private fun createAccessToken(user: User, expiration: Date) = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("userId", user.id)
        .withClaim("tokenType", "accessToken")
        .withExpiresAt(expiration)
        .sign(algorithm)

    private fun createRefreshToken(user: User, expiration: Date) = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("userId", user.id)
        .withClaim("tokenType", "refreshToken")
        .withExpiresAt(expiration)
        .sign(algorithm)

    private fun createResetToken(user: User, expiration: Date) = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("userId", user.id)
        .withClaim("tokenType", "resetToken")
        .withExpiresAt(expiration)
        .sign(algorithm)

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getTokenExpiration(validity: Long = validityInMs) = Date(System.currentTimeMillis() + validity)
}

interface TokenProvider {
    fun createTokens(user: User): TokenResponse
    fun verifyTokenType(token: String): String
    fun verifyToken(token: String): String?
    fun getTokenExpiration(token: String): Date
}
