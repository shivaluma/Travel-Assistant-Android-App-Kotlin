package com.ygaps.travelapp.service

import android.app.Notification
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.NotificationManager
import com.ygaps.travelapp.R
import android.media.RingtoneManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ygaps.travelapp.MainActivity
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
import com.ygaps.travelapp.ErrorResponse
import com.ygaps.travelapp.ResponsePutFcmToken
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.network.model.ApiServicePutFcmToken
import com.ygaps.travelapp.network.model.ApiServiceTourComment
import com.ygaps.travelapp.network.model.WebAccess
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.NotificationChannel
import android.os.Build

import android.graphics.BitmapFactory





class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("abab", "From + " + remoteMessage.from!!)
        Log.d("abab", "Type + " + remoteMessage.messageType.toString())
        Log.d("abab", "Notification + " + remoteMessage.notification.toString())
        for (i in remoteMessage.data) {
            Log.d("abab", "DATA "  + i.key + " -> " + i.value)
        }
        sendNotification(remoteMessage.data!!)
    }

    private fun sendNotification(map: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

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
            .setAutoCancel(false)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.sym_call_missed,
                    "Decline",
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.sym_call_outgoing,
                    "Accept",
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
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


    fun ApiRequestPutFcmToken(FcmToken : String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServicePutFcmToken::class.java)
            val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
            val token = sharePref.getString("token", "notoken")!!
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