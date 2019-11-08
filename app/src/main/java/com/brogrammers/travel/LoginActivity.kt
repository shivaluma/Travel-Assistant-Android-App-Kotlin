package com.brogrammers.travel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.layout_login.*
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            val emailPhone = editTextEmailPhone.text.toString()
            val password = editTextPassword.text.toString()
            if (emailPhone.isEmpty()) {
                editTextEmailPhone.error = "Yêu cầu nhập email/phone."
                editTextEmailPhone.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Yêu cầu nhập mật khẩu."
                editTextPassword.requestFocus()
                return@setOnClickListener
            }


        }

    }

    fun viewRegisterClicked(view: View) {
        startActivity(Intent(this,RegisterActivity::class.java))
    }
}
