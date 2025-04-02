package com.messange.app.messengerapp.domain.model

data class UserModel(
    val id: String = "",
    val name: String? = null,
    val city : String? = null,
    val citate : String? = null,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null
)

