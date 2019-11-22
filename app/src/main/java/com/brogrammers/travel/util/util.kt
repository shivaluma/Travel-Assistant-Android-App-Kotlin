package com.brogrammers.travel.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.widget.EditText
import com.brogrammers.travel.manager.Constant
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object util {
    fun dateToLong(dateString : String): Long {
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
        val format = java.text.SimpleDateFormat("dd.MM.yyyy")
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


    fun setOnClickDate(date: EditText, context: Context) {
        var mcurrentTime = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            mcurrentTime.set(Calendar.DAY_OF_MONTH, day)
            mcurrentTime.set(Calendar.MONTH, month)
            mcurrentTime.set(Calendar.YEAR, year)
            date.setText(SimpleDateFormat("dd/MM/yyyy").format(mcurrentTime.time))
        }
        DatePickerDialog(
            context,
            dateSetListener,
            mcurrentTime.get(Calendar.YEAR),
            mcurrentTime.get(Calendar.MONTH),
            mcurrentTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}