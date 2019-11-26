package com.brogrammers.travel.model

data class Tour (
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
    var avatar : String
)