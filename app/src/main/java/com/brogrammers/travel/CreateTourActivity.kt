package com.brogrammers.travel

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_tour.*
import java.util.*
import android.provider.MediaStore
import android.graphics.Bitmap

import android.util.Log
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.stoppoint.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlin.collections.ArrayList


class CreateTourActivity : AppCompatActivity() {

    val GALLERY_REQUEST_CODE = 12
    val Base64Image: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_create_tour)
        floatgetcoordinate.setOnClickListener {
            if (editTourName.text.toString().isEmpty()) {
                editTourName.error = "Required*"
            }
            else if (editDateStart.text.toString().isEmpty()) {
                editDateStart.error = "Required*"
            }
            else if (editDateEnd.text.toString().isEmpty()) {
                editDateEnd.error = "Required*"
            }
            else {
                val intent = Intent(this, GetCoordinateActivity::class.java)
                intent.putExtra("iTourName", editTourName.text.toString())
                intent.putExtra("iStartDate", dateToLong(editDateStart.text.toString()))
                intent.putExtra("iEndDate", dateToLong(editDateEnd.text.toString()))
                intent.putExtra("iAdultNum", editAdultNum.text.toString().toInt())
                intent.putExtra("iChildNum", editChildNum.text.toString().toInt())
                intent.putExtra("iMinCost", editMinCostTour.text.toString().toInt())
                intent.putExtra("iMaxCost", editMaxCostTour.text.toString().toInt())
                intent.putExtra("iIsPrivate", isPrivateCheckbox.isChecked)
                intent.putExtra("iImage", Base64Image)
                startActivityForResult(intent, 69)




            }
        }

        var dateStart = editDateStart
        var dateEnd = editDateEnd

        dateStart.setOnClickListener { setOnClickDate(dateStart) }
        dateEnd.setOnClickListener { setOnClickDate(dateEnd) }

        img_pick_btn.setOnClickListener {
            pickFromGallery()
        }
    }


    private fun setOnClickDate(date: EditText) {
        var mcurrentTime = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            mcurrentTime.set(Calendar.DAY_OF_MONTH, day)
            mcurrentTime.set(Calendar.MONTH, month)
            mcurrentTime.set(Calendar.YEAR, year)
            date.setText(SimpleDateFormat("dd/MM/yyyy").format(mcurrentTime.time))
        }
        DatePickerDialog(this, dateSetListener, mcurrentTime.get(Calendar.YEAR), mcurrentTime.get(
            Calendar.MONTH), mcurrentTime.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickFromGallery() {
        //Create an Intent with action as ACTION_PICK
        val intent = Intent(Intent.ACTION_PICK)
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.type = "image/*"
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (data == null) {

            }
            else {
                val selectedImage = data!!.data
                image_view.setImageURI(selectedImage)
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                //Use your Base64 String as you wish
                val encodedString = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    fun dateToLong(dateString : String): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val date = sdf.parse(dateString)
        return date.time
    }


}
