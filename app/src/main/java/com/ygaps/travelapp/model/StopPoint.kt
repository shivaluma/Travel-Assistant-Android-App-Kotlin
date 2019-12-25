package com.ygaps.travelapp.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize



data class StopPoint(
    var id : Int,
    var serviceId : Int,
    var name: String ,
    var type: String ,
    var address: String ,
    var lat: Double? ,
    var long: Double? ,
    var minCost: Long? ,
    var maxCost: Long? ,
    var arrivalAt: Long? ,
    var leaveAt: Long? ,
    var provinceId : Int?,
    var serviceTypeId: Int?,
    var avatar: String?,
    var contact: String?,
    var index : Int
)

