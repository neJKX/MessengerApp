package com.messange.app.messengerapp.domain.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.messange.app.messengerapp.domain.model.UserModel

interface AuthRepository {
    suspend fun registerWithEmail(email: String, password : String): Result<UserModel>
    suspend fun registerWithPhone(phone: String, activity: Activity): Result<String>
    suspend fun confirmPhoneSignIn(verificationId: String, code: String): Result<UserModel>
    suspend fun loginWithEmail(email: String, password: String): Result<UserModel>
    suspend fun loginWithPhone(verificationId: String, code: String): Result<UserModel>
    suspend fun logOut()
}
