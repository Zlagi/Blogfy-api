package dev.zlagi.application.model.response

import kotlinx.serialization.Serializable

@Serializable
data class BlogsResponse(
    override val status: State,
    override val message: String,
    val pagination: Pagination? = null,
    val results: List<BlogDomainModel> = emptyList()
) : Response {
    companion object {

        fun failed(message: String) = BlogsResponse(
            State.FAILED,
            message
        )

        fun notFound(message: String) = BlogsResponse(
            State.NOT_FOUND,
            message
        )

        fun success(pagination: Pagination?, blogs: List<BlogDomainModel>, message: String) =
            BlogsResponse(
                State.SUCCESS,
                message,
                pagination,
                blogs,
            )
    }
}

@Serializable
data class Pagination(
    val total_count: Int,
    val current_page: Int,
    val total_pages: Int,
    val _links: Links
)

@Serializable
data class Links(
    val previous: Int? = null,
    val next: Int? = null
)
