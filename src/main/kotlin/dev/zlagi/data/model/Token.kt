package dev.zlagi.data.model

import dev.zlagi.data.entity.EntityToken

data class Token(
    val id: String,
    val token: String,
    val expirationTime: String
) {
    companion object {
        fun fromEntity(entity: EntityToken) =
            Token(
                entity.id.value.toString(),
                entity.token,
                entity.expirationTime
            )
    }
}