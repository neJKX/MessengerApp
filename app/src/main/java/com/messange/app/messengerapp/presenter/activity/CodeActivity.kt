package com.messange.app.messengerapp.presenter.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.Data
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.messange.app.messengerapp.R
import com.messange.app.messengerapp.data.DataRepositoryImpl
import com.messange.app.messengerapp.data.SharedPrefManager
import com.messange.app.messengerapp.databinding.ActivityCodeBinding
import com.messange.app.messengerapp.domain.model.UserModel
import com.messange.app.messengerapp.domain.usecase.authUseCases.ConfirmPhoneSignInUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LoginWithEmailUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LoginWithPhoneUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.RegisterWithEmailUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.RegisterWithPhoneUseCase
import com.messange.app.messengerapp.presenter.viewModels.AuthViewModel

class CodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCodeBinding
    private val authViewModel by lazy{
        ViewModelProvider(this , object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(
                    registerWithEmailUseCase = RegisterWithEmailUseCase(DataRepositoryImpl()),
                    registerWithPhoneUseCase = RegisterWithPhoneUseCase(DataRepositoryImpl()),
                    confirmPhoneSignInUseCase = ConfirmPhoneSignInUseCase(DataRepositoryImpl()),
                    loginWithEmailUseCase = LoginWithEmailUseCase(DataRepositoryImpl()),
                    loginWithPhoneUseCase = LoginWithPhoneUseCase(DataRepositoryImpl())
                ) as T
            }
        })[AuthViewModel::class.java]
    }
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificationId = SharedPrefManager.getVerificationId() ?: intent.getStringExtra("verificationId")
        Log.d("CodeActivity", "Полученный verificationId: $verificationId")

        if (verificationId == null) {
            Toast.makeText(this, "Ошибка: verificationId отсутствует", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateUI()
    }

    private fun updateUI() {
        val contextCode = intent.getStringExtra("contextCode") ?: "phone"
        binding.phoneLayout.visibility = if (contextCode == "phone") View.VISIBLE else View.GONE

        binding.next.setOnClickListener {
            val code = binding.codeInputPhone.text.toString().trim()
            if (code.isBlank()) {
                Toast.makeText(this, "Введите код", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cleanCode = code.replace(" ", "")
            confirmPhoneSignIn(cleanCode)
        }

        binding.codeInputPhone.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing || s.isNullOrEmpty()) return

                isEditing = true
                val cleanText = s.toString().replace("\\D".toRegex(), "")
                val formattedText = StringBuilder()

                for (i in cleanText.indices) {
                    if (i > 0 && i % 3 == 0) {
                        formattedText.append("   ")
                    }
                    formattedText.append(cleanText[i])
                }

                binding.codeInputPhone.setText(formattedText.toString())
                binding.codeInputPhone.setSelection(formattedText.length)

                isEditing = false
            }
        })

    }

    private fun confirmPhoneSignIn(code: String) {
        val newUser = intent.getBooleanExtra("newUser", false)

        if (verificationId == null) {
            Toast.makeText(this, "Ошибка: отсутствует verificationId", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("CodeActivity", "Передача кода: $code и verificationId: $verificationId в confirmPhoneSignIn")


        authViewModel.confirmPhoneSignIn(verificationId!!, code).observe(this) { result ->
            result.onSuccess { user ->
                Toast.makeText(this, "Телефон подтверждён! Добро пожаловать, ${user.phone}", Toast.LENGTH_LONG).show()
                if (newUser){
                    navigateToSecondStep(user.id)
                }else{
                    navigateToMain(user.id)
                }
            }.onFailure {
                Toast.makeText(this, "Ошибка подтверждения: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e("Error" , it.localizedMessage)
            }
        }
    }

    private fun navigateToSecondStep(userId: String){
        val intent = Intent(this, SecondAuthActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain(userId: String){
        val intent = Intent(this, SecondAuthActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
        finish()
    }
}
