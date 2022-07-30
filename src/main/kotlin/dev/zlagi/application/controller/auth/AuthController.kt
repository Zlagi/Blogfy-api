package dev.zlagi.application.controller.auth

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import dev.zlagi.application.auth.TokenProvider
import dev.zlagi.application.auth.firebase.FirebaseUserPrincipal
import dev.zlagi.application.auth.principal.UserPrincipal
import dev.zlagi.application.controller.BaseController
import dev.zlagi.application.exception.BadRequestException
import dev.zlagi.application.exception.UnauthorizedActivityException
import dev.zlagi.application.model.request.*
import dev.zlagi.application.model.response.AccountResponse
import dev.zlagi.application.model.response.AuthResponse
import dev.zlagi.application.model.response.GeneralResponse
import dev.zlagi.application.model.response.Response
import dev.zlagi.data.dao.TokenDao
import dev.zlagi.data.dao.UserDao
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAuthController : BaseController(), AuthController, KoinComponent {

    private val userDao by inject<UserDao>()
    private val refreshTokensDao by inject<TokenDao>()
    private val tokenProvider by inject<TokenProvider>()
    private var i: Boolean = false

    override suspend fun idpAuthentication(
        idpAuthenticationRequest: IdpAuthenticationRequest,
        ctx: ApplicationCall
    ): Response {
        return try {
            val userEmail = ctx.principal<FirebaseUserPrincipal>()?.email
            userDao.findByEmail(userEmail!!)?.let { user ->
                val tokens = tokenProvider.createTokens(user)
                AuthResponse.success(
                    "Sign in successfully",
                    tokens.access_token,
                    tokens.refresh_token
                )
            } ?: userDao.storeUser(userEmail, idpAuthenticationRequest.username, null).let {
                val tokens = tokenProvider.createTokens(it)
                AuthResponse.success(
                    "Sign up successfully",
                    tokens.access_token,
                    tokens.refresh_token,
                )
            }
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        }
    }

    override suspend fun signIn(signInRequest: SignInRequest): Response {
        return try {
            if (!i) delay(5000)
            i = true
            validateSignInFieldsOrThrowException(signInRequest)
            userDao.findByEmail(signInRequest.email)?.let {
                verifyPasswordOrThrowException(signInRequest.password, it)
                val tokens = tokenProvider.createTokens(it)
                AuthResponse.success(
                    "Sign in successfully",
                    tokens.access_token,
                    tokens.refresh_token
                )
            } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: UnauthorizedActivityException) {
            GeneralResponse.unauthorized(e.message)
        }
    }

    override suspend fun signUp(signUpRequest: SignUpRequest): Response {
        return try {
            validateSignUpFieldsOrThrowException(signUpRequest)
            verifyEmail(signUpRequest.email)
            val encryptedPassword = getEncryptedPassword(signUpRequest.password)
            val user = userDao.storeUser(signUpRequest.email, signUpRequest.username, encryptedPassword)
            val tokens = tokenProvider.createTokens(user)
            AuthResponse.success(
                "Sign up successfully",
                tokens.access_token,
                tokens.refresh_token
            )
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        }
    }

    override suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): Response {
        return try {
            val token = refreshTokenRequest.token
            val expirationTime = getConvertedTokenExpirationTime(token)
            val tokenType = getTokenType(token)
            validateRefreshTokenFieldsOrThrowException(token)
            tokenProvider.verifyToken(token)?.let { userId ->
                validateRefreshTokenType(tokenType)
                verifyTokenRevocation(token, userId)
                deleteExpiredTokens(userId, getConvertedCurrentTime())
                userDao.findByID(userId)?.let {
                    val tokens = tokenProvider.createTokens(it)
                    refreshTokensDao.store(userId, token, expirationTime)
                    AuthResponse.success(
                        "Tokens updated",
                        tokens.access_token,
                        tokens.refresh_token,
                    )
                } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
            } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
        } catch (e: TokenExpiredException) {
            GeneralResponse.failed("Authentication failed: Refresh token expired")
        } catch (e: SignatureVerificationException) {
            GeneralResponse.failed("Authentication failed: Failed to parse Refresh token")
        } catch (e: JWTDecodeException) {
            GeneralResponse.failed("Authentication failed: Failed to parse Refresh token")
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: UnauthorizedActivityException) {
            GeneralResponse.unauthorized(e.message)
        }
    }

    override suspend fun revokeToken(revokeTokenRequest: RevokeTokenRequest): Response {
        return try {
            val token = revokeTokenRequest.token
            validateSignOutFieldsOrThrowException(token)
            tokenProvider.verifyToken(token)?.let { userId ->
                val tokenType = getTokenType(token)
                validateRefreshTokenType(tokenType)
                verifyTokenRevocation(token, userId)
                deleteExpiredTokens(userId, getConvertedCurrentTime())
                userDao.findByID(userId)?.let {
                    storeToken(token)
                    GeneralResponse.success(
                        "Sign out successfully"
                    )
                } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
            } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
        } catch (e: TokenExpiredException) {
            GeneralResponse.success("Revocation success: Refresh token already expired")
        } catch (e: SignatureVerificationException) {
            GeneralResponse.success("Revocation failed: Failed to parse Refresh token")
        } catch (e: JWTDecodeException) {
            GeneralResponse.success("Revocation failed: Failed to parse Refresh token")
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: UnauthorizedActivityException) {
            GeneralResponse.unauthorized(e.message)
        }
    }

    override suspend fun getAccountById(ctx: ApplicationCall): Response {
        return try {
            val userId = ctx.principal<UserPrincipal>()?.user?.id
            userDao.findByID(userId!!)?.let {
                AccountResponse.success("User found", it.id, it.username, it.email)
            } ?: throw UnauthorizedActivityException("User do not exist")
        } catch (e: UnauthorizedActivityException) {
            GeneralResponse.notFound(e.message)
        }
    }

    override suspend fun updateAccountPassword(
        updatePasswordRequest: UpdatePasswordRequest,
        ctx: ApplicationCall
    ): Response {
        return try {
            val userId = ctx.principal<UserPrincipal>()?.user?.id
            val encryptedPassword = getEncryptedPassword(updatePasswordRequest.currentPassword)
            validateUpdatePasswordFieldsOrThrowException(updatePasswordRequest)
            userDao.findByID(userId!!)?.let { user ->
                verifyPasswordOrThrowException(updatePasswordRequest.currentPassword, user)
                userDao.updatePassword(user.id, encryptedPassword)
                GeneralResponse.success(
                    "Password updated",
                )
            } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: UnauthorizedActivityException) {
            GeneralResponse.failed(e.message)
        }
    }

    override suspend fun resetPassword(userEmail: String): Response {
        return try {
            validateResetPasswordFieldsOrThrowException(userEmail)
            userDao.findByEmail(userEmail)?.let {
                val token = tokenProvider.createTokens(it)
                val email = SimpleEmail()
                email.hostName = "smtp-mail.outlook.com"
                email.setSmtpPort(587)
                email.setAuthenticator(
                    DefaultAuthenticator(
                        "",
                        ""
                    )
                )
                email.isStartTLSEnabled = true
                email.setFrom("")
                email.subject = "Complete Password Reset!"
                email.setMsg(
                    "To complete the password reset process, " +
                            "please click here: \n https://blogfy-server.herokuapp.com/auth/confirm-reset-password?token=${token.access_token}"
                )
                email.addTo("")
                email.send()
                GeneralResponse.success(
                    "Request to reset password received. Check your inbox for the reset link.",
                )
            } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: UnauthorizedActivityException) {
            GeneralResponse.unauthorized(e.message)
        }
    }

    override suspend fun confirmPasswordReset(
        tokenParameters: Parameters,
        updatePasswordRequest: UpdatePasswordRequest
    ): Response {
        return try {
            val token = tokenParameters["token"]
            validateTokenParametersOrThrowException(token)
            tokenProvider.verifyToken(token!!)?.let { userId ->
                validateUpdatePasswordFieldsOrThrowException(updatePasswordRequest)
                val encryptedPassword = getEncryptedPassword(updatePasswordRequest.currentPassword)
                verifyTokenRevocation(token, userId)
                validateAccessTokenType(getTokenType(token))
                userDao.findByID(userId)?.let { user ->
                    verifyPasswordOrThrowException(updatePasswordRequest.currentPassword, user)
                    storeToken(token)
                    userDao.updatePassword(userId, encryptedPassword)
                    GeneralResponse.success(
                        "Password updated"
                    )
                } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
            } ?: throw UnauthorizedActivityException("Authentication failed: Invalid credentials")
        } catch (e: TokenExpiredException) {
            GeneralResponse.failed("Reset link has been revoked")
        } catch (e: SignatureVerificationException) {
            GeneralResponse.failed("Authentication failed: Failed to parse token")
        } catch (e: JWTDecodeException) {
            GeneralResponse.failed("Authentication failed: Failed to parse token")
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: UnauthorizedActivityException) {
            GeneralResponse.unauthorized(e.message)
        }
    }
}

interface AuthController {
    suspend fun idpAuthentication(
        idpAuthenticationRequest: IdpAuthenticationRequest,
        ctx: ApplicationCall
    ): Response

    suspend fun signIn(signInRequest: SignInRequest): Response
    suspend fun signUp(signUpRequest: SignUpRequest): Response
    suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): Response
    suspend fun revokeToken(revokeTokenRequest: RevokeTokenRequest): Response
    suspend fun getAccountById(ctx: ApplicationCall): Response
    suspend fun updateAccountPassword(
        updatePasswordRequest: UpdatePasswordRequest,
        ctx: ApplicationCall
    ): Response

    suspend fun resetPassword(userEmail: String): Response
    suspend fun confirmPasswordReset(
        tokenParameters: Parameters,
        updatePasswordRequest: UpdatePasswordRequest
    ): Response
}