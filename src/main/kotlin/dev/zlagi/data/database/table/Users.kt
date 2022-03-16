package dev.zlagi.data.database.table

import dev.zlagi.data.dao.UserDao
import dev.zlagi.data.entity.EntityUser
import dev.zlagi.data.model.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

object Users : UUIDTable(), UserDao {
    val email = varchar("email", length = 30).uniqueIndex()
    val username = varchar("username", length = 30)
    val password = text("password").nullable()

    override suspend fun storeUser(email: String, username: String, password: String?): User =
        newSuspendedTransaction(Dispatchers.IO) {
            EntityUser.new {
                this.email = email
                this.username = username
                this.password = password
            }.let {
                User.fromEntity(it)
            }
        }

    override suspend fun findByUUID(userId: String): User? =
        newSuspendedTransaction(Dispatchers.IO) {
            EntityUser.findById(UUID.fromString(userId))
        }?.let {
            User.fromEntity(it)
        }

    override suspend fun findByEmail(email: String): User? =
        newSuspendedTransaction(Dispatchers.IO) {
            EntityUser.find {
                (Users.email eq email)
            }.firstOrNull()
        }?.let { User.fromEntity(it) }

    override suspend fun isEmailAvailable(email: String): Boolean {
        return newSuspendedTransaction(Dispatchers.IO) {
            EntityUser.find { Users.email eq email }.firstOrNull()
        } == null
    }

    override suspend fun updatePassword(userId: String, password: String) {
        newSuspendedTransaction(Dispatchers.IO) {
            EntityUser[UUID.fromString(userId)].apply {
                this.password = password
            }.id.value.toString()
        }
    }
}
