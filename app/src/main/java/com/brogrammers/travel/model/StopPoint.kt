package com.brogrammers.travel.model

data class StopPoint(
    var id : Int,
    var name: String ,
    var type: String ,
    var address: String ,
    var lat: Double? ,
    var long: Double? ,
    var minCost: Long? ,
    var maxCost: Long? ,
    var arrivalAt: Long? ,
    var leaveAt: Long? ,
    var serviceTypeId: Int?,
    var avatar: String?,
    var contact: String?
)