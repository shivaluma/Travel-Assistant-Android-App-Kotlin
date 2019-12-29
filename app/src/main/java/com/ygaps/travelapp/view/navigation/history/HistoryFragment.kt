package com.ygaps.travelapp.view.navigation.history

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ygaps.travelapp.*
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.model.Tour
import com.ygaps.travelapp.network.model.*
import com.ygaps.travelapp.ui.home.HomeFragment
import com.ygaps.travelapp.ui.home.HomeViewModel
import com.ygaps.travelapp.util.util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.historyview.view.*

import org.jetbrains.anko.image
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryFragment : Fragment() {

    var token: String = ""
    var listTour = ArrayList<Tour>()
    var rowPerPage: Int = 10
    var pageNum: Int = 1
    var orderBy : String ?= null
    var isDesc : Boolean = false
    lateinit var tourAdapter : RecyclerViewAdapter
    lateinit var lv: RecyclerView
    lateinit var loaded: TextView
    lateinit var curTourCount: TextView
    var curLoaded = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        val sharePref: SharedPreferences =
            this.activity!!.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        token = sharePref.getString("token", "nnn")!!

        loaded = root.findViewById<TextView>(R.id.tourLoaded)
        curTourCount = root.findViewById<TextView>(R.id.tourNumber)
        tourAdapter = RecyclerViewAdapter(listTour)
        val layoutManager = LinearLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        lv = root.findViewById<RecyclerView>(R.id.tourListView)
        lv.layoutManager = layoutManager
        lv.adapter = tourAdapter

        ApiRequest(root,pageNum,rowPerPage.toString())
        ApiRequestHistoryByStatus(root)

        val addNewBtn = root.findViewById<FloatingActionButton>(R.id.floatingaddnew)
        addNewBtn.setOnClickListener {
            startActivity(Intent(activity, CreateTourActivity::class.java))
        }

        val sv = root.findViewById<SearchView>(R.id.searchTours)
        val bntext = root.findViewById<TextView>(R.id.bannerText)

        sv.setOnSearchClickListener {
            sv.maxWidth= Int.MAX_VALUE
            bntext.visibility = View.GONE
        }

        sv.setOnCloseListener(object: SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                bntext.visibility = View.VISIBLE
                return false
            }
        })

        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                ApiRequestSearchHistoryTour(query!!, 1, "9999")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })



        lv.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {


                if (!recyclerView.canScrollVertically(1) && curLoaded >= rowPerPage) {
                    pageNum++
                    ApiRequest(root,pageNum,rowPerPage.toString())
                }

                super.onScrolled(recyclerView, dx, dy)
            }
        })


        return root
    }


    inner class RecyclerViewAdapter(data: ArrayList<Tour>) :
        RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

        var data = ArrayList<Tour>()

        init {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.historyview, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val item = data.get(position)


            holder.titleItem.text = item.name

            //date
            if (item.startDate > 0  && item.endDate > 0) {
                holder.dateItem.text =
                    (util.longToDate(item.startDate) + " - " + util.longToDate(item.endDate))
            }

            // people
            var people = ""
            if (item.adults.toString() != "null") {
                people += item.adults.toString() + " adults"
            }

            if (item.childs.toString() != "null") {
                if (!people.isEmpty()) people += " - "
                people += item.childs.toString() + " childs"
            }
            holder.peopleItem.text = people

            holder.statusItem.image = resources.getDrawable(util.getAssetByStatus(item.status))

            // cost
            holder.costItem.text = (item.minCost.toString() + " - " + item.maxCost.toString())

            holder.itemView.setOnClickListener {
                val intent = Intent(context, TourInfoActivity::class.java)
                intent.putExtra("token", token)
                intent.putExtra("tourID", item.id)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }


        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var titleItem: TextView
            internal var dateItem: TextView
            internal var peopleItem: TextView
            internal var costItem: TextView
            internal var statusItem: ImageView

            init {
                titleItem = itemView.findViewById(R.id.titleItem) as TextView
                dateItem = itemView.findViewById(R.id.dateItem) as TextView
                peopleItem = itemView.findViewById(R.id.peopleItem) as TextView
                costItem = itemView.findViewById(R.id.costItem) as TextView
                statusItem = itemView.tourStatusImage
            }
        }
    }


    fun ApiRequest(root: View, pageNum : Int, pageSize: String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetHistoryTours::class.java)
            val call = service.getTours(token, pageNum, pageSize)
            call.enqueue(object : Callback<ResponseListHistoryTours> {
                override fun onFailure(call: Call<ResponseListHistoryTours>, t: Throwable) {
                    Toast.makeText(activity!!.applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseListHistoryTours>,
                    response: Response<ResponseListHistoryTours>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(
                            activity!!.applicationContext,
                            "Load list tours failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {

                        listTour.addAll(response.body()!!.tours)
                        listTour.removeIf {
                            it.status == -1
                        }
                        if (response.body()!!.total.toString() != "0") {
                            curTourCount.text = response.body()!!.total.toString()
                        }
                        tourAdapter.notifyDataSetChanged()

                        loaded.text = listTour.size.toString()
                        curLoaded = listTour.size
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestHistoryByStatus(view : View) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetHistoryToursByStatus::class.java)
            val call = service.getToursByStatus(token)
            call.enqueue(object : Callback<ResponseListHistoryToursByStatus> {
                override fun onFailure(call: Call<ResponseListHistoryToursByStatus>, t: Throwable) {
                    Toast.makeText(activity!!.applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseListHistoryToursByStatus>,
                    response: Response<ResponseListHistoryToursByStatus>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(
                            activity!!.applicationContext,
                            "Load list tours failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        var item = response.body()!!.totalToursGroupedByStatus
                        view.numTourCancelled.text = item[0].total.toString()
                        view.numTourOpenned.text = item[1].total.toString()
                        view.numTourStarted.text = item[2].total.toString()
                        view.numTourClosed.text = item[3].total.toString()
                    }
                }
            })
        }.execute()
    }



    fun ApiRequestSearchHistoryTour(searchKey : String, pageNum : Int, pageSize: String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceSearchHistoryTours::class.java)
            val call = service.searchTours(token,searchKey, pageNum, pageSize)
            call.enqueue(object : Callback<ResponseListHistoryTours> {
                override fun onFailure(call: Call<ResponseListHistoryTours>, t: Throwable) {
                    Toast.makeText(activity!!.applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseListHistoryTours>,
                    response: Response<ResponseListHistoryTours>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(
                            activity!!.applicationContext,
                            "Load list tours failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        listTour.clear()
                        listTour.addAll(response.body()!!.tours)
                        curTourCount.text = response.body()!!.total.toString()
                        tourAdapter.notifyDataSetChanged()
                        loaded.text = listTour.size.toString()
                        curLoaded = listTour.size
                    }
                }
            })
        }.execute()
    }
}