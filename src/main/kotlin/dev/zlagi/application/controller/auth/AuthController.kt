package dev.zlagi.application.controller.auth

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import dev.zlagi.application.auth.TokenProvider
import dev.zlagi.application.auth.principal.UserPrincipal
import dev.zlagi.application.controller.BaseController
import dev.zlagi.application.exception.BadRequestException
import dev.zlagi.application.exception.UnauthorizedActivityException
import dev.zlagi.application.model.request.*
import dev.zlagi.application.model.response.AccountResponse
import dev.zlagi.application.model.response.AuthResponse
import dev.zlagi.application.model.response.GeneralResponse
import dev.zlagi.data.dao.TokenDao
import dev.zlagi.data.dao.UserDao
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAuthController : BaseController(), AuthController, KoinComponent {

    private val userDao by inject<UserDao>()
    private val refreshTokensDao by inject<TokenDao>()
    private val tokenProvider by inject<TokenProvider>()

    override suspend fun idpAuthentication(idpAuthenticationRequest: IdpAuthenticationRequest): AuthResponse {
        return try {
            validateIdpAuthenticationFieldsOrThrowException(idpAuthenticationRequest.email)
            userDao.findByEmail(idpAuthenticationRequest.email)?.let { user ->
                val tokens = tokenProvider.createTokens(user)
                AuthResponse.success(
                    "Sign in successfully",
                    tokens.access_token,
                    tokens.refresh_token
                )
            } ?: userDao.storeUser(idpAuthenticationRequest.email, idpAuthenticationRequest.username, null).let {
                val tokens = tokenProvider.createTokens(it)
                AuthResponse.success(
                    "Sign up successfully",
                    tokens.access_token,
                    tokens.refresh_token,
                )
            }
        } catch (e: BadRequestException) {
            AuthResponse.failed(e.message)
        }
    }

    override suspend fun signIn(signInRequest: SignInRequest): AuthResponse {
        return try {
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
            AuthResponse.failed(e.message)
        } catch (e: UnauthorizedActivityException) {
            AuthResponse.unauthorized(e.message)
        }
    }

    override suspend fun signUp(signUpRequest: SignUpRequest): AuthResponse {
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
            AuthResponse.failed(e.message)
        }
    }

    override suspend fun signOut(revokeTokenRequest: RevokeTokenRequest): GeneralResponse {
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

    override suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): AuthResponse {
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
            AuthResponse.failed("Authentication failed: Refresh token expired")
        } catch (e: SignatureVerificationException) {
            AuthResponse.failed("Authentication failed: Failed to parse Refresh token")
        } catch (e: JWTDecodeException) {
            AuthResponse.failed("Authentication failed: Failed to parse Refresh token")
        } catch (e: BadRequestException) {
            AuthResponse.failed(e.message)
        } catch (e: UnauthorizedActivityException) {
            AuthResponse.unauthorized(e.message)
        }
    }

    override suspend fun getAccountById(ctx: ApplicationCall): AccountResponse {
        return try {
            val userId = ctx.principal<UserPrincipal>()?.user?.id
            userDao.findByID(userId!!)?.let {
                AccountResponse.success("User found", it.id, it.username, it.email)
            } ?: throw UnauthorizedActivityException("User do not exist")
        } catch (e: UnauthorizedActivityException) {
            AccountResponse.notFound(e.message)
        }
    }

    override suspend fun updateAccountPassword(
        updatePasswordRequest: UpdatePasswordRequest,
        ctx: ApplicationCall
    ): GeneralResponse {
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
            GeneralResponse.success(e.message)
        }
    }

    override suspend fun resetPassword(userEmail: String): GeneralResponse {
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
    ): GeneralResponse {
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
    suspend fun idpAuthentication(idpAuthenticationRequest: IdpAuthenticationRequest): AuthResponse
    suspend fun signIn(signInRequest: SignInRequest): AuthResponse
    suspend fun signUp(signUpRequest: SignUpRequest): AuthResponse
    suspend fun signOut(revokeTokenRequest: RevokeTokenRequest): GeneralResponse
    suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): AuthResponse
    suspend fun getAccountById(ctx: ApplicationCall): AccountResponse
    suspend fun updateAccountPassword(
        updatePasswordRequest: UpdatePasswordRequest,
        ctx: ApplicationCall
    ): GeneralResponse

    suspend fun resetPassword(userEmail: String): GeneralResponse
    suspend fun confirmPasswordReset(
        tokenParameters: Parameters,
        updatePasswordRequest: UpdatePasswordRequest
    ): GeneralResponse
}