package com.messange.app.messengerapp.presenter.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.messange.app.messengerapp.R
import com.messange.app.messengerapp.data.SharedPrefManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        SharedPrefManager.init(this)

        if (auth.currentUser != null){
            startActivity(Intent(this, MainScreenActivity::class.java))
        }else{
            startActivity(Intent(this, AuthorizationActivity::class.java))
        }
    }
}