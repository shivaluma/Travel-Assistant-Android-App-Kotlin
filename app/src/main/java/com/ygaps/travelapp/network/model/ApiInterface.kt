package com.ygaps.travelapp.network.model

import android.database.Observable
import com.ygaps.travelapp.*
import com.ygaps.travelapp.model.StopPoint
import com.ygaps.travelapp.ui.home.HomeFragment
import com.google.gson.JsonObject
import okhttp3.MultipartBody
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
        @Query("orderBy") orderBy: String?,
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

interface ApiServiceSearchHistoryTours {
    @GET("/tour/search-history-user")
    fun searchTours(
        @Header("Authorization") token : String,
        @Query("searchKey") searchKey: String,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: String
    ): Call<ResponseListHistoryTours>
}

interface ApiServiceGetHistoryToursByStatus {
    @GET("/tour/history-user-by-status")
    fun getToursByStatus(
        @Header("Authorization") token : String
    ): Call<ResponseListHistoryToursByStatus>
}

interface ApiServiceGetTourInfo {
    @GET("/tour/info")
    fun getTourInfo(
        @Header("Authorization") token : String,
        @Query("tourId") tourId: Int
    ): Call<ResponseTourInfo>
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

interface ApiServiceGetUserList {
    @GET("/user/search")
    fun getUsers(
        @Query("searchKey") searchKey : String,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: String
    ): Call<ResponseSearchUser>
}

interface ApiServiceAddUserToTour {
    @POST("/tour/add/member")
    fun addUser(
        @Header("Authorization") Authorization: String,
        @Body body: JsonObject
    ): Call<ResponseAddUserToTour>
}


interface ApiServiceGetNotifications {
    @GET("/tour/get/invitation")
    fun getNotif(
        @Header("Authorization") token : String,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: String
    ): Call<ResponseUserNotification>
}


interface ApiServiceResponseInvitaion {
    @POST("/tour/response/invitation")
    fun response(
        @Header("Authorization") Authorization: String,
        @Body body: JsonObject
    ): Call<ResponseToInvitation>
}

interface ApiServiceTourComment {
    @POST("/tour/comment")
    fun comment(
        @Header("Authorization") Authorization: String,
        @Body body: JsonObject
    ): Call<ResponseToComment>
}

interface ApiServiceGetUserInfo {
    @GET("/user/info")
    fun getInfo(
        @Header("Authorization") token : String
    ): Call<ResponseUserInfo>
}

interface ApiServiceGetCommentList {
    @GET("/tour/comment-list")
    fun getList(
        @Header("Authorization") token : String,
        @Query("tourId") tourId: String,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: String
    ): Call<ResponseCommentList>
}

interface ApiServiceGetVerifyCode {
    @GET("/user/send-active")
    fun getVerify(
        @Header("Authorization") token : String,
        @Query("userId") userId : Int,
        @Query("type") type : String
    ): Call<ResponseGetVerifyCode>
}

interface ApiServiceUpdateUserInfo {
    @POST("/user/edit-info")
    fun updateInfo(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseUpdateUserInfo>
}

interface ApiServiceGetTourReview {
    @GET("/tour/get/review-list")
    fun getReview(
        @Header("Authorization") token : String,
        @Query("tourId") tourId : Int,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize : String
    ): Call<ResponseGetReviewsTour>
}

interface ApiServiceAddReview {
    @POST("/tour/add/review")
    fun addReview(
        @Header("Authorization") Authorization: String,
        @Body body: JsonObject
    ): Call<ResponseToAddReview>
}

interface ApiServiceGetStopPointInfo {
    @GET("/tour/get/service-detail")
    fun getTourInfo(
        @Header("Authorization") token : String,
        @Query("serviceId") serviceId: Int
    ): Call<ResponseStopPointInfo>
}

interface ApiServiceGetStopPointPoints {
    @GET("/tour/get/feedback-point-stats")
    fun getPoints(
        @Header("Authorization") token : String,
        @Query("serviceId") serviceId: Int
    ): Call<ResponseStopPointRatingPoints>
}

interface ApiServiceGetTourNotices {
    @GET("/tour/notification-list")
    fun getNotices(
        @Header("Authorization") token : String,
        @Query("tourId") tourId: Int,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: String
    ): Call<ResponseGetTourNotice>
}




interface ApiServiceSearchDestination {
    @GET("/tour/search/service")
    fun search(
        @Header("Authorization") token : String,
        @Query("searchKey") searchKey: String,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: String
    ): Call<ResponseSearchDestination>
}


interface ApiServiceGetServiceFeedBack {
    @GET("/tour/get/feedback-service")
    fun getFeedback(
        @Header("Authorization") token : String,
        @Query("serviceId") searchKey: Int,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: String
    ): Call<ResponseListFeedBackService>
}

interface ApiServiceSuggestDestination {
    @POST("/tour/suggested-destination-list")
    fun getSuggest(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseSuggestDestination>
}

interface ApiServiceGetOTP {
    @POST("/user/request-otp-recovery")
    fun getOTP(
        @Body body: JsonObject
    ): Call<ResponseGetOTP>
}

interface ApiServiceCheckOTP {
    @POST("/user/verify-otp-recovery")
    fun checkOTP(
        @Body body: JsonObject
    ): Call<ResponseCheckOTP>
}


interface ApiServiceChangePassword {
    @POST("/user/update-password")
    fun changepw(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseChangePassword>
}

interface ApiServiceUpdateTour {
    @POST("/tour/update-tour")
    fun update(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseUpdateTourInfo>
}

interface ApiServicePutFcmToken {
    @POST("/user/notification/put-token")
    fun putToken(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponsePutFcmToken>
}


interface ApiServiceRemoveFcmToken {
    @POST("/user/notification/remove-token")
    fun removeToken(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseRemoveFcmToken>
}


interface ApiServiceSendServiceFeedback {
    @POST("/tour/add/feedback-service")
    fun sendFeedback(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseFeedbackService>
}

interface ApiServiceSendTourNotice {
    @POST("/tour/notification")
    fun sendNotice(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseSendNotice>
}

interface ApiServiceCreateNotificationOnRoad {
    @POST("/tour/add/notification-on-road")
    fun addNoti(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseAddNotificationOnRoad>
}

interface ApiServiceGetNotificationOnRoad {
    @GET("/tour/get/noti-on-road")
    fun getdNoti(
        @Header("Authorization") token : String,
        @Query("tourId") tourId : Int,
        @Query("pageIndex") pageIndex : Int,
        @Query("pageSize") pageSize : String
    ): Call<ResponseGetNotificationOnRoad>
}

interface ApiServiceUpdateStopPoint {
    @POST("/tour/update-stop-point")
    fun updateStopPoint(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseUpdateStopPoint>
}


interface ApiServiceSendCoordinate {
    @POST("/tour/current-users-coordinate")
    fun sendCoordinate(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseSendCoordinate>
}



interface ApiServiceUploadRecord {
    @Multipart
    @POST("/tour/recording")
    fun upRecord(
        @Header("Authorization") token : String,
        @Part file : MultipartBody.Part
    ): Call<ResponseUploadRecord>
}

interface ApiServiceUpdateLandingTime {
    @POST("/tour/update/landing-times-for-destination")
    fun landing(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseUpdateLandingTime>
}

interface ApiServiceCloneTour {
    @POST("/tour/clone")
    fun clone(
        @Header("Authorization") token : String,
        @Body body: JsonObject
    ): Call<ResponseTourInfo>
}



