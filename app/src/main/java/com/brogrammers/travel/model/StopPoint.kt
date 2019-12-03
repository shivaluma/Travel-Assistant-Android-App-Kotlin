package com.brogrammers.travel.model

data class StopPoint(
    var id : Int,
    var name: String ,
    var type: String ,
    var address: String ,
    var lat: Double? ,
    var long: Double? ,
    var minCost: Int? ,
    var maxCost: Int? ,
    var arrivalAt: Long? ,
    var leaveAt: Long? ,
    var serviceTypeId: Int?,
    var avatar: String?
)