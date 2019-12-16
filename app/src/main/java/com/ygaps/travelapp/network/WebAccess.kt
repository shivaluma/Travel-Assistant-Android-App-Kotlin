package com.ygaps.travelapp.network.model


import android.util.Log
import com.ygaps.travelapp.manager.Constant
import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton retrofit
object WebAccess {
    val retrofit: Retrofit by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // Create Retrofit client
        return@lazy retrofit
    }
}