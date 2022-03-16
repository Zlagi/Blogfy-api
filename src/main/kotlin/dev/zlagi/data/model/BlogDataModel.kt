package dev.zlagi.data.model

import dev.zlagi.data.entity.EntityBlog

data class BlogDataModel(
    val id: Int,
    val username: String,
    val title: String,
    val description: String,
    val created: String,
    val updated: String?
) {
    companion object {
        fun fromEntity(entity: EntityBlog) =
            BlogDataModel(entity.id.value,
                entity.username,
                entity.title,
                entity.description,
                entity.created,
                entity.updated)
    }
}