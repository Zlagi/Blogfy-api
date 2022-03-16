package dev.zlagi.data.entity

import dev.zlagi.data.database.table.Users
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class EntityUser(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<EntityUser>(Users)

    var email by Users.email
    var username by Users.username
    var password by Users.password
}