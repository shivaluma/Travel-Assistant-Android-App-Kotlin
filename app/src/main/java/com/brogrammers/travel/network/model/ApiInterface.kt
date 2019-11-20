package com.brogrammers.travel.network.model

import com.brogrammers.travel.*
import com.brogrammers.travel.ui.home.HomeFragment
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface ApiServiceLogin {
    @POST("/user/login")
    fun postData(
        @Body body: JsonObject
    ): Call<ResponseLogin>
}

interface ApiServiceFBLogin {
    @POST("/user/login/by-facebook")
    fun postData(
        @Body body: JsonObject
    ): Call<ResponseOthersLogin>
}

interface ApiServiceGGLogin {
    @POST("/user/login/by-google")
    fun postData(
        @Body body: JsonObject
    ): Call<ResponseOthersLogin>
}

interface ApiServiceRegister {
    @POST("/user/register")
    fun postData(
        @Body body: JsonObject
    ): Call<ResponseRegister>
}


interface ApiServiceGetTours {
    @GET("/tour/list")
    fun getTours(
        @Header("Authorization") token : String,
        @Query("rowPerPage") rowPerPage: Int,
        @Query("pageNum") pageNum: Int,
        @Query("orderBy") orderBy: String,
        @Query("isDesc") isDesc: Boolean
    ): Call<ResponseListTours>
}

interface ApiServiceGetHistoryTours {
    @GET("/tour/history-user")
    fun getTours(
        @Header("Authorization") token : String,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: String
    ): Call<ResponseListHistoryTours>
}

interface ApiServiceCreateTour {
    @POST("/tour/create")
    fun postData(
        @Header("Authorization") Authorization: String,
        @Body body: JsonObject
    ): Call<ResponseCreateTour>
}

interface ApiServiceAddStopPointToTour {
    @POST("/tour/set-stop-points")
    fun postData(
        @Header("Authorization") Authorization: String,
        @Body body: JsonObject
    ): Call<ResponseAddStopPoint>
}




