package dev.zlagi.application.plugins

import dev.zlagi.application.router.authApi
import dev.zlagi.application.router.blogApi
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.routing.*

fun Application.configureRouting(httpClient: HttpClient, apiKey: String) {
    routing {
        authApi()
        blogApi(httpClient, apiKey)
    }
}
