package com.messange.app.messengerapp.presenter.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.messange.app.messengerapp.R
import com.messange.app.messengerapp.data.SharedPrefManager
import com.messange.app.messengerapp.databinding.ActivitySecondAuthBinding

class SecondAuthActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySecondAuthBinding
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySecondAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.next.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val surname = binding.surname.text.toString().trim()
            val city = binding.sity.text.toString().trim()
            val status = binding.citate.text.toString().trim()

            if (name.isBlank() || surname.isBlank() || city.isBlank() || status.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = SharedPrefManager.getUser() ?: return@setOnClickListener

            val updatedData = mapOf(
                "name" to "$name $surname",
                "city" to city,
                "status" to status
            )

            firestore.collection("users")
                .document(user.id)
                .update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainScreenActivity::class.java))
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка обновления: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("Firestore", "Ошибка обновления профиля", e)
                }
        }

    }
}