package dev.zlagi.application.route

import dev.zlagi.application.controller.blog.BlogController
import dev.zlagi.application.model.request.BlogRequest
import dev.zlagi.application.model.response.generateHttpResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Route.blogApi() {

    val blogController by inject<BlogController>()

    authenticate("jwt") {

        route("/blog") {

            post {
                val createBlogRequest = call.receive<BlogRequest>()
                val createBlogResponse = blogController.storeBlog(createBlogRequest, this.context)
                val response = generateHttpResponse(createBlogResponse)
                call.respond(response.code, response.body)
            }

            put("/{blogId}") {
                val updateBlogRequest = call.receive<BlogRequest>()
                val updateBlogResponse = blogController.updateBlog(updateBlogRequest, this.context)
                val response = generateHttpResponse(updateBlogResponse)
                call.respond(response.code, response.body)
            }

            delete("/{blogId}") {
                val deleteBlogResponse = blogController.deleteBlog(this.context)
                val response = generateHttpResponse(deleteBlogResponse)
                call.respond(response.code, response.body)
            }

            delete("/{blogId}") {
                val deleteBlogResponse = blogController.deleteBlog(this.context)
                val response = generateHttpResponse(deleteBlogResponse)
                call.respond(response.code, response.body)
            }

            get("{blogId}/is_author") {
                val checkAuthorResponse = blogController.checkBlogAuthor(this.context)
                val response = generateHttpResponse(checkAuthorResponse)
                call.respond(response.code, response.body)
            }

            get("/list") {
                val getBlogsRequest = call.request.queryParameters
                val getBlogsResults = blogController.getBlogsByQuery(getBlogsRequest, this.context)
                val response = generateHttpResponse(getBlogsResults)
                call.respond(response.code, response.body)
            }
        }
    }

}