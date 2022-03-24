package dev.zlagi.application.router

import dev.zlagi.application.auth.principal.UserPrincipal
import dev.zlagi.application.controller.blog.BlogController
import dev.zlagi.application.controller.blog.BlogController.Companion.ONESIGNAL_APP_ID
import dev.zlagi.application.model.request.BlogRequest
import dev.zlagi.application.model.request.Notification
import dev.zlagi.application.model.request.NotificationMessage
import dev.zlagi.application.model.request.NotificationRequest
import dev.zlagi.application.model.response.generateHttpResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Route.blogApi(httpClient: HttpClient, apiKey: String) {

    val blogController by inject<BlogController>()

    authenticate("jwt") {

        route("/blog") {

            get("/list") {
                val getBlogsRequest = call.request.queryParameters
                val getBlogsResults = blogController.getBlogsByQuery(getBlogsRequest, this.context)
                val response = generateHttpResponse(getBlogsResults)
                call.respond(response.code, response.body)
            }

            post("/notification") {
                val blog = call.receive<NotificationRequest>()
                val username = call.principal<UserPrincipal>()?.user?.username
                val notificationResponse = blogController.sendNotification(httpClient, apiKey, Notification(
                    includedSegments = listOf("All"),
                    headings = NotificationMessage(en = "New post from $username \uD83D\uDD25 \n"),
                    contents = NotificationMessage(en = blog.title),
                    appId = ONESIGNAL_APP_ID
                ))
                val response = generateHttpResponse(notificationResponse)
                call.respond(response.code, response.body)
            }

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
        }
    }

}