package com.brogrammers.travel.ui.history

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
import com.brogrammers.travel.*
import com.brogrammers.travel.manager.doAsync
import com.brogrammers.travel.model.Tour
import com.brogrammers.travel.network.model.ApiServiceGetHistoryTours
import com.brogrammers.travel.network.model.ApiServiceGetTours
import com.brogrammers.travel.network.model.WebAccess
import com.brogrammers.travel.ui.home.HomeFragment
import com.brogrammers.travel.ui.home.HomeViewModel
import com.brogrammers.travel.util.util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.tourview.*
import kotlinx.android.synthetic.main.tourview.view.*
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
    var curLoaded = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val sharePref: SharedPreferences =
            this.activity!!.getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        token = sharePref.getString("token", "nnn")!!

        val tourText = root.findViewById<TextView>(R.id.bannerText)
        tourText.text = "History Tours"
        loaded = root.findViewById<TextView>(R.id.tourLoaded)
        tourAdapter = RecyclerViewAdapter(listTour)
        val layoutManager = LinearLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        lv = root.findViewById<RecyclerView>(R.id.tourListView)
        lv.layoutManager = layoutManager
        lv.adapter = tourAdapter

        ApiRequest(root,pageNum,rowPerPage.toString())

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
                ApiRequest(root,pageNum,rowPerPage.toString())
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

            init {
                titleItem = itemView.findViewById(R.id.titleItem) as TextView
                dateItem = itemView.findViewById(R.id.dateItem) as TextView
                peopleItem = itemView.findViewById(R.id.peopleItem) as TextView
                costItem = itemView.findViewById(R.id.costItem) as TextView
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
                        tourNumber.text = response.body()!!.total.toString()
                        tourAdapter.notifyDataSetChanged()
                        loaded.text = listTour.size.toString()
                        curLoaded = listTour.size
                    }
                }
            })
        }.execute()
    }
}