package com.brogrammers.travel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.brogrammers.travel.model.StopPoint
import com.brogrammers.travel.network.model.ApiServiceGetTourInfo
import com.brogrammers.travel.network.model.WebAccess
import com.brogrammers.travel.util.util
import com.brogrammers.travel.view.member.MemberListOfTour
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_tour_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Member

class TourInfoActivity : AppCompatActivity() {


    lateinit var token : String
    var tourId : Int = 0
    var listStopPoint = ArrayList<StopPoint>()
    var typeCount = arrayOf(0,0,0,0)
    lateinit var StpLV : RecyclerView
    lateinit var StpAdt : StopPointAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_info)

        token = this.intent.extras!!.getString("token","notoken")!!
        tourId = this.intent.extras!!.getInt("tourID",100)
        supportActionBar!!.hide()
        tourRating.numStars = 5

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        stopPointRecyclerView.layoutManager = layoutManager
        StpAdt = StopPointAdapter(listStopPoint)
        stopPointRecyclerView.adapter = StpAdt


        ApiRequest()




        tourListMembers.setOnClickListener {
            val intent = Intent(this, MemberListOfTour::class.java)
            intent.putExtra("tourId",tourId)
            intent.putExtra("token",token)
            startActivity(intent)
        }
    }


    inner class StopPointAdapter(data: ArrayList<StopPoint>) :
        RecyclerView.Adapter<StopPointAdapter.RecyclerViewHolder>() {

        var data = ArrayList<StopPoint>()

        init {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.stop_point_item_view, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val item = data.get(position)
            holder.name.setText(item.name)
            holder.time.setText(Html.fromHtml(util.longToDateTime(item.arrivalAt!!) + " <br> " + util.longToDateTime(item.leaveAt!!)))
            holder.type.setText(util.StopPointTypeToString(item.serviceTypeId!!))
            holder.cost.setText(item.minCost.toString() + " - " + item.maxCost.toString())
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var name: TextView
            internal var time: TextView
            internal var type: TextView
            internal var cost: TextView

            init {
                name = itemView.findViewById(R.id.itemstpname) as TextView
                time = itemView.findViewById(R.id.itemstptime) as TextView
                type = itemView.findViewById(R.id.itemstptype) as TextView
                cost = itemView.findViewById(R.id.itemstpcost) as TextView
            }
        }
    }


    fun ApiRequest() {
        val service = WebAccess.retrofit.create(ApiServiceGetTourInfo::class.java)
        val call = service.getTourInfo(token,tourId)
        call.enqueue(object : Callback<ResponseTourInfo> {
            override fun onFailure(call: Call<ResponseTourInfo>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<ResponseTourInfo>,
                response: Response<ResponseTourInfo>
            ) {
                if (response.code() != 200) {
                    Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                } else {
                    val tourInfoName = tourInfoName
                    val tourInfoDate = tourInfoDate
                    val tourInfoPeople = tourInfoPeople
                    val tourInfoCost = tourInfoCost
                    val data = response.body()!!
                    tourInfoName.setText(data.name)
                    tourInfoDate.setText(util.longToDate(data.startDate) + " - " + util.longToDate(data.endDate))
                    var people : String = ""
                    if (data.adults >= 0) people += data.adults.toString() + " adults"
                    if (data.childs >= 0) people += " - " + data.childs.toString() + " childs"
                    tourInfoPeople.setText(people)
                    tourInfoCost.setText(data.minCost.toString() + " - " + data.maxCost)
                    listStopPoint.addAll(data.stopPoints)
                    StpAdt.notifyDataSetChanged()
                    tourMemberNum.setText(data.members.size.toString())
                    tourCommentNum.setText(data.comments.size.toString())
                    countTypeStopPoint()
                }
            }
        })
    }

    fun countTypeStopPoint() {
        for (i in listStopPoint) {
            typeCount[i.serviceTypeId!!-1]++
        }
        countRes.setText(typeCount[0].toString())
        countHotel.setText(typeCount[1].toString())
        countRest.setText(typeCount[2].toString())
        countOthers.setText(typeCount[3].toString())
    }
}
