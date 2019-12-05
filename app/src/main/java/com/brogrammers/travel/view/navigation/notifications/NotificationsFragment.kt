package com.brogrammers.travel.view.navigation.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.brogrammers.travel.*
import com.brogrammers.travel.manager.doAsync
import com.brogrammers.travel.network.model.ApiServiceGetTourInfo
import com.brogrammers.travel.network.model.ApiServiceGetUserInfo
import com.brogrammers.travel.network.model.ApiServiceUpdateUserInfo
import com.brogrammers.travel.network.model.WebAccess
import com.brogrammers.travel.util.util
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_edit_infomation.*
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.fragment_users.userInfoName
import kotlinx.android.synthetic.main.fragment_users.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationsFragment : Fragment() {




    var token : String = ""


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
            var gender = root.spinnerGender.selectedIndex
            Log.d("abab", fullName + email +phone + gender + dob)
            ApiRequestSaveInfo(token,root,fullName,email,phone,gender,dob)
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
                        view.userInfoName.text = item!!.full_name
                        view.userInfoEmail.text = item!!.email
                        view.userInfoAddress.text = item!!.address
                        view.userInfoDOB.text = item!!.dob
                        view.userInfoPhone.text = item!!.phone
                        if (item.gender != null) view.userInfoGender.text = util.getGenders(item.gender!!)

                    }
                }
            })
        }.execute()

    }


    fun ApiRequestSaveInfo(token : String,root:View, fullName : String, email : String, phone: String, gender : Int, dob : String) {
            val service = WebAccess.retrofit.create(ApiServiceUpdateUserInfo::class.java)
            val body = JsonObject()
            if (fullName.isNotEmpty()) body.addProperty("fullName", fullName)
            if (email.isNotEmpty()) body.addProperty("email", email)
            if (phone.isNotEmpty()) body.addProperty("phone", phone)
            if (gender >= 0) body.addProperty("gender", gender)
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



}