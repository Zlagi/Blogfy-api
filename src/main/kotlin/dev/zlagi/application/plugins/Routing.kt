package dev.zlagi.application.plugins

import dev.zlagi.application.route.authApi
import dev.zlagi.application.route.blogApi
import io.ktor.application.*
import io.ktor.routing.*

fun Application.configureRouting() {
    routing {
        authApi()
        blogApi()
    }
}
