package com.messange.app.messengerapp.domain.usecase.authUseCases

import com.google.firebase.firestore.auth.User
import com.messange.app.messengerapp.domain.model.UserModel
import com.messange.app.messengerapp.domain.repository.AuthRepository

class RegisterWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password : String): Result<UserModel> {
        return authRepository.registerWithEmail(email, password)
    }
}
