package com.messange.app.messengerapp.domain.usecase.authUseCases

import android.app.Activity
import com.messange.app.messengerapp.domain.repository.AuthRepository

class RegisterWithPhoneUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(phone: String, activity: Activity): Result<String> {
        return authRepository.registerWithPhone(phone, activity)
    }
}