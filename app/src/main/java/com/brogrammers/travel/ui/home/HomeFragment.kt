package com.brogrammers.travel.ui.home

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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
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

class HomeFragment : Fragment() {

    var token : String = ""
    private lateinit var homeViewModel: HomeViewModel
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
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val sharePref : SharedPreferences = this.activity!!.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        token = sharePref.getString("token", "nnn")!!


        val service = retrofit.create(ApiGetTours::class.java)

        val call = service.getTours(token,20,1,"createdOn",true)



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
                    tourNumber.text = response.body()!!.total.toString()
                    var tourAdapter = myTourAdapter(listTours, context!!)
                    var lv : ListView = root.tourListView
                    lv.adapter = tourAdapter
                }
            }
        })

        val addNewBtn = root.findViewById<FloatingActionButton>(R.id.floatingaddnew)
        addNewBtn.setOnClickListener {
            startActivity(Intent(activity, CreateTourActivity::class.java))
        }

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
            if (myTour.startDate != null && myTour.endDate != null) {
                myView.dateItem.text = (convertLongToTime(myTour.startDate!!) + " - " + convertLongToTime(myTour.endDate!!))
            }
            var people = ""
            if (myTour.aduls.toString() != "null") {
                people += myTour.aduls.toString() + " adults"
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
        var aduls : Int ?= null
        var childs : Int ?= null
        var isPrivate: Boolean ?= null
        var avatar : String ?= null
    }


    private interface ApiGetTours {
        @GET("/tour/list")
        fun getTours(
            @Header("Authorization") token : String,
            @Query("rowPerPage") rowPerPage: Int,
            @Query("pageNum") pageNum: Int,
            @Query("orderBy") orderBy: String,
            @Query("isDesc") isDesc: Boolean
        ): Call<getToursResult>
    }

    data class getToursResult(var total:Int, var tours:ArrayList<tours>, var message:String)

}