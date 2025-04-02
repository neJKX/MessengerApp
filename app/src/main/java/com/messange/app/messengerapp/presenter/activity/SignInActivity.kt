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
import com.messange.app.messengerapp.R
import com.messange.app.messengerapp.data.DataRepositoryImpl
import com.messange.app.messengerapp.databinding.ActivitySignInBinding
import com.messange.app.messengerapp.domain.usecase.authUseCases.ConfirmPhoneSignInUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LoginWithEmailUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.LoginWithPhoneUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.RegisterWithEmailUseCase
import com.messange.app.messengerapp.domain.usecase.authUseCases.RegisterWithPhoneUseCase
import com.messange.app.messengerapp.presenter.viewModels.AuthViewModel

class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignInBinding
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
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        navigation()

        binding.next.setOnClickListener {
            val phone = binding.phoneInput.text.toString()

            if (phone.isBlank()) {
                Toast.makeText(this, "Введите номер телефона", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.registerWithPhone(phone, this).observe(this) { result ->
                result.onSuccess {
                    val intent = Intent(this, CodeActivity::class.java)
                    intent.putExtra("contextCode", "phone")
                    startActivity(intent)
                }.onFailure {
                    Toast.makeText(this, "Ошибка: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }


        binding.nextEmail.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            authViewModel.loginWithEmail(email, password).observe(this){ result ->
                result.onSuccess {
                    val intent = Intent(this, MainScreenActivity::class.java)
                    intent.putExtra("contextCode", "phone")
                    startActivity(intent)
                }.onFailure {
                    Toast.makeText(this, "Ошибка: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun navigation(){
        binding.phoneInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isNotEmpty = !s.isNullOrBlank()
                binding.next.isEnabled = isNotEmpty
                binding.next.setBackgroundResource(if (isNotEmpty) R.drawable.green_drawble else R.drawable.gray_drawble)
                binding.next.setTextColor(ContextCompat.getColor(this@SignInActivity, if (isNotEmpty) R.color.white else R.color.gray))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isNotEmpty = !s.isNullOrBlank()
                binding.nextEmail.isEnabled = isNotEmpty
                binding.nextEmail.setBackgroundResource(if (isNotEmpty) R.drawable.green_drawble else R.drawable.gray_drawble)
                binding.nextEmail.setTextColor(ContextCompat.getColor(this@SignInActivity, if (isNotEmpty) R.color.white else R.color.gray))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.goRegistration.setOnClickListener {
            startActivity(Intent(this, AuthorizationActivity::class.java))
        }

        binding.goRegistrationEmail.setOnClickListener {
            startActivity(Intent(this, AuthorizationActivity::class.java))
        }

        binding.goEmail.setOnClickListener {
            binding.emailLayout.visibility = View.VISIBLE
            binding.phoneLayout.visibility = View.GONE
        }

        binding.goPhone.setOnClickListener {
            binding.emailLayout.visibility = View.GONE
            binding.phoneLayout.visibility = View.VISIBLE
        }
    }
}