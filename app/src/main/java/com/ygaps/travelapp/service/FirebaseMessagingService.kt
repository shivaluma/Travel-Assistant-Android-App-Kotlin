package com.ygaps.travelapp.service

import android.app.*
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.media.RingtoneManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService
import java.lang.Exception
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.provider.Settings
import android.widget.Toast

import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.network.model.ApiServicePutFcmToken
import com.ygaps.travelapp.network.model.ApiServiceTourComment
import com.ygaps.travelapp.network.model.WebAccess
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.BroadcastReceiver
import android.os.Build

import android.graphics.BitmapFactory
import com.ygaps.travelapp.*
import com.ygaps.travelapp.network.model.ApiServiceResponseInvitaion
import org.jetbrains.anko.accessibilityManager


class FirebaseMessagingService : FirebaseMessagingService() {

    var userToken : String = ""


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        sendNotification(remoteMessage.data)
    }

    private fun sendNotification(map: Map<String, String>) {
        val acceptIntent = Intent(this, NotificationActionService::class.java).setAction("Tour_Invitation_Accept")
        val declineIntent = Intent(this, NotificationActionService::class.java).setAction("Tour_Invitation_Decline")

        val pendingIntentAccept = PendingIntent.getService(this, 0, acceptIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val pendingIntentDecline = PendingIntent.getService(this, 0, declineIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        val editor = sharePref.edit()
        editor.putString("tourId", map["id"])
        editor.apply()


        val channelId = getString(R.string.project_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)




        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_launcher_background
                )
            )
            .setContentTitle("Tour Invitation")
            .setContentText(map.get("hostName") + " invites you to tour " + map.get("name"))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.btn_default,
                    "Decline",
                    pendingIntentDecline
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.btn_default,
                    "Accept",
                    pendingIntentAccept
                )
            )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private val TAG = "MyFirebaseMsgService"
    }

    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onSendError(p0: String, p1: Exception) {
        super.onSendError(p0, p1)
    }

    override fun onNewToken(p0: String) {
        ApiRequestPutFcmToken(p0)
        super.onNewToken(p0)
    }




    fun ApiRequestPutFcmToken( FcmToken : String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServicePutFcmToken::class.java)
            val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
            val token = sharePref.getString("token", "notoken")!!
            userToken = token
            val jsonObject = JsonObject()
            var uniqueId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)

            jsonObject.addProperty("fcmToken", FcmToken)
            jsonObject.addProperty("deviceId", uniqueId)
            jsonObject.addProperty("platform", 1)
            jsonObject.addProperty("appVersion", "1.0")
            val call = service.putToken(token,jsonObject)
            call.enqueue(object : Callback<ResponsePutFcmToken> {
                override fun onFailure(call: Call<ResponsePutFcmToken>, t: Throwable) {
                    Toast.makeText(applicationContext,"Fcmtoken : " + t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponsePutFcmToken>,
                    response: Response<ResponsePutFcmToken>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(applicationContext, "Fcmtoken : " + errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "Put Token OK!!", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }.execute()
    }



}