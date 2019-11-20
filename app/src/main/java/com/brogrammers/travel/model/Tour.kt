package com.brogrammers.travel.model

data class Tour (
    var id : Int ,
    var status : Int ,
    var name : String ,
    var minCost : Int ,
    var maxCost : Int ,
    var startDate: Long ,
    var endDate: Long ,
    var adults : Int ,
    var childs : Int ,
    var isPrivate: Boolean ,
    var avatar : String
)