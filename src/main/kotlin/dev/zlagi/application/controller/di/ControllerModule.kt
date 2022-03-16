package dev.zlagi.application.controller.di

import dev.zlagi.application.controller.auth.AuthController
import dev.zlagi.application.controller.auth.DefaultAuthController
import dev.zlagi.application.controller.blog.BlogController
import dev.zlagi.application.controller.blog.DefaultBlogController
import org.koin.dsl.module

object ControllerModule {
    val koinBeans = module {
        single<AuthController> { DefaultAuthController() }
        single<BlogController> { DefaultBlogController() }
    }
}