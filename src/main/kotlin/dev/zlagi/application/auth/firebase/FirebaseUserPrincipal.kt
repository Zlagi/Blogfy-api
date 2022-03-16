package dev.zlagi.application.auth.firebase

import io.ktor.auth.*

class FirebaseUserPrincipal(val email: String) : Principal