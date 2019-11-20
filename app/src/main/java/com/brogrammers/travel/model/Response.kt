package com.brogrammers.travel

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

