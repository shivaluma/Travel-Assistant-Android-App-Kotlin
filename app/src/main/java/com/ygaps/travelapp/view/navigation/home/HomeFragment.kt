package com.ygaps.travelapp.ui.home

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonObject
import com.ygaps.travelapp.*
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.model.Tour
import com.ygaps.travelapp.network.model.ApiServiceGetTours
import com.ygaps.travelapp.network.model.WebAccess
import com.ygaps.travelapp.util.util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.materialspinner.MaterialSpinner
import com.ygaps.travelapp.network.model.ApiServiceCloneTour
import kotlinx.android.synthetic.main.activity_get_coordinate.*
import kotlinx.android.synthetic.main.activity_get_coordinate.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.tourview.*
import kotlinx.android.synthetic.main.tourview.view.*
import kotlinx.coroutines.delay
import org.jetbrains.anko.onScrollListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    var token: String = ""
    var listTour = ArrayList<Tour>()
    var rowPerPage: Int = 10
    var pageNum: Int = 1
    var orderBy : String ?= null
    var isDesc : Boolean = false
    lateinit var tourAdapter : RecyclerViewAdapter
    lateinit var lv: RecyclerView
    lateinit var loaded: TextView
    lateinit var tourNumber: TextView
    var isQuerying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val sharePref: SharedPreferences =
            this.activity!!.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        token = sharePref.getString("token", "nnn")!!

        loaded = root.findViewById<TextView>(R.id.tourLoaded)
        tourNumber = root.findViewById<TextView>(R.id.tourNumber)

        tourAdapter = RecyclerViewAdapter(listTour)
        val layoutManager = LinearLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        lv = root.findViewById<RecyclerView>(R.id.tourListView)
        lv.layoutManager = layoutManager
        lv.adapter = tourAdapter




        ApiRequest(root,rowPerPage,pageNum,orderBy,isDesc)

        val addNewBtn = root.findViewById<FloatingActionButton>(R.id.floatingaddnew)
        addNewBtn.setOnClickListener {
            startActivity(Intent(activity, CreateTourActivity::class.java))
        }

        val sv = root.findViewById<SearchView>(R.id.searchTours)
        val bntext = root.findViewById<TextView>(R.id.bannerText)
        val btnConfig = root.findViewById<ImageButton>(R.id.configButton)
        sv.setOnSearchClickListener {
            sv.maxWidth= Int.MAX_VALUE
            bntext.visibility = View.GONE
        }

        sv.setOnCloseListener(object: SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                bntext.visibility = View.VISIBLE
                isQuerying = false
                return false
            }
        })

        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                isQuerying = true
                ApiRequest(root,rowPerPage,pageNum,orderBy,isDesc,query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })

        btnConfig.setOnClickListener {
            val inflater: LayoutInflater =
                activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.tourview_setting, null)

            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
            ,true
            )

            val spnOrder = view.findViewById<MaterialSpinner>(R.id.spinnerOrderBy)
            spnOrder.setItems("id","name", "minCost", "maxCost", "startDate", "endDate")

            // Set an elevation for the popup window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }


            // If API level 23 or higher then execute the code
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.TOP
                popupWindow.exitTransition = slideOut

            }

            // Get the widgets reference from custom view
            //val tv = view.findViewById<TextView>(R.id.text_view)
            val buttonPopup = view.findViewById<ImageButton>(R.id.btnCloseConfig)
            val buttonApply = view.findViewById<Button>(R.id.btnTourViewApply)

            // Set a click listener for popup's button widget
            buttonPopup.setOnClickListener {
                // Dismiss the popup window
                Toast.makeText(this.context, "Cancelled", Toast.LENGTH_SHORT).show()
                popupWindow.dismiss()
            }

            var seekbar = view.findViewById<SeekBar>(R.id.seekBar)
            var seekbarCurrentValue = view.findViewById<TextView>(R.id.seekBarCurrentRow)
            seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    rowPerPage = progress*5 + 10
                    seekbarCurrentValue.text = rowPerPage.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })

            var isDescCheck = view.findViewById<CheckBox>(R.id.isDescCb)

            buttonApply.setOnClickListener {
                listTour.clear()
                orderBy = spnOrder.text.toString()
                isDesc = isDescCheck.isChecked
                ApiRequest(root,rowPerPage,pageNum,orderBy,isDesc)
                Toast.makeText(this.context, "Applied", Toast.LENGTH_SHORT).show()
                popupWindow.dismiss()
            }


            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(root as ViewGroup)
            popupWindow.showAtLocation(
                root, // Location to display popup window
                Gravity.TOP, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
        }

        lv.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && !isQuerying) {
                    pageNum++
                    ApiRequest(root,rowPerPage,pageNum,orderBy,isDesc)
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
            val view = inflater.inflate(R.layout.tourview, parent, false)
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

            // cost
            holder.costItem.text = (item.minCost.toString() + " - " + item.maxCost.toString())


            //onclick

            holder.itemView.cloneTourBtn.setOnClickListener {
                ApiRequestCloneTour(item.id)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context,TourInfoActivity::class.java)
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

            init {
                titleItem = itemView.findViewById(R.id.titleItem) as TextView
                dateItem = itemView.findViewById(R.id.dateItem) as TextView
                peopleItem = itemView.findViewById(R.id.peopleItem) as TextView
                costItem = itemView.findViewById(R.id.costItem) as TextView
            }
        }
    }


    fun ApiRequest(root: View, rowPerPage: Int, pageNum: Int, order: String?, isDes: Boolean, query: String = "") {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetTours::class.java)
            var requestRow = rowPerPage
            var requestPage = pageNum
            if (query.isNotEmpty()) {
                requestRow = 9999
                requestPage = 0
            }
            val call = service.getTours(token, requestRow, requestPage, order, isDes)
            call.enqueue(object : Callback<ResponseListTours> {
                override fun onFailure(call: Call<ResponseListTours>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseListTours>,
                    response: Response<ResponseListTours>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(
                            context,
                            "Load list tours failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {

                        var total = response.body()!!.total
                        if (query.isNotEmpty()) {
                            listTour.clear()
                        }
                        listTour.addAll(response.body()!!.tours)
                        if (query.isNotEmpty()) {
                            val filterList = ArrayList<Tour>()
                            var tourName = ""
                            for (i in listTour) {
                                if (!i.name.isNullOrEmpty()) {
                                    if (i.name.contains(query)) {
                                        filterList.add(i)
                                    }
                                }
                            }
                            listTour.clear()
                            listTour.addAll(filterList)
                            total = listTour.size
                        }
                        tourNumber.text = total.toString()
                        tourAdapter.notifyDataSetChanged()
                        loaded.text = listTour.size.toString()
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestCloneTour(tourId : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceCloneTour::class.java)
            val body = com.google.gson.JsonObject()
            body.addProperty("tourId", tourId)
            val call = service.clone(token, body)
            call.enqueue(object : Callback<ResponseTourInfo> {
                override fun onFailure(call: Call<ResponseTourInfo>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseTourInfo>,
                    response: Response<ResponseTourInfo>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(
                            context,
                            "Load list tours failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Clone success!!", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }.execute()
    }
}