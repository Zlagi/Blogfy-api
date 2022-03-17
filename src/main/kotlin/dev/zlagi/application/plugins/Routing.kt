package dev.zlagi.application.plugins

import dev.zlagi.application.router.authApi
import dev.zlagi.application.router.blogApi
import io.ktor.application.*
import io.ktor.routing.*

fun Application.configureRouting() {
    routing {
        authApi()
        blogApi()
    }
}
