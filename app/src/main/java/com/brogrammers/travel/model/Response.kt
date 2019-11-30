package com.brogrammers.travel

import com.brogrammers.travel.model.StopPoint
import com.brogrammers.travel.model.Tour
import com.brogrammers.travel.ui.history.HistoryFragment
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query


data class ResponseLogin(val message:String, val userId:Int, val token:String)
data class ResponseOthersLogin(val message:String, val avatar:Int, val fullname:String, val token:String)
data class ResponseRegister(val error:Int, val message:ArrayList<message>)
data class ResponseListTours(var total:Int, var tours:ArrayList<Tour>, var message:String)
data class ResponseListHistoryTours(var total:Int, var tours:ArrayList<Tour>, var message:String)
data class ResponseCreateTour(
    val hostId: String,
    val name: String,
    val id: Int,
    val message: ArrayList<errorlist>
)

data class ResponseAddStopPoint(
    val message: String,
    val arr : ArrayList<AddStopPointResultOK>
)

data class AddStopPointResultOK(
    val tourId: String,
    val name: String,
    val lat: Double,
    val long: Double,
    val arrivalAt: Long,
    val leaveAt: Long,
    val minCost: Int,
    val maxCost: Int,
    val serviceTypeId: Int,
    val id: Int
)

data class ResponseTourInfo (
    var id : Int ,
    var status : Int ,
    var name : String ,
    var minCost : Long ,
    var maxCost : Long ,
    var startDate: Long ,
    var endDate: Long ,
    var adults : Int ,
    var childs : Int ,
    var isPrivate: Boolean ,
    var avatar : String,
    var stopPoints : ArrayList<StopPoint>,
    var members: ArrayList<member>,
    var comments: ArrayList<comment>
)

data class ResponseSearchUser (
    var total : Int ,
    var users: ArrayList<UserInfo>,
    var message: String
)

data class ResponseAddUserToTour (
    var message: String
)

data class ResponseToInvitation (
    var message: String
)

data class ResponseUserNotification (
    var total: Int,
    var tours: ArrayList<TourNotification>
)

// sub class
data class message (
    val location:String,
    val param:String,
    val msg:String,
    val value:String
)


data class errorlist (
    var location: String,
    var param: String,
    var msg: String
)

data class member(var id: Int, var name:String,var phone: String, var avatar: String, var isHost : Boolean)
data class comment(var id: Int, var name:String,var comment: String, var avatar: String)
data class UserInfo(var id : Int?, var fullName: String?, var email: String?,var phone: String?, var gender: Int?,
                var typeLogin : Int?, var avatar: String?, var dob: String?)

data class TourNotification(var id: Int,var status: Int,var hostId: Int,var hostName: String,
                            var hostPhone: String,var hostEmail: String,var hostAvatar: String,
                            var name: String,var minCost : Long , var maxCost : Long ,
                            var startDate: Long , var endDate: Long , var adults : Int ,
                            var childs : Int , var isPrivate: Boolean , var avatar : String,
                            var createOn: Long)




