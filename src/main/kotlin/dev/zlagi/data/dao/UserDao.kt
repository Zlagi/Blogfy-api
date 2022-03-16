package dev.zlagi.data.dao

import dev.zlagi.data.model.User

interface UserDao {
    suspend fun storeUser(email: String, username: String, password: String?): User
    suspend fun findByUUID(userId: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun isEmailAvailable(email: String): Boolean
    suspend fun updatePassword(userId: String, password: String)
}
