package dev.zlagi.application.auth.principal

import dev.zlagi.data.model.User
import io.ktor.auth.*

class UserPrincipal(val user: User) : Principal