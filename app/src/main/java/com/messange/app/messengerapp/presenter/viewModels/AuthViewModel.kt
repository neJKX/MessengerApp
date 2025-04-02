package com.messange.app.messengerapp.presenter.viewModels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.messange.app.messengerapp.domain.repository.AuthRepository
import com.messange.app.messengerapp.domain.usecase.authUseCases.ConfirmPhoneSignInUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LogOutUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LoginWithEmailUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LoginWithPhoneUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.RegisterWithEmailUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.RegisterWithPhoneUseCase

class AuthViewModel(
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val registerWithPhoneUseCase: RegisterWithPhoneUseCase,
    private val confirmPhoneSignInUseCase: ConfirmPhoneSignInUseCase,
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val loginWithPhoneUseCase: LoginWithPhoneUseCase
) : ViewModel() {

    fun registerWithEmail(email: String, password: String) = liveData {
        emit(registerWithEmailUseCase(email, password))
    }

    fun registerWithPhone(phone: String, activity: Activity) = liveData {
        emit(registerWithPhoneUseCase(phone, activity))
    }

    fun confirmPhoneSignIn(verificationId: String, code: String) = liveData {
        emit(confirmPhoneSignInUseCase(verificationId, code))
    }

    fun loginWithEmail(email: String, password: String) = liveData {
        emit(loginWithEmailUseCase(email, password))
    }

    fun loginWithPhone(verificationId: String, code: String) = liveData {
        emit(loginWithPhoneUseCase(verificationId, code))
    }

}

