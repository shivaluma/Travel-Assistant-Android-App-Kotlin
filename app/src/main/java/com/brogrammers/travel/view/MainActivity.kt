package com.brogrammers.travel

import android.content.Context
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.content.SharedPreferences

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        val apptoken = sharePref.getString("token", "notoken")
        if (apptoken != "notoken") {
            startActivity(Intent(applicationContext,NavigationBottomActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
            return
        }
        else {
            startActivity(Intent(applicationContext,LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
            return
        }
    }
}
