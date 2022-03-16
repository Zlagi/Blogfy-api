package dev.zlagi.application.auth.firebase

import kotlinx.coroutines.runBlocking

object FirebaseConfig {
    fun FirebaseAuthenticationProvider.Configuration.configure() {
        principal = { email ->
            //this is where you'd make a db call to fetch your User profile
            runBlocking { FirebaseUserPrincipal(email) }
        }
    }
}