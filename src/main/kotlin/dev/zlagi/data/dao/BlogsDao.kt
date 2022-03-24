package dev.zlagi.data.dao

import dev.zlagi.data.model.BlogDataModel

interface BlogsDao {
    suspend fun store(
        userId: Int,
        username: String,
        title: String,
        description: String,
        created: String,
        updated: String?
    ): BlogDataModel

    suspend fun searchByQuery(query: String): List<BlogDataModel>
    suspend fun update(blogId: Int, title: String, description: String, updated: String?): BlogDataModel
    suspend fun deleteById(blogId: Int): Boolean
    suspend fun isBlogAuthor(blogId: Int, userId: Int): Boolean
    suspend fun exists(blogId: Int): Boolean
}