package com.messange.app.messengerapp.domain.model

data class ChannelModel(
    val id: String = "",            // ID канала
    val name: String = "",          // Название канала
    val ownerId: String = "",       // ID владельца (только он отправляет сообщения)
    val users: List<String> = emptyList(), // Список подписчиков
    val lastMessage: String = ""    // Последнее сообщение
)