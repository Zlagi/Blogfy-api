package dev.zlagi.application.controller

import dev.zlagi.application.auth.PasswordEncryptorContract
import dev.zlagi.application.auth.TokenProvider
import dev.zlagi.application.exception.BadRequestException
import dev.zlagi.application.exception.UnauthorizedActivityException
import dev.zlagi.application.model.request.SignInRequest
import dev.zlagi.application.model.request.SignUpRequest
import dev.zlagi.application.model.request.UpdatePasswordRequest
import dev.zlagi.application.model.response.AuthResponse
import dev.zlagi.application.utils.isAlphaNumeric
import dev.zlagi.application.utils.isEmailValid
import dev.zlagi.data.dao.TokenDao
import dev.zlagi.data.dao.UserDao
import dev.zlagi.data.model.BlogDataModel
import dev.zlagi.data.model.User
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat

abstract class BaseController : KoinComponent {

    private val userDao by inject<UserDao>()
    private val refreshTokensDao by inject<TokenDao>()
    private val passwordEncryption by inject<PasswordEncryptorContract>()
    private val tokenProvider by inject<TokenProvider>()
    private val simpleDateFormat = SimpleDateFormat("'Date: 'yyyy-MM-dd' Time: 'HH:mm:ss")

    internal fun validateSignInFieldsOrThrowException(
        signInRequest: SignInRequest
    ) {
        val message = when {
            (signInRequest.email.isBlank() or (signInRequest.password.isBlank())) -> "Credentials fields should not be blank"
            (!signInRequest.email.isEmailValid()) -> "Email invalid"
            (signInRequest.password.length !in (8..50)) -> "Password should be of min 8 and max 50 character in length"
            else -> return
        }

        throw BadRequestException(message)
    }

    internal fun validateSignUpFieldsOrThrowException(
        signUpRequest: SignUpRequest
    ) {
        val message = when {
            (signUpRequest.email.isBlank() or (signUpRequest.username.isBlank()) or (signUpRequest.password.isBlank()) or (signUpRequest.confirmPassword.isBlank())) -> "Fields should not be blank"
            (!signUpRequest.email.isEmailValid()) -> "Email invalid"
            (!signUpRequest.username.isAlphaNumeric()) -> "No special characters allowed in username"
            (signUpRequest.username.length !in (4..30)) -> "Username should be of min 4 and max 30 character in length"
            (signUpRequest.password.length !in (8..50)) -> "Password should be of min 8 and max 50 character in length"
            (signUpRequest.confirmPassword.length !in (8..50)) -> "Password should be of min 8 and max 50 character in length"
            (signUpRequest.password != signUpRequest.confirmPassword) -> "Passwords do not match"
            else -> return
        }

        throw BadRequestException(message)
    }

    internal fun validateResetPasswordFieldsOrThrowException(email: String) {
        val message = when {
            (email.isBlank()) -> "Email field should not be blank"
            else -> return
        }

        throw BadRequestException(message)
    }

    internal fun validateUpdatePasswordFieldsOrThrowException(
        updatePasswordRequest: UpdatePasswordRequest
    ) {
        val message = when {
            (updatePasswordRequest.currentPassword.isBlank() || updatePasswordRequest.newPassword.isBlank()
                    || updatePasswordRequest.confirmNewPassword.isBlank()) -> {
                "Password field should not be blank"
            }
            updatePasswordRequest.newPassword != updatePasswordRequest.confirmNewPassword -> "Passwords do not match"
            updatePasswordRequest.newPassword.length !in (8..50) -> "Password should be of min 8 and max 50 character in length"
            updatePasswordRequest.confirmNewPassword.length !in (8..50) -> "Password should be of min 8 and max 50 character in length"
            else -> return
        }

        throw BadRequestException(message)
    }

    internal fun validateTokenParametersOrThrowException(token: String?) {
        if (token == null) throw BadRequestException("Missing token query parameter")
    }

    internal fun validateIdpAuthenticationFieldsOrThrowException(email: String) {
        val message = when {
            (email.isBlank()) -> "Email field should not be blank"
            else -> return
        }

        throw BadRequestException(message)
    }

