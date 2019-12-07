package com.ygaps.travelapp.view.navigation.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.ygaps.travelapp.*
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.util.util
import com.ygaps.travelapp.view.user.UpdatePassword
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ygaps.travelapp.network.model.*

import kotlinx.android.synthetic.main.fragment_users.view.*
import kotlinx.android.synthetic.main.popup_verify_account.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationsFragment : Fragment() {




    var token : String = ""
    var userId : Int = 0

    lateinit var popupWindow : PopupWindow

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_users, container, false)
        token = activity!!.intent.extras!!.getString("userToken","notoken")




        ApiRequest(token,root)

        root.userMenu.setOnClickListener {
            val popup = PopupMenu(context, root.userMenu)
            popup.inflate(R.menu.usermenuitem)

            popup.setOnMenuItemClickListener(object:  PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(menuitem: MenuItem?): Boolean {
                    if (menuitem!!.itemId == R.id.change_password) {
                        var intent = Intent(context, UpdatePassword::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("token", token)
                        startActivity(intent)
                    }
                    return false
                }
            })

            popup.show()
        }

        root.spinnerGender.setItems("Female", "Male")
        root.editUserDOB.setOnClickListener {
            util.setOnClickDate(root.editUserDOB, context!!, "YYYY-MM-dd")
        }


        root.btnEditUserInfo.setOnClickListener {
            root.switcher.showNext()
            root.editUserFullName.setText(root.userInfoName.text)
            root.editUserEmail.setText(root.userInfoEmail.text)
            root.editUserAddress.setText(root.userInfoAddress.text)
            root.editUserPhone.setText(root.userInfoPhone.text)
            root.editUserDOB.setText(root.userInfoDOB.text)
            root.spinnerGender.selectedIndex = util.getGenders(root.userInfoGender.text.toString())
            root.btnEditUserInfo.visibility = View.GONE
            root.btnSaveUserInfo.visibility = View.VISIBLE
        }

        root.btnSaveUserInfo.setOnClickListener {

            var fullName = root.editUserFullName.text.toString()
            var email = root.editUserEmail.text.toString()
            var phone = root.editUserPhone.text.toString()
            var dob = root.editUserDOB.text.toString()
            var address = root.editUserAddress.text.toString()
            var gender = root.spinnerGender.selectedIndex
            Log.d("abab", fullName + email +phone + gender + dob)
            ApiRequestSaveInfo(token,root,fullName,email,phone,gender,dob,address)
        }



        root.btnLogoutAccount.setOnClickListener {
            val sharePref : SharedPreferences = this.activity!!.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
            val editor = sharePref.edit()
            editor.remove("token")
            editor.apply()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity!!.finish()
        }


        root.btnVerifyUser.setOnClickListener {
            popupVerifyAccount(root)
        }




        return root
    }

    fun ApiRequest(token : String, view: View) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetUserInfo::class.java)
            val call = service.getInfo(token)
            call.enqueue(object : Callback<ResponseUserInfo> {
                override fun onFailure(call: Call<ResponseUserInfo>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseUserInfo>,
                    response: Response<ResponseUserInfo>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(context, errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        var item = response!!.body()
                        userId = item!!.id
                        view.userInfoName.text = item!!.fullName.toString()
                        view.userInfoEmail.text = item!!.email.toString()
                        view.userInfoAddress.text = item!!.address.toString()
                        view.userInfoDOB.text = item!!.dob.toString()
                        view.userInfoPhone.text = item!!.phone.toString()

                        if (item.avatar != null) {
                            util.urlToImageView(item.avatar!!, view.userAvatar, 90)
                        }


                        if (item.gender != null) view.userInfoGender.text = util.getGenders(item.gender!!)

                    }
                }
            })
        }.execute()

    }


    fun ApiRequestSaveInfo(token : String,root:View, fullName : String, email : String, phone: String, gender : Int, dob : String, address : String) {
            val service = WebAccess.retrofit.create(ApiServiceUpdateUserInfo::class.java)
            val body = JsonObject()
            if (fullName.isNotEmpty()) body.addProperty("fullName", fullName)
            if (email.isNotEmpty()) body.addProperty("email", email)
            if (phone.isNotEmpty()) body.addProperty("phone", phone)
            if (gender >= 0) body.addProperty("gender", gender)
            if (address.isNotEmpty()) body.addProperty("address", address)
            if (dob.isNotEmpty()) body.addProperty("dob", dob)

            val call = service.updateInfo(token,body)
            call.enqueue(object : Callback<ResponseUpdateUserInfo> {
                override fun onFailure(call: Call<ResponseUpdateUserInfo>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseUpdateUserInfo>,
                    response: Response<ResponseUpdateUserInfo>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(context, errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Update Success!!", Toast.LENGTH_LONG).show()
                        root.switcher.showNext()
                        root.btnEditUserInfo.visibility = View.VISIBLE
                        root.btnSaveUserInfo.visibility = View.GONE
                        ApiRequest(token,root)
                    }
                }
            })


    }


    fun popupVerifyAccount(root : View) {
        val inflater: LayoutInflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_verify_account, null)



        popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
            true
        )

        view.btnHideVerifyPopup.setOnClickListener {
            popupWindow.dismiss()
        }

        view.spinnerVerifyType.setItems("Email" , "Phone")

        view.btnSubmitVerify.setOnClickListener {
            var type = view.spinnerVerifyType.text.toString()
            ApiRequestVerify(userId,type)
        }

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }


        // If API level 23 or higher then execute the code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.LEFT
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut

        }

        // Get the widgets reference from custom view
        //val tv = view.findViewById<TextView>(R.id.text_view)




        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            root.fragmentUserMainLayout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }


    fun popupVerifyCode(pw : PopupWindow) {
        val inflater: LayoutInflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_setting_bottom, null)
        pw.contentView = view
    }



    fun ApiRequestVerify(userId : Int, type: String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetVerifyCode::class.java)
            val call = service.getVerify(token,userId,type)
            call.enqueue(object : Callback<ResponseGetVerifyCode> {
                override fun onFailure(call: Call<ResponseGetVerifyCode>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseGetVerifyCode>,
                    response: Response<ResponseGetVerifyCode>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(context, errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        popupVerifyCode(popupWindow)
                    }
                }
            })
        }.execute()

    }


}