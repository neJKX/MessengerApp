package com.messange.app.messengerapp.data

import com.messange.app.messengerapp.domain.model.UserModel
import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager {

    private lateinit var sharedPreferences : SharedPreferences
    private const val SHARED_NAME = "app_shared"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE = "email"
    private const val KEY_PASSWORD = "password"

    fun init(context : Context){
        sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(user : UserModel){
        sharedPreferences.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL , user.email)
            .putString(KEY_PHONE , user.phone)
            .putString(KEY_PASSWORD , user.password)
            .apply()
    }

    fun getVerificationId() : String = sharedPreferences.getString("verificationId", "").toString()

    fun saveVerificationId(verificationId : String){
        sharedPreferences.edit().putString("verificationId", verificationId).apply()
    }

    fun saveSecondStep(name : String, sity : String, citate : String){
        sharedPreferences.edit()
            .putString("name", name)
            .putString("sity", sity)
            .putString("citate" , citate)
            .apply()
    }

    fun getUser() : UserModel {
        val id = sharedPreferences.getString(KEY_USER_ID, "").toString()
        val name = sharedPreferences.getString(KEY_NAME, "").toString()
        val email = sharedPreferences.getString(KEY_EMAIL, null).toString()
        val phone = sharedPreferences.getString(KEY_PHONE, null).toString()
        val password = sharedPreferences.getString(KEY_PASSWORD, "").toString()
        val user = UserModel(id, name, email, phone, password)
        return user
    }
}