package com.brogrammers.travel.ui.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.brogrammers.travel.CreateTourActivity
import com.brogrammers.travel.R
import com.brogrammers.travel.ResponseListTours
import com.brogrammers.travel.model.Tour
import com.brogrammers.travel.network.model.ApiServiceGetTours
import com.brogrammers.travel.network.model.WebAccess
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.tourview.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    var token: String = ""
    private lateinit var homeViewModel: HomeViewModel
    var listTour = ArrayList<Tour>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        val sharePref: SharedPreferences =
            this.activity!!.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        token = sharePref.getString("token", "nnn")!!

        val service = WebAccess.retrofit.create(ApiServiceGetTours::class.java)
        val call = service.getTours(token, 20, 1, "createdOn", true)
        call.enqueue(object : Callback<ResponseListTours> {
            override fun onFailure(call: Call<ResponseListTours>, t: Throwable) {
                Toast.makeText(activity!!.applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ResponseListTours>,
                response: Response<ResponseListTours>
            ) {
                Log.d("cacca", response.body()!!.total.toString())
                Log.d("cacca", response.body()!!.tours.toString())
                if (response.code() != 200) {
                    Toast.makeText(
                        activity!!.applicationContext,
                        "Load list tours failed",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    listTour.addAll(response.body()!!.tours)
                    tourNumber.text = response.body()!!.total.toString()
                    var tourAdapter = myTourAdapter(listTour, context!!)
                    var lv: ListView = root.tourListView
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

        var listTourArr = ArrayList<Tour>()
        var context: Context? = null

        constructor(listTourArr: ArrayList<Tour>, context: Context) : super() {
            this.listTourArr = listTourArr
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            //dua item vao
            var myView = layoutInflater.inflate(R.layout.tourview, null)
            var myTour = listTourArr[position]
            myView.titleItem.text = myTour.name
            if (myTour.startDate.toString().isNotEmpty() && myTour.endDate.toString().isNotEmpty()) {
                myView.dateItem.text =
                    (convertLongToTime(myTour.startDate) + " - " + convertLongToTime(myTour.endDate))
            }
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


}