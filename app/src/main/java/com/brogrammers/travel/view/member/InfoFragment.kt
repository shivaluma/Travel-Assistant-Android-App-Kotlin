package com.brogrammers.travel.view.member

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.brogrammers.travel.R
import com.brogrammers.travel.manager.doAsync
import com.brogrammers.travel.model.StopPoint
import com.brogrammers.travel.network.model.ApiServiceGetHistoryTours
import com.brogrammers.travel.network.model.ApiServiceGetTourInfo
import com.brogrammers.travel.network.model.WebAccess
import com.brogrammers.travel.util.util
import kotlinx.android.synthetic.main.activity_create_tour.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.tourview.*
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ExpandableListAdapter
import com.brogrammers.travel.ResponseTourInfo
import com.brogrammers.travel.member


/**
 * A placeholder fragment containing a simple view.
 */
class InfoFragment : Fragment() {

    lateinit var token : String
    var tourId : Int = 0
    var listStopPoint = ArrayList<StopPoint>()
    var listMember = ArrayList<member>()

    var serviceTypeCount = arrayOf(0,0,0,0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tab_info, container, false)
        token = activity!!.intent.extras!!.getString("token","notoken")!!
        tourId = activity!!.intent.extras!!.getInt("tourID",100)
        ApiRequest(root)

//        var stpexlv = root.findViewById<ExpandableListView>(R.id.StopPointExpandListView)
//        var stoppointexpandAdapter = StopPointExpandableListAdapter(context!!,"Stop Point",listStopPoint)
//        stpexlv.setAdapter(stoppointexpandAdapter)
//
//        stpexlv.setOnGroupClickListener(ExpandableListView.OnGroupClickListener { parent, v, groupPosition, id ->
//            setListViewHeight(parent, groupPosition)
//            false
//        })

        return root
    }
    companion object {
        fun newInstance(): InfoFragment = InfoFragment()
    }



    fun ApiRequest(root : View) {
        val service = WebAccess.retrofit.create(ApiServiceGetTourInfo::class.java)
        val call = service.getTourInfo(token,tourId)
            call.enqueue(object : Callback<ResponseTourInfo> {
                override fun onFailure(call: Call<ResponseTourInfo>, t: Throwable) {
                    Toast.makeText(activity!!.applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseTourInfo>,
                    response: Response<ResponseTourInfo>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(activity!!.applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        val tourInfoName = activity!!.findViewById<TextView>(R.id.tourInfoName)
                        val tourInfoDate = root.findViewById<TextView>(R.id.tourInfoDate)
                        val tourInfoPeople = root.findViewById<TextView>(R.id.tourInfoPeople)
                        val tourInfoCost = root.findViewById<TextView>(R.id.tourInfoCost)
                        val data = response.body()!!
                        tourInfoName.text = data.name
                        tourInfoDate.text = util.longToDate(data.startDate) + " - " + util.longToDate(data.endDate)
                        var people : String = ""
                        if (data.adults >= 0) people += data.adults.toString() + " adults"
                        if (data.childs >= 0) people += " - " + data.childs.toString() + " childs"
                        tourInfoPeople.text = people
                        tourInfoCost.text = data.minCost.toString() + " - " + data.maxCost
                        listStopPoint.addAll(data.stopPoints)
                        listMember.addAll(data.members)
                        countServiceType()
                    }
                }
            })
    }


    private fun setListViewHeight(
        listView: ExpandableListView,
        group: Int
    ) {
        val listAdapter = listView.expandableListAdapter as ExpandableListAdapter
        var totalHeight = 0
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(
            listView.width,
            View.MeasureSpec.EXACTLY
        )
        for (i in 0 until listAdapter.groupCount) {
            val groupItem = listAdapter.getGroupView(i, false, null, listView)
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)

            totalHeight += groupItem.measuredHeight

            if (listView.isGroupExpanded(i) && i != group || !listView.isGroupExpanded(i) && i == group) {
                for (j in 0 until listAdapter.getChildrenCount(i)) {
                    val listItem = listAdapter.getChildView(
                        i, j, false, null,
                        listView
                    )
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)

                    totalHeight += listItem.measuredHeight

                }
            }
        }

        val params = listView.layoutParams
        var height = totalHeight + listView.dividerHeight * (listAdapter.groupCount - 1)
        if (height < 10)
            height = 200
        params.height = height
        listView.layoutParams = params
        listView.requestLayout()
    }

    fun countServiceType() {
        for (i in listStopPoint) {
            serviceTypeCount[i.serviceTypeId!!-1]++
        }
    }

}