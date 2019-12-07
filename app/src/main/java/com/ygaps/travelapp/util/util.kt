package com.ygaps.travelapp.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.ygaps.travelapp.R
import com.ygaps.travelapp.ResponseTourInfo
import com.ygaps.travelapp.ResponseUserInfo
import com.ygaps.travelapp.manager.Constant
import com.ygaps.travelapp.network.model.ApiServiceGetTourInfo
import com.ygaps.travelapp.network.model.ApiServiceGetUserInfo
import com.ygaps.travelapp.network.model.WebAccess
import kotlinx.android.synthetic.main.activity_tour_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object util {
    fun dateToLong(dateString : String, pattern: String = "dd/MM/yyyy"): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val date = sdf.parse(dateString)
        return date.time
    }

    fun longToDateTime(time: Long): String {
        var zero: Long = 0
        if (time == zero) return ""
        var instant: Instant = Instant.ofEpochMilli(time)
        var date: LocalDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
        return date.format(formatter)
    }

    fun longToDate(time: Long): String {
        val date = Date(time)
        val format = java.text.SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }

    fun datetimeToLong(datetime: String): Long {
        return LocalDateTime.parse(
            datetime,
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
        ).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()
    }

    fun getProvinceID(province: String): Int {
        return Constant.provinceList.indexOf(province)
    }

    fun setOnClickTime(time: EditText, context: Context) {
        var mcurrentTime = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            mcurrentTime.set(Calendar.HOUR_OF_DAY, hour)
            mcurrentTime.set(Calendar.MINUTE, minute)
            time.setText(SimpleDateFormat("HH:mm").format(mcurrentTime.time))
        }
        TimePickerDialog(
            context,
            timeSetListener,
            mcurrentTime.get(Calendar.HOUR_OF_DAY),
            mcurrentTime.get(Calendar.MINUTE),
            true
        ).show()
    }


    fun setOnClickDate(date: EditText, context: Context, pattern : String = "dd/MM/yyyy") {
        var mcurrentTime = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            mcurrentTime.set(Calendar.DAY_OF_MONTH, day)
            mcurrentTime.set(Calendar.MONTH, month)
            mcurrentTime.set(Calendar.YEAR, year)
            date.setText(SimpleDateFormat(pattern).format(mcurrentTime.time))
        }
        DatePickerDialog(
            context,
            dateSetListener,
            mcurrentTime.get(Calendar.YEAR),
            mcurrentTime.get(Calendar.MONTH),
            mcurrentTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun StopPointTypeToString(type : Int): String {
        if (type == 1) return "Restaurant"
        if (type == 2) return "Hotel"
        if (type == 3) return "Rest Station"
        return "Others"
    }

    fun getGenders(type: Int) : String {
        if (type == 1) return "Male"
        return "Female"
    }

    fun getGenders(type: String) : Int {
        if (type == "Male") return 1
        return 0
    }

    fun getAssetByStatus(status: Int) : Int {
        if (status == -1) {
            return  R.drawable.ic_cancel_big
        }
        else if (status == 0) {
            return  R.drawable.ic_open_sign_big
        }
        else if (status == 1) {
            return  R.drawable.ic_start_big
        }
        else {
            return  R.drawable.ic_closed_big
        }
    }

    fun urlToImageView(urlConnection : String, view : ImageView, size : Int) {

        Picasso.get()
                .load(urlConnection)
                .resize(size, size)
                .centerCrop()
                .into(view)

    }





}