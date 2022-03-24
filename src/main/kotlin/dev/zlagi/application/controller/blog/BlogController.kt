package dev.zlagi.application.controller.blog

import dev.zlagi.application.auth.principal.UserPrincipal
import dev.zlagi.application.controller.BaseController
import dev.zlagi.application.exception.BadRequestException
import dev.zlagi.application.exception.BlogNotFoundException
import dev.zlagi.application.model.request.BlogRequest
import dev.zlagi.application.model.request.Notification
import dev.zlagi.application.model.response.*
import dev.zlagi.data.dao.BlogsDao
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultBlogController : BaseController(), BlogController, KoinComponent {

    private val blogDao by inject<BlogsDao>()

    override suspend fun getBlogsByQuery(parameters: Parameters, ctx: ApplicationCall): Response {
        return try {
            val header = ctx.request.headers["Authorization"]
            header?.replace("Bearer ", "")?.let { token ->
                validateAccessTokenType(getTokenType(token))
            }
            val page = parameters["page"]?.toInt() ?: 1
            val limit = parameters["limit"]?.toInt() ?: 10
            val search = parameters["search_query"] ?: ""
            val blogs = blogDao.searchByQuery(search)
            val windowedBlogs = blogs.windowed(
                size = limit,
                step = limit,
                partialWindows = true
            )
            checkPageNumber(page, windowedBlogs)
            val paginatedBlogs = provideBlogs(windowedBlogs, page)
            BlogsResponse.success(
                Pagination(
                    blogs.size, page, windowedBlogs.size, Links(
                        calculatePage(windowedBlogs, page)["previous"],
                        calculatePage(windowedBlogs, page)["next"]
                    )
                ),
                paginatedBlogs.map {
                    BlogDomainModel(
                        it.id, it.username, it.title, it.description, it.created, it.updated
                    )
                },
                "Blogs found"
            )
        } catch (e: BadRequestException) {
            GeneralResponse.success(e.message)
        }
    }

    override suspend fun sendNotification(httpClient: HttpClient, apiKey: String, notification: Notification): Response {
        return try {
            httpClient.post<String>{
                url(BlogController.NOTIFICATIONS)
                contentType(ContentType.Application.Json)
                header("Authorization", "Basic $apiKey")
                body = notification
            }
            GeneralResponse.success("Notification sent")
        } catch (e: Exception) {
            e.printStackTrace()
            GeneralResponse.failed("Error occurred")
        }
    }

    override suspend fun storeBlog(blogRequest: BlogRequest, ctx: ApplicationCall): Response {
        return try {
            val header = ctx.request.headers["Authorization"]
            header?.replace("Bearer ", "")?.let { token ->
                validateAccessTokenType(getTokenType(token))
            }
            val userId = ctx.principal<UserPrincipal>()?.user?.id
            val username = ctx.principal<UserPrincipal>()?.user?.username
            validateCreateBlogFields(0, blogRequest.title, blogRequest.description, blogRequest.creationTime)
            val blog =
                blogDao.store(
                    userId!!,
                    username!!,
                    blogRequest.title,
                    blogRequest.description,
                    blogRequest.creationTime,
                    null
                )
            BlogResponse.success(
                "Created",
                BlogDomainModel.fromData(blog)
            )
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        }
    }

    override suspend fun updateBlog(
        blogRequest: BlogRequest,
        ctx: ApplicationCall
    ): Response {
        return try {
            val blogId = ctx.parameters["blogId"]?.toInt()
            val header = ctx.request.headers["Authorization"]
            header?.replace("Bearer ", "")?.let { token ->
                validateAccessTokenType(getTokenType(token))
            }
            val updateTime = blogRequest.creationTime.ifEmpty { null }
            validateUpdateBlogFields(blogId!!, blogRequest.title, blogRequest.description)
            blogDao.update(blogId, blogRequest.title, blogRequest.description, updateTime).let {
                BlogResponse.success(
                    "Updated",
                    BlogDomainModel.fromData(it)
                )
            }
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: IllegalArgumentException) {
            GeneralResponse.failed(e.message!!)
        } catch (e: EntityNotFoundException) {
            GeneralResponse.notFound("Blog not exist with ID '${ctx.parameters["blogId"]?.toInt()}'")
        }
    }

    override suspend fun deleteBlog(ctx: ApplicationCall): Response {
        return try {
            val blogId = ctx.parameters["blogId"]?.toInt()
            val header = ctx.request.headers["Authorization"]
            header?.replace("Bearer ", "")?.let { token ->
                validateAccessTokenType(getTokenType(token))
            }
            if (!blogDao.exists(blogId!!)) {
                throw BlogNotFoundException("Blog not exist with ID '$blogId'")
            }
            if (blogDao.deleteById(blogId)) {
                GeneralResponse.success(
                    "Deleted"
                )
            } else {
                GeneralResponse.failed(
                    "Error occured $blogId",
                )
            }
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: BlogNotFoundException) {
            GeneralResponse.notFound(e.message)
        } catch (e: IllegalArgumentException) {
            GeneralResponse.failed(e.message!!)
        }
    }

    override suspend fun checkBlogAuthor(ctx: ApplicationCall): Response {
        return try {
            val blogId = ctx.parameters["blogId"]?.toInt()
            val userId = ctx.principal<UserPrincipal>()?.user?.id
            val header = ctx.request.headers["Authorization"]
            header?.replace("Bearer ", "")?.let { token ->
                validateAccessTokenType(getTokenType(token))
            }
            if (blogDao.isBlogAuthor(blogId!!, userId!!)) {
                GeneralResponse.success(
                    "You have permission to edit that"
                )
            } else {
                GeneralResponse.success(
                    "You don't have permission to edit that"
                )
            }
        } catch (e: BadRequestException) {
            GeneralResponse.failed(e.message)
        } catch (e: BlogNotFoundException) {
            GeneralResponse.notFound(e.message)
        } catch (e: IllegalArgumentException) {
            GeneralResponse.failed(e.message!!)
        }
    }
}

interface BlogController {
    suspend fun getBlogsByQuery(parameters: Parameters, ctx: ApplicationCall): Response
    suspend fun sendNotification(httpClient: HttpClient, apiKey: String, notification: Notification): Response
    suspend fun storeBlog(blogRequest: BlogRequest, ctx: ApplicationCall): Response
    suspend fun updateBlog(blogRequest: BlogRequest, ctx: ApplicationCall): Response
    suspend fun deleteBlog(ctx: ApplicationCall): Response
    suspend fun checkBlogAuthor(ctx: ApplicationCall): Response

    companion object {
        const val ONESIGNAL_APP_ID = "6679eba8-ba98-43da-ba87-9a9c7457bd33"
        const val NOTIFICATIONS = "https://onesignal.com/api/v1/notifications"
    }
}