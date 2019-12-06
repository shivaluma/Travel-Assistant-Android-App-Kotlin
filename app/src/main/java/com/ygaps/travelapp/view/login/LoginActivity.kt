package com.ygaps.travelapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ygaps.travelapp.manager.Constant
import com.ygaps.travelapp.network.model.ApiServiceFBLogin
import com.ygaps.travelapp.network.model.ApiServiceGGLogin
import com.ygaps.travelapp.network.model.ApiServiceLogin
import com.ygaps.travelapp.network.model.WebAccess
import com.ygaps.travelapp.view.forgetpassword.getOTPActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_login.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

class LoginActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {


    var cbManager: CallbackManager? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var gso: GoogleSignInOptions? = null
    var mGoogleSignInClient: GoogleSignInClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

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
            val service = WebAccess.retrofit.create(ApiServiceLogin::class.java)

            val call = service.postData(jsonObject)

            call.enqueue(object : Callback<ResponseLogin> {
                override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseLogin>,
                    response: Response<ResponseLogin>
                ) {
                    if (response.message() == "Not Found") {
                        Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Đăng nhập thành công!",
                            Toast.LENGTH_LONG
                        ).show()
                        updateTokenToStorage(response.body()!!.token)

                        finish()
                    }
                }
            })
        }

        btnFacebookInterface.setOnClickListener {
            cbManager = CallbackManager.Factory.create()
            LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
            LoginManager.getInstance()
                .registerCallback(cbManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        val jsonObject = JsonObject()
                        jsonObject.addProperty("accessToken", result!!.accessToken.token)
                        val service = WebAccess.retrofit.create(ApiServiceFBLogin::class.java)
                        val call = service.postData(jsonObject)
                        call.enqueue(object : Callback<ResponseOthersLogin> {
                            override fun onFailure(call: Call<ResponseOthersLogin>, t: Throwable) {
                                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG)
                                    .show()
                            }

                            override fun onResponse(
                                call: Call<ResponseOthersLogin>,
                                response: Response<ResponseOthersLogin>
                            ) {
                                if (response.code() != 200) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Đăng nhập thất bại!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    LoginManager.getInstance().logOut()
                                    Toast.makeText(
                                        applicationContext,
                                        "Đăng nhập thành công!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    updateTokenToStorage(response.body()!!.token)

                                    finish()
                                }
                            }
                        })
                    }

                    override fun onCancel() {
                        Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG)
                            .show()
                    }

                    override fun onError(error: FacebookException?) {
                        Log.d("errrr", error.toString())
                        Toast.makeText(applicationContext, "Đăng nhập thất bại!", Toast.LENGTH_LONG)
                            .show()
                    }
                })
        }



        btnLoginGoogleInterface.setOnClickListener {

            if (gso == null) {
                gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
                    .requestServerAuthCode(Constant.ggServerClientID)
                    .requestEmail()
                    .build()
                mGoogleSignInClient = GoogleSignIn.getClient(this, gso!!)
            }

//            if (mGoogleApiClient == null ) {
//                mGoogleApiClient = GoogleApiClient.Builder(this)
//                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso!!)
//                    .build()
//            }

            val signInIntent: Intent = mGoogleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, 69)

        }


        forgetPasswordBtn.setOnClickListener {
            var intent = Intent(applicationContext, getOTPActivity::class.java)
            startActivity(intent)
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
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d("Sign In: ", task.toString())
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account!!.serverAuthCode
                Log.d("Sign In: ", idToken.toString())
                var client: OkHttpClient = OkHttpClient()
                var requestBody: RequestBody = FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("client_id", Constant.ggServerClientID)
                    .add("client_secret", Constant.ggclientSecret)
                    .add("redirect_uri", "")
                    .add("code", idToken!!)
                    .build()
                var request: Request = Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v4/token")
                    .post(requestBody)
                    .build()
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        Log.e("errr", e.toString())
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        try {
                            var jsonObject2: JSONObject = JSONObject(response.body()!!.string())
                            var ggaccesstoken: String = jsonObject2.get("access_token").toString()
                            Log.d("ggacc", ggaccesstoken)
                            val jsonObject = JsonObject()
                            jsonObject.addProperty("accessToken", ggaccesstoken)
                            val service = WebAccess.retrofit.create(ApiServiceGGLogin::class.java)
                            val call = service.postData(jsonObject)
                            call.enqueue(object : Callback<ResponseOthersLogin> {
                                override fun onFailure(
                                    call: Call<ResponseOthersLogin>,
                                    t: Throwable
                                ) {
                                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG)
                                        .show()
                                }

                                override fun onResponse(
                                    call: Call<ResponseOthersLogin>,
                                    response: Response<ResponseOthersLogin>
                                ) {
                                    if (response.code() != 200) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Đăng nhập thất bại!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        LoginManager.getInstance().logOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Đăng nhập thành công!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        updateTokenToStorage(response.body()!!.token)

                                    }
                                }
                            })

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                })
            } catch (e: ApiException) {
                Log.d("Sign In: ", "signInResult:failed code=" + e.statusCode)
            }

        }
        cbManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

    }

    fun viewRegisterClicked(view: View) {
        startActivityForResult(Intent(this, RegisterActivity::class.java), 6969)
    }

    fun updateTokenToStorage(token: String) {
        val sharePref: SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        val editor = sharePref.edit()
        editor.putString("token", token)
        editor.apply()

        var intent = Intent(
            applicationContext,
            NavigationBottomActivity::class.java
        )
        intent.putExtra("userToken", token)
        startActivity(intent)
        finish()
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }


}
