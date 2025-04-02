package com.messange.app.messengerapp.presenter.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.firestore
import com.messange.app.messengerapp.R
import com.messange.app.messengerapp.data.DataRepositoryImpl
import com.messange.app.messengerapp.databinding.ActivityAuthorizationBinding
import com.messange.app.messengerapp.domain.usecase.authUseCases.ConfirmPhoneSignInUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LogOutUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LoginWithEmailUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LoginWithPhoneUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.RegisterWithEmailUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.RegisterWithPhoneUseCase
import com.messange.app.messengerapp.presenter.viewModels.AuthViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthorizationActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAuthorizationBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navigation()
        updateUI()
    }

    private fun navigation(){
        binding.goLogin.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.goLoginEmail.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun updateUI(){
        binding.phoneInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isNotEmpty = !s.isNullOrBlank()
                binding.next.isEnabled = isNotEmpty
                binding.next.setBackgroundResource(if (isNotEmpty) R.drawable.green_drawble else R.drawable.gray_drawble)
                binding.next.setTextColor(ContextCompat.getColor(this@AuthorizationActivity, if (isNotEmpty) R.color.white else R.color.gray))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isNotEmpty = !s.isNullOrBlank()
                binding.nextEmail.isEnabled = isNotEmpty
                binding.nextEmail.setBackgroundResource(if (isNotEmpty) R.drawable.green_drawble else R.drawable.gray_drawble)
                binding.nextEmail.setTextColor(ContextCompat.getColor(this@AuthorizationActivity, if (isNotEmpty) R.color.white else R.color.gray))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.goEmail.setOnClickListener {
            binding.emailLayout.visibility = View.VISIBLE
            binding.phoneLayout.visibility = View.GONE
        }

        binding.goPhone.setOnClickListener {
            binding.emailLayout.visibility = View.GONE
            binding.phoneLayout.visibility = View.VISIBLE
        }

        binding.nextEmail.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isBlank()) {
                Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.registerWithEmail(email, password).observe(this) { registerResult ->
                registerResult.onSuccess {
                    val intent = Intent(this, SecondAuthActivity::class.java)
                    startActivity(intent)
                }.onFailure {
                    Toast.makeText(this, "Ошибка: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.next.setOnClickListener {
            val phone = binding.phoneInput.text.toString()

            if (phone.isBlank()) {
                Toast.makeText(this, "Введите номер телефона", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val userCollection = Firebase.firestore.collection("users")
                    val querySnapshot = userCollection.whereEqualTo("phone", phone).get().await()

                    val newUser = querySnapshot.isEmpty
                    authViewModel.registerWithPhone(phone, this@AuthorizationActivity).observe(this@AuthorizationActivity) { result ->
                        result.onSuccess {
                            val intent = Intent(this@AuthorizationActivity, CodeActivity::class.java)
                            intent.putExtra("contextCode", "phone")
                            intent.putExtra("newUser", newUser)
                            startActivity(intent)
                        }.onFailure {
                            Toast.makeText(this@AuthorizationActivity, "Ошибка: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AuthorizationActivity, "Ошибка запроса: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}