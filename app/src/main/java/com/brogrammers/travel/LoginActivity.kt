package com.brogrammers.travel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import bolts.Task
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.*

class LoginActivity : AppCompatActivity() {

    val BASE_URL = "http://35.197.153.192:3000"
    var cbManager : CallbackManager ?= null
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
           val editor = sharePref.edit()


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


            val jsonObject = JsonObject()
            jsonObject.addProperty("emailPhone", emailPhone)
            jsonObject.addProperty("password", password)



            val service = retrofit.create(ApiServiceLogin::class.java)

            val call = service.postData(jsonObject)

            call.enqueue(object : Callback<PostResponseLogin> {
                override fun onFailure(call: Call<PostResponseLogin>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<PostResponseLogin>,
                    response: Response<PostResponseLogin>
                ) {
                    if (response.message() == "Not Found") {
                        Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(applicationContext, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
                        updateTokenToStorage(response.body()!!.token)
                        startActivity(Intent(applicationContext,NavigationBottomActivity::class.java))
                        finish()
                    }
                }
            })
        }

        btnFacebookInterface.setOnClickListener {
            btnLoginFacebook.performClick()
        }

        btnLoginFacebook.setOnClickListener {

            cbManager = CallbackManager.Factory.create()
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
            LoginManager.getInstance().registerCallback(cbManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.d("Fbtoken", result!!.accessToken.token)
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("accessToken", result.accessToken.token)
                    val service = retrofit.create(ApiServiceFBLogin::class.java)
                    val call = service.postData(jsonObject)
                    call.enqueue(object : Callback<PostResponseFBLogin> {
                        override fun onFailure(call: Call<PostResponseFBLogin>, t: Throwable) {
                            Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(
                            call: Call<PostResponseFBLogin>,
                            response: Response<PostResponseFBLogin>
                        ) {
                            Log.d("fbmesg", response.message())
                            if (response.code() != 200) {
                                Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show()
                            }
                            else {
                                Toast.makeText(applicationContext, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
                                updateTokenToStorage(response.body()!!.token)
                                startActivity(Intent(applicationContext,NavigationBottomActivity::class.java))
                                finish()
                            }
                        }
                    })
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }
            })
        }

        btnLoginGoogleInterface.setOnClickListener {
            firebaseAuth = FirebaseAuth.getInstance()
            configureGoogleSignIn()
            mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 69)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 6969) {
            if (resultCode == Activity.RESULT_OK) {
                editTextEmailPhone.setText(data?.getStringExtra("usrname"))
                editTextPassword.setText(data?.getStringExtra("passwrd"))
            }
        }
        if (requestCode == 69) {
            val task: com.google.android.gms.tasks.Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        cbManager?.onActivityResult(requestCode,resultCode,data)
    }

    fun viewRegisterClicked(view: View) {
        startActivityForResult(Intent(this,RegisterActivity::class.java),6969)
    }

    fun updateTokenToStorage(token: String) {
        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        val editor = sharePref.edit()
        Log.d("Token", token)
        editor.putString("token", token)
    }

    fun checkTokenAlive(token: String) {
        
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val jsonObject = JsonObject()
                jsonObject.addProperty("accessToken", acct.idToken.toString())
                val service = retrofit.create(ApiServiceGGLogin::class.java)
                val call = service.postData(jsonObject)

                call.enqueue(object : Callback<PostResponseGGLogin> {

                    override fun onFailure(call: Call<PostResponseGGLogin>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<PostResponseGGLogin>,
                        response: Response<PostResponseGGLogin>
                    ) {
                        if (response.message() == "Not Found") {
                            Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(applicationContext, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
                            updateTokenToStorage(response.body()!!.token)
                            startActivity(Intent(applicationContext,NavigationBottomActivity::class.java))
                            finish()
                        }
                    }
                })
            } else {
                Toast.makeText(this, "Google sign in failed :(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    private interface ApiServiceLogin {
        @POST("/user/login")
        fun postData(
            @Body body: JsonObject
        ): Call<PostResponseLogin>
    }

    private interface ApiServiceFBLogin {
        @POST("/user/login/by-facebook")
        fun postData(
            @Body body: JsonObject
        ): Call<PostResponseFBLogin>
    }

    private interface ApiServiceGGLogin {
        @POST("/user/login/by-google")
        fun postData(
            @Body body: JsonObject
        ): Call<PostResponseGGLogin>
    }



    data class PostResponseLogin(val message:String, val userId:Int, val token:String)
    data class PostResponseFBLogin(val message:String, val avatar:Int, val fullname:String, val token:String)
    data class PostResponseGGLogin(val message:String, val avatar:Int, val fullname:String, val token:String)
}
