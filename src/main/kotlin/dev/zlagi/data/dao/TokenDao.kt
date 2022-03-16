package dev.zlagi.data.dao

import dev.zlagi.data.model.Token

interface TokenDao {
    suspend fun store(
        userId: String, token: String, expirationTime: String
    ): String
    suspend fun getAllById(userId: String): List<Token>
    suspend fun exists(userId: String, token: String): Boolean
    suspend fun deleteById(tokenId: String): Boolean
}