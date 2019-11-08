package com.brogrammers.travel

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import com.brogrammers.travel.LoginActivity




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var loginStatus = false
        if (!loginStatus) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            loginStatus = true
        } else {
            val intent = Intent(this, NavigationBottomActivity::class.java)
            startActivity(intent)
        }
    }
}
