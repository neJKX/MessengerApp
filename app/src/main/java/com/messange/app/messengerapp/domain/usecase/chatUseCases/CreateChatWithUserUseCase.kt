package com.messange.app.messengerapp.domain.usecase.chatUseCases

import com.messange.app.messengerapp.domain.repository.ChatRepository

class CreateChatWithUserUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(otherId : String){
        chatRepository.createChatWithUser(otherId)
    }
}