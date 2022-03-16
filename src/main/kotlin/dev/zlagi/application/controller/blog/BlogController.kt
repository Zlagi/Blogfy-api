package dev.zlagi.application.controller.blog

import dev.zlagi.application.auth.principal.UserPrincipal
import dev.zlagi.application.controller.BaseController
import dev.zlagi.application.exception.BadRequestException
import dev.zlagi.application.exception.BlogNotFoundException
import dev.zlagi.application.model.request.BlogRequest
import dev.zlagi.application.model.response.*
import dev.zlagi.data.dao.BlogsDao
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultBlogController : BaseController(), BlogController, KoinComponent {

    private val blogDao by inject<BlogsDao>()

    override suspend fun getBlogsByQuery(parameters: Parameters, ctx: ApplicationCall): BlogsResponse {
        return try {
            val page = parameters["page"]?.toInt() ?: 1
            val limit = parameters["limit"]?.toInt() ?: 10
            val search = parameters["search_query"] ?: ""
            val cachedBlogs = blogDao.searchByQuery(search)
            checksBlogResult(cachedBlogs)
            val windowedBlogs = cachedBlogs.windowed(
                size = limit,
                step = limit,
                partialWindows = true
            )
            checkPageNumber(page, windowedBlogs)
            val paginatedBlogs = provideBlogs(windowedBlogs, page)
            BlogsResponse.success(
                Pagination(
                    cachedBlogs.size, page, windowedBlogs.size, Links(
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
            BlogsResponse.notFound(e.message)
        }
    }

    override suspend fun storeBlog(blogRequest: BlogRequest, ctx: ApplicationCall): BlogResponse {
        return try {
            val userId = ctx.principal<UserPrincipal>()?.user?.id
            val username = ctx.principal<UserPrincipal>()?.user?.username
            validateUpdateBlogFields(0, blogRequest.title, blogRequest.description)
            val blog =
                blogDao.store(userId!!, username!!, blogRequest.title, blogRequest.description, getConvertedCurrentTime(), null)
            BlogResponse.success(
                "Created",
                BlogDomainModel.fromData(blog)
            )
        } catch (e: BadRequestException) {
            BlogResponse.notFound(e.message)
        }
    }

    override suspend fun updateBlog(
        blogRequest: BlogRequest,
        ctx: ApplicationCall
    ): BlogResponse {
        return try {
            val blogId = ctx.parameters["blogId"]?.toInt()
            validateUpdateBlogFields(blogId!!, blogRequest.title, blogRequest.description)
            blogDao.update(blogId, blogRequest.title, blogRequest.description, getConvertedCurrentTime()).let {
                BlogResponse.success(
                    "Updated",
                    BlogDomainModel.fromData(it)
                )
            }
        } catch (e: BadRequestException) {
            BlogResponse.failed(e.message)
        } catch (e: IllegalArgumentException) {
            BlogResponse.failed(e.message!!)
        } catch (e: EntityNotFoundException) {
            BlogResponse.notFound(e.message!!)
        }
    }

    override suspend fun deleteBlog(ctx: ApplicationCall): GeneralResponse {
        return try {
            val blogId = ctx.parameters["blogId"]?.toInt()

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

    override suspend fun checkBlogAuthor(ctx: ApplicationCall): GeneralResponse {
        return try {
            val blogId = ctx.parameters["blogId"]?.toInt()
            val userId = ctx.principal<UserPrincipal>()?.user?.id

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
    suspend fun getBlogsByQuery(parameters: Parameters, ctx: ApplicationCall): BlogsResponse
    suspend fun storeBlog(blogRequest: BlogRequest, ctx: ApplicationCall): BlogResponse
    suspend fun updateBlog(blogRequest: BlogRequest, ctx: ApplicationCall): BlogResponse
    suspend fun deleteBlog(ctx: ApplicationCall): GeneralResponse
    suspend fun checkBlogAuthor(ctx: ApplicationCall): GeneralResponse
}