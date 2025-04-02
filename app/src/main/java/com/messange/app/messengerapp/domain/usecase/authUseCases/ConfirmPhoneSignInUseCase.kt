package com.messange.app.messengerapp.domain.usecase.authUseCases

import com.messange.app.messengerapp.domain.model.UserModel
import com.messange.app.messengerapp.domain.repository.AuthRepository

class ConfirmPhoneSignInUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(verificationId: String, code: String): Result<UserModel> {
        return authRepository.confirmPhoneSignIn(verificationId, code)
    }
}