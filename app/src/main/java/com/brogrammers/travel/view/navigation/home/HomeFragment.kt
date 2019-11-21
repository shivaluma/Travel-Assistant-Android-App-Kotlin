package com.brogrammers.travel.ui.home

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
import com.brogrammers.travel.CreateTourActivity
import com.brogrammers.travel.R
import com.brogrammers.travel.ResponseListTours
import com.brogrammers.travel.model.Tour
import com.brogrammers.travel.network.model.ApiServiceGetTours
import com.brogrammers.travel.network.model.WebAccess
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.activity_get_coordinate.*
import kotlinx.android.synthetic.main.activity_get_coordinate.view.*
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
    var rowPerPage: Int = 10
    var pageNum: Int = 1
    var orderBy : String ?= null
    var isDesc : Boolean = false

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


    fun ApiRequest(root: View, rowPerPage: Int, pageNum: Int, order: String?, isDes: Boolean) {
        val service = WebAccess.retrofit.create(ApiServiceGetTours::class.java)
        val call = service.getTours(token, rowPerPage, pageNum, order, isDes)
        call.enqueue(object : Callback<ResponseListTours> {
            override fun onFailure(call: Call<ResponseListTours>, t: Throwable) {
                Toast.makeText(activity!!.applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ResponseListTours>,
                response: Response<ResponseListTours>
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
                    var tourAdapter = myTourAdapter(listTour, context!!)
                    var lv: ListView = root.tourListView
                    lv.adapter = tourAdapter
                }
            }
        })
    }

}