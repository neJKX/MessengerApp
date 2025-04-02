package com.messange.app.messengerapp.domain.model

data class ChatModel(
    val id: String = "",             // ID чата
    val name: String = "",           // Имя собеседника
    val otherImage: String = "",     // Ссылка на аватар собеседника (храним как URL)
    val otherId: String = "",        // ID собеседника
    val lastMessage: String = ""     // Последнее сообщение
)
