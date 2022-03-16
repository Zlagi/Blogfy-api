package dev.zlagi.data.dao

import dev.zlagi.data.model.BlogDataModel

interface BlogsDao {
    suspend fun store(
        userId: String,
        username: String,
        title: String,
        description: String,
        created: String,
        updated: String?
    ): BlogDataModel

    suspend fun searchByQuery(query: String): List<BlogDataModel>
    suspend fun update(blogId: String, title: String, description: String, updated: String): BlogDataModel
    suspend fun deleteById(blogId: String): Boolean
    suspend fun isBlogAuthor(blogId: String, userId: String): Boolean
    suspend fun exists(id: String): Boolean
}