package com.brogrammers.travel.ui.history

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.brogrammers.travel.*
import com.brogrammers.travel.ui.history.HistoryViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.tourview.*
import kotlinx.android.synthetic.main.tourview.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList

class HistoryFragment : Fragment() {

    var token : String = ""

    var listTours = ArrayList<tours>()
    val BASE_URL = "http://35.197.153.192:3000"
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        val sharePref : SharedPreferences = this.activity!!.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        token = sharePref.getString("token", "nnn")!!
        val service = retrofit.create(ApiGetTours::class.java)
        val call = service.getTours(token,1,"100")

        call.enqueue(object : Callback<getToursResult> {
            override fun onFailure(call: Call<getToursResult>, t: Throwable) {
                Toast.makeText(activity!!.applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<getToursResult>,
                response: Response<getToursResult>
            ) {
                Log.d("cacca", response.body()!!.total.toString())
                Log.d("cacca", response.body()!!.tours.toString())
                if (response.code() != 200) {
                    Toast.makeText(activity!!.applicationContext, "fail", Toast.LENGTH_LONG).show()
                }
                else {
                    listTours.addAll(response.body()!!.tours)
                    root.historytourNumber.text = listTours.size.toString()
                    var tourAdapter = myTourAdapter(listTours, context!!)
                    var lv : ListView = root.historytourListView
                    lv.adapter = tourAdapter
                }
            }
        })

        return root
    }


    inner class myTourAdapter : BaseAdapter {

        var listTourArr = ArrayList<tours>()
        var context: Context? = null

        constructor(listTourArr: ArrayList<tours>, context: Context) : super() {
            this.listTourArr = listTourArr
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            //dua item vao
            var myView = layoutInflater.inflate(R.layout.tourview, null)
            var myTour = listTourArr[position]
            myView.titleItem.text = myTour.name
            myView.dateItem.text = (convertLongToTime(myTour.startDate!!) + " - " + convertLongToTime(myTour.endDate!!))
            var people = ""
            if (myTour.adults.toString() != "null") {
                people += myTour.adults.toString() + " adults"
            }

            if (myTour.childs.toString() != "null") {
                if (!people.isEmpty()) people += " - "
                people += myTour.childs.toString() + " childs"
            }
            myView.peopleItem.text = people
            myView.costItem.text = (myTour.minCost.toString() + " - " + myTour.maxCost.toString())
            return myView
        }

        override fun getItem(position: Int): Any {
            return listTourArr[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listTourArr.size
        }

        fun convertLongToTime(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("dd.MM.yyyy")
            return format.format(date)
        }


    }

    inner class tours {
        var id : Int ?= null
        var status : Int ?= null
        var name : String ?= null
        var minCost : Int ?= null
        var maxCost : Int ?= null
        var startDate: Long ?= null
        var endDate: Long ?= null
        var adults : Int ?= null
        var childs : Int ?= null
        var isPrivate: Boolean ?= null
        var avatar : String ?= null
        var isHost : Boolean ?= null
        var isKicked : Boolean ?= null
    }


    private interface ApiGetTours {
        @GET("/tour/history-user")
        fun getTours(
            @Header("Authorization") token : String,
            @Query("pageIndex") pageIndex: Int,
            @Query("pageSize") pageSize: String
        ): Call<getToursResult>
    }

    data class getToursResult(var total:Int, var tours:ArrayList<tours>, var message:String)

}