package com.brogrammers.travel.ui.history

import android.content.Context
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
import com.brogrammers.travel.R
import com.brogrammers.travel.ResponseListHistoryTours
import com.brogrammers.travel.model.Tour
import com.brogrammers.travel.network.model.ApiServiceGetHistoryTours
import com.brogrammers.travel.network.model.WebAccess
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.android.synthetic.main.tourview.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryFragment : Fragment() {

    var token: String = ""
    var listTours = ArrayList<Tour>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        val sharePref: SharedPreferences =
            this.activity!!.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        token = sharePref.getString("token", "nnn")!!
        val service = WebAccess.retrofit.create(ApiServiceGetHistoryTours::class.java)
        val call = service.getTours(token, 1, "100")

        call.enqueue(object : Callback<ResponseListHistoryTours> {
            override fun onFailure(call: Call<ResponseListHistoryTours>, t: Throwable) {
                Toast.makeText(activity!!.applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ResponseListHistoryTours>,
                response: Response<ResponseListHistoryTours>
            ) {
                Log.d("cacca", response.body()!!.total.toString())
                Log.d("cacca", response.body()!!.tours.toString())
                if (response.code() != 200) {
                    Toast.makeText(activity!!.applicationContext, "fail", Toast.LENGTH_LONG).show()
                } else {
                    listTours.addAll(response.body()!!.tours)
                    root.historytourNumber.text = listTours.size.toString()
                    var tourAdapter = myTourAdapter(listTours, context!!)
                    var lv: ListView = root.historytourListView
                    lv.adapter = tourAdapter
                }
            }
        })

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
            myView.dateItem.text =
                (convertLongToTime(myTour.startDate) + " - " + convertLongToTime(myTour.endDate))
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