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
import androidx.core.app.RemoteInput
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ygaps.travelapp.*
import com.ygaps.travelapp.network.model.ApiServiceResponseInvitaion
import com.ygaps.travelapp.util.util
import org.jetbrains.anko.accessibilityManager


class FirebaseMessagingService : FirebaseMessagingService() {


    internal var userToken : String ?= null


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("notifas", "NOTIFICATION")

        for (i in remoteMessage.data) {
            Log.d("notifas", i.key + " -> " + i.value )
        }
        sendNotification(remoteMessage.data)
        super.onMessageReceived(remoteMessage)

    }

    private fun sendNotification(map: Map<String, String>) {
        val channelId = getString(R.string.project_id)
        val channel = NotificationChannel(
            channelId,
            "Travel Assistant",
            NotificationManager.IMPORTANCE_HIGH
        )
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (map["type"]!!.toInt() == 5) {
            val intent = Intent(this, TourInfoActivity::class.java)

            if (userToken == null) {
                val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
                userToken = sharePref.getString("token", "notoken")!!
            }

            intent.putExtra("token", userToken)
            intent.putExtra("tourID", map["tourId"]!!.toInt())
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)


            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_launcher_background
                    )
                )
                .setContentTitle(map.get("userId") + " comment to " + map.get("tourId"))
                .setContentText(map["comment"])
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)



            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(0, notificationBuilder.build())

        }
        else if (map["type"]!!.toInt() == 6) {
            val acceptIntent = Intent(this, NotificationActionService::class.java).setAction("Tour_Invitation_Accept")
            val declineIntent = Intent(this, NotificationActionService::class.java).setAction("Tour_Invitation_Decline")

            val pendingIntentAccept = PendingIntent.getService(this, 0, acceptIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            val pendingIntentDecline = PendingIntent.getService(this, 0, declineIntent, PendingIntent.FLAG_CANCEL_CURRENT)


            val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
            val editor = sharePref.edit()
            editor.putString("tourId", map["id"])
            editor.apply()


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



            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(0, notificationBuilder.build())
        }
        else if (map["type"]!!.toInt() == 4) {
            val intent = Intent(this, NotificationActionService::class.java).setAction("Tour_Follow_Reply")
            intent.putExtra("tourId", map["tourId"])
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


            val resultPendingIntent: PendingIntent? = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            val replyLabel = "Reply"
            val remoteInput = RemoteInput.Builder("chat_reply")
                .setLabel(replyLabel)
                .build()

            //notify new message
            notifyNewMessage(map["tourId"]!!)

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_launcher_background
                    )
                )
                .setContentTitle(map.get("userId") + " send a notification to " + map.get("tourId"))
                .setContentText(map["notification"])
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_star_gold_24dp,replyLabel ,resultPendingIntent)
                        .addRemoteInput(remoteInput)
                        .setAllowGeneratedReplies(true)
                        .build()
                    )




            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                notificationManager.createNotificationChannel(channel)
            }


            notificationManager.notify(0, notificationBuilder.build())
        }

        else if (map["type"]!!.toInt() == 2) {
            val intent = Intent(this, TourInfoActivity::class.java)
            intent.putExtra("tourID", map["tourId"])
            intent.putExtra("token", userToken)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


            val resultPendingIntent: PendingIntent? = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)


            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_launcher_background
                    )
                )
                .setContentTitle(map.get("userId") + " send a notification to " + map.get("tourId"))
                .setContentText("Note : " + map["note"] + "\n" +"Location :" + map["lat"].toString() +", " + map["long"].toString() )
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)




            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                notificationManager.createNotificationChannel(channel)
            }


            notificationManager.notify(0, notificationBuilder.build())
        }


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
//        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
////        val editor = sharePref.edit()
////        Log.d("onnewtoken", p0 + " - " + userToken)
////        editor.putString("fcmToken", p0)
////        editor.putBoolean("fcmTokenPushed", false)
////        editor.apply()
        super.onNewToken(p0)
    }

    private fun notifyNewMessage(tourId : String) {
        Log.d("sender", "Broadcasting message")
        val intent = Intent("notify-new-message")
        // You can also include some extra data.
        intent.putExtra("tourId", tourId)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }




    fun ApiRequestPutFcmToken( FcmToken : String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServicePutFcmToken::class.java)

            val jsonObject = JsonObject()
            var uniqueId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)

            jsonObject.addProperty("fcmToken", FcmToken)
            jsonObject.addProperty("deviceId", uniqueId)
            jsonObject.addProperty("platform", 1)
            jsonObject.addProperty("appVersion", "1.0")

            if (userToken == null) {
                val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
                userToken = sharePref.getString("token", "notoken")!!
            }

            val call = service.putToken(userToken!!,jsonObject)
            call.enqueue(object : Callback<ResponsePutFcmToken> {
                override fun onFailure(call: Call<ResponsePutFcmToken>, t: Throwable) {
                    //Toast.makeText(applicationContext,"Fcmtoken : " + t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponsePutFcmToken>,
                    response: Response<ResponsePutFcmToken>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        //Toast.makeText(applicationContext, "Fcmtoken : " + errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        //Toast.makeText(applicationContext, "Put Token OK!!", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }.execute()
    }



}