    internal fun validateRefreshTokenFieldsOrThrowException(token: String) {
        val message = when {
            (token.isBlank()) -> "Token field should not be blank"
            else -> return
        }

        throw BadRequestException(message)
    }

    internal fun validateSignOutFieldsOrThrowException(token: String) {
        val message = when {
            (token.isBlank()) -> "Token field should not be blank"
            else -> return
        }
        throw BadRequestException(message)
    }

    internal fun validateRefreshTokenType(tokenType: String) {
        if (tokenType != "refreshToken") throw BadRequestException("Invalid token type")
    }

    internal fun validateAccessTokenType(tokenType: String) {
        if (tokenType != "accessToken") throw BadRequestException("Invalid token type")
    }

    internal suspend fun verifyTokenRevocation(token: String, userId: String) {
        if (refreshTokensDao.exists(userId, token)) throw UnauthorizedActivityException("Token has been revoked")
    }

    internal fun verifyPasswordOrThrowException(password: String, user: User) {
        user.password?.let {
            if (!passwordEncryption.validatePassword(password, it))
                throw UnauthorizedActivityException("Invalid credentials")
        }
    }

    internal suspend fun storeToken(token: String) {
        val simpleDateFormat = SimpleDateFormat("'Date: 'yyyy-MM-dd' Time: 'HH:mm:ss")
        val expirationTime = tokenProvider.getTokenExpiration(token)
        val convertedExpirationTime = simpleDateFormat.format(expirationTime)

        try {
            tokenProvider.verifyToken(token)?.let { userId ->
                userDao.findByUUID(userId)?.let {
                    refreshTokensDao.store(
                        it.id,
                        token,
                        convertedExpirationTime
                    )
                } ?: throw UnauthorizedActivityException("Invalid credentials")
            } ?: throw UnauthorizedActivityException("Invalid credentials")
        } catch (uae: UnauthorizedActivityException) {
            AuthResponse.unauthorized(uae.message)
        }
    }

    internal suspend fun deleteExpiredTokens(userId: String, currentTime: String) {
        refreshTokensDao.getAllById(userId).let { refreshtokens ->
            refreshtokens.forEach {
                if (it.expirationTime < currentTime) {
                    refreshTokensDao.deleteById(it.id)
                }
            }
        }
    }

    internal fun validateUpdateBlogFields(blogId: Int?, title: String, description: String) {
        val message = when {
            blogId == null -> "Blog id should not be null or empty"
            title.count() < 3 -> "Title must be at least 3 characters"
            description.count() < 7 -> "Description must be at least 8 characters"
            else -> return
        }
        throw BadRequestException(message)
    }

    internal suspend fun verifyEmail(email: String) {
        if (!userDao.isEmailAvailable(email)) {
            throw BadRequestException("Email is not available")
        }
    }

    internal fun getEncryptedPassword(password: String): String {
        return passwordEncryption.encryptPassword(password)
    }

    internal fun getConvertedTokenExpirationTime(token: String): String {
        val expirationTime = tokenProvider.getTokenExpiration(token)
        return simpleDateFormat.format(expirationTime)
    }

    internal fun getConvertedCurrentTime(): String = simpleDateFormat.format((System.currentTimeMillis()))

    internal fun getTokenType(token: String): String {
        return tokenProvider.verifyTokenType(token)
    }

    internal fun checksBlogResult(blogs: List<BlogDataModel>) {
        if (blogs.isEmpty()) throw BadRequestException("No blogs found")
    }

    internal fun checkPageNumber(page: Int, blogs: List<List<BlogDataModel>>) {
        if (!(page > 0 && page <= blogs.size)) throw BadRequestException("Invalid page")
    }

    internal fun calculatePage(
        blogs: List<List<BlogDataModel>>,
        page: Int
    ): Map<String, Int?> {
        val previous = if (page == 1) null else page - 1
        val next = if (page == blogs.size) null else page + 1
        return mapOf(
            "previous" to previous,
            "next" to next
        )
    }

    internal fun provideBlogs(
        blogs: List<List<BlogDataModel>>,
        page: Int
    ): List<BlogDataModel> {
        return blogs[page - 1]
    }

}