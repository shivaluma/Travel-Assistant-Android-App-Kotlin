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
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
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

class LoginActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener {



    val BASE_URL = "http://35.197.153.192:3000"
    var cbManager : CallbackManager ?= null
    var mGoogleApiClient:GoogleApiClient ?= null
    var serverClientID = "1013546197046-9v7r3u73jmnvi0o82avm93k3aqkllmne.apps.googleusercontent.com"
    var gso : GoogleSignInOptions ?= null

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
        LoginManager.getInstance().logOut()
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
                            if (response.code() != 200) {
                                Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show()
                            }
                            else {
                                LoginManager.getInstance().logOut()
                                Toast.makeText(applicationContext, "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
                                updateTokenToStorage(response.body()!!.token)
                                startActivity(Intent(applicationContext,NavigationBottomActivity::class.java))
                                finish()
                            }
                        }
                    })
                }

                override fun onCancel() {
                    Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show()
                }

                override fun onError(error: FacebookException?) {
                    Log.d("errrr", error.toString())
                    Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show()
                }
            })
        }



        btnLoginGoogleInterface.setOnClickListener {
           if (gso == null) {
               gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                   .requestServerAuthCode(serverClientID)
                   .requestEmail()
                   .build()
           }

            if (mGoogleApiClient == null) {
                mGoogleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso!!)
                    .build()
            }

            val signInIntent : Intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
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
        if(requestCode == 69){
            var result : GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if(result.isSuccess()){
                var acct : GoogleSignInAccount = result.signInAccount!!
                var authCode : String = acct.serverAuthCode!!
            }
            else {
                Toast.makeText(applicationContext, "Fail!", Toast.LENGTH_LONG).show()
            }
        }
        cbManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

    }

    fun viewRegisterClicked(view: View) {
        startActivityForResult(Intent(this,RegisterActivity::class.java),6969)
    }

    fun updateTokenToStorage(token: String) {
        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        val editor = sharePref.edit()

        editor.putString("token", token)
        editor.apply()
    }

    override fun onConnected(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
