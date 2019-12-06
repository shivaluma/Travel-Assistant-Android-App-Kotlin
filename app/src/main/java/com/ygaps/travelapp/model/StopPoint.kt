package com.ygaps.travelapp.model

import android.os.Parcel
import android.os.Parcelable

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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeString(address)
        parcel.writeValue(lat)
        parcel.writeValue(long)
        parcel.writeValue(minCost)
        parcel.writeValue(maxCost)
        parcel.writeValue(arrivalAt)
        parcel.writeValue(leaveAt)
        parcel.writeValue(serviceTypeId)
        parcel.writeString(avatar)
        parcel.writeString(contact)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StopPoint> {
        override fun createFromParcel(parcel: Parcel): StopPoint {
            return StopPoint(parcel)
        }

        override fun newArray(size: Int): Array<StopPoint?> {
            return arrayOfNulls(size)
        }
    }
}