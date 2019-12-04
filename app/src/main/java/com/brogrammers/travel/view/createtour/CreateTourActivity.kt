package com.brogrammers.travel

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.brogrammers.travel.util.util
import kotlinx.android.synthetic.main.activity_create_tour.*
import java.io.ByteArrayOutputStream
import java.util.*


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
            } else if (editDateStart.text.toString().isEmpty()) {
                editDateStart.error = "Required*"
            } else if (editDateEnd.text.toString().isEmpty()) {
                editDateEnd.error = "Required*"
            } else {
                val intent = Intent(this, GetCoordinateActivity::class.java)
                intent.putExtra("iTourName", editTourName.text.toString())
                intent.putExtra("iStartDate", util.dateToLong(editDateStart.text.toString()))
                intent.putExtra("iEndDate", util.dateToLong(editDateEnd.text.toString()))
                intent.putExtra("iAdultNum", editAdultNum.text.toString().toInt())
                intent.putExtra("iChildNum", editChildNum.text.toString().toInt())
                intent.putExtra("iMinCost", editMinCostTour.text.toString().toLong())
                intent.putExtra("iMaxCost", editMaxCostTour.text.toString().toLong())
                intent.putExtra("iIsPrivate", isPrivateCheckbox.isChecked)
                intent.putExtra("iImage", Base64Image)
                startActivityForResult(intent, 69)
            }
        }

        var dateStart = editDateStart
        var dateEnd = editDateEnd

        dateStart.setOnClickListener { util.setOnClickDate(dateStart,this) }
        dateEnd.setOnClickListener { util.setOnClickDate(dateEnd,this) }

        img_pick_btn.setOnClickListener {
            pickFromGallery()
        }
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
            if (data != null) {
                val selectedImage = data.data
                image_view.setImageURI(selectedImage)
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                //Use your Base64 String as you wish
                val encodedString =
                    android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


}
