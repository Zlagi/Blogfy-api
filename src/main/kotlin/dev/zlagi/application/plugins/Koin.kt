package dev.zlagi.application.plugins

import com.auth0.jwt.interfaces.JWTVerifier
import dev.zlagi.application.auth.JWTController
import dev.zlagi.application.auth.PasswordEncryptor
import dev.zlagi.application.auth.PasswordEncryptorContract
import dev.zlagi.application.auth.TokenProvider
import dev.zlagi.application.controller.di.ControllerModule
import dev.zlagi.data.di.DaoModule
import io.ktor.application.*
import org.koin.core.annotation.KoinReflectAPI
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger

@KoinReflectAPI
fun Application.configureKoin() {

    install(feature = Koin) {
        slf4jLogger(level = org.koin.core.logger.Level.ERROR) //This params are the workaround itself
        modules(
            module {
                single<JWTVerifier> { JWTController.verifier }
                single<TokenProvider> { JWTController }
                single<PasswordEncryptorContract> { PasswordEncryptor }
            },
            DaoModule.koinBeans,
            ControllerModule.koinBeans
        )
    }
}
