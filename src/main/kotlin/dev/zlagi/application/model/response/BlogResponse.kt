package dev.zlagi.application.model.response

import dev.zlagi.data.model.BlogDataModel
import kotlinx.serialization.Serializable

@Serializable
data class BlogDomainModel(
    val pk: Int,
    val username: String,
    val title: String,
    val description: String,
    val created: String,
    val updated: String?
) {
    companion object {
        fun fromData(entity: BlogDataModel) =
            BlogDomainModel(
                entity.id,
                entity.username,
                entity.title,
                entity.description,
                entity.created,
                entity.updated
            )
    }
}

@Serializable
data class BlogResponse(
    override val status: State,
    override val message: String,
    val pk: Int = -1,
    val title: String = "",
    val description: String = "",
    val created: String = "",
    val updated: String? = "",
    val username: String = ""
) : Response {
    companion object {

        fun failed(message: String) = BlogResponse(
            State.FAILED,
            message
        )

        fun success(message: String, blog: BlogDomainModel) = BlogResponse(
            State.SUCCESS,
            message,
            blog.pk,
            blog.title,
            blog.description,
            blog.created,
            blog.updated,
            blog.username
        )
    }
}
