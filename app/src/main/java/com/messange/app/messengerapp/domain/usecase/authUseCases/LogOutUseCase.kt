package com.messange.app.messengerapp.domain.usecase.authUseCases

import com.messange.app.messengerapp.domain.repository.AuthRepository

class LogOutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() {
        authRepository.logOut()
    }
}
