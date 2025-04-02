package com.messange.app.messengerapp.domain.usecase.authUseCases

import com.messange.app.messengerapp.domain.model.UserModel
import com.messange.app.messengerapp.domain.repository.AuthRepository

class LoginWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<UserModel> {
        return authRepository.loginWithEmail(email, password)
    }
}