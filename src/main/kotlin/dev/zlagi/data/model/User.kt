package dev.zlagi.data.model

import dev.zlagi.data.entity.EntityUser

data class User(
    val id: String,
    val email: String,
    val username: String,
    val password: String?
) {
    companion object {
        fun fromEntity(entity: EntityUser) = User(entity.id.value.toString(), entity.email, entity.username, entity.password)
    }
}