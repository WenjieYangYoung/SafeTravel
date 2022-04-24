package com.example.openDataCoursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.openDataCoursework.ui.login.LoginActivity

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun Login(view: View) {
        // Do something in response to button
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}