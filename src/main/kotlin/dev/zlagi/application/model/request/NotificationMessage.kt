package dev.zlagi.application.model.request

import kotlinx.serialization.Serializable

@Serializable
data class NotificationMessage(
    val en: String
)