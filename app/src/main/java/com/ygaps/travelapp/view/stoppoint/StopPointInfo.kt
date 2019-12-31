package com.ygaps.travelapp.view.stoppoint

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ygaps.travelapp.*
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.util.util
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.taufiqrahman.reviewratings.BarLabels

import com.taufiqrahman.reviewratings.RatingReviews
import kotlinx.android.synthetic.main.activity_stop_point_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment

import com.ygaps.travelapp.model.StopPoint
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.ygaps.travelapp.manager.Constant
import com.ygaps.travelapp.network.model.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_tour_info.*
import kotlinx.android.synthetic.main.item_reviews_layout.view.*
import org.jetbrains.anko.scrollView
import java.io.File


class StopPointInfo : AppCompatActivity(), OnMapReadyCallback{

    lateinit var mGoogleMap : GoogleMap
    var token : String = ""
    var serviceId : Int = 100
    var name: String = ""
    lateinit var mReviewAdapter : ReviewAdapter
    var listFeedback = ArrayList<feedback>()
    var mCurrentPage = 1
    var mCurrentItemPerPage = 3
    var mTotal = 0
    var mapReady = false
    lateinit var stopPointLatLng: LatLng


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_point_info)
        supportActionBar!!.hide()


        var mapFragment = supportFragmentManager.findFragmentById(R.id.locateStopPoint) as SupportMapFragment
        mapFragment.getMapAsync(this)


        token = this.intent.extras!!.getString("token","notoken")!!
        serviceId = this.intent.extras!!.getInt("stpid",100)
        serviceRatingStarSelect.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (rating != 0f) {
                serviceFeedbackEditContent.visibility = View.VISIBLE
            }
        }
        btnFeedbackCancel.setOnClickListener {
            serviceFeedbackEditContent.visibility = View.GONE
            serviceRatingStarSelect.rating = 0f
        }

        btnFeedbackSubmit.setOnClickListener {
            var content = editserviceRatingContent.text.toString()
            ApiRequestSendFeedBack(serviceId, content, serviceRatingStarSelect.rating.toInt())
            serviceRatingStarSelect.setIsIndicator(true)
            serviceFeedbackEditContent.visibility = View.GONE
        }

        stoppointSeeAllReview.setOnClickListener {
            var intent = Intent(this, StopPointFeedbackActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("serviceId", serviceId)
            intent.putExtra("name", name)
            startActivity(intent)
        }

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mReviewAdapter = ReviewAdapter(listFeedback)
        stpReviewRecyclerView.adapter = mReviewAdapter
        stpReviewRecyclerView.layoutManager = layoutManager
        stpReviewRecyclerView.setHasFixedSize(false)

        ApiRequest()
        ApiRequestGetPoints()
        ApiRequestGetListFeedBack(mCurrentPage,mCurrentItemPerPage)
    }

    override fun onMapReady(p0: GoogleMap?) {
        mapReady = true
        mGoogleMap = p0!!
        if (::stopPointLatLng.isInitialized) {
            val update = CameraUpdateFactory.newLatLng(stopPointLatLng)
            val zoom = CameraUpdateFactory.zoomTo(15f)
            mGoogleMap.moveCamera(update)
            mGoogleMap.animateCamera(zoom)
        }
    }

    inner class ReviewAdapter(data: ArrayList<feedback>) :
        RecyclerView.Adapter<ReviewAdapter.RecyclerViewHolder>() {

        var data = ArrayList<feedback>()

        init {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_reviews_layout, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val item = data.get(position)
            holder.content.text = item.feedback
            holder.rating.rating = item.point.toFloat()
            holder.date.text = util.longToDate(item.createdOn)
            if (!item.name.isNullOrEmpty()) {
                holder.name.text = item.name
            }
            else {
                holder.name.text = "<No Name>"
            }

            if (!item.avatar.isNullOrEmpty()) {
                Picasso.get()
                    .load(item.avatar)
                    .resize(40, 40)
                    .centerCrop()
                    .into(holder.avatar)
            }

            holder.itemView.btnReport.setOnClickListener {
                Log.d("abab", "zozozo")
                val builder = AlertDialog.Builder(this@StopPointInfo)
                builder.setTitle("Report this feedback")
                builder.setMessage("Are you sure to report this feedback?")
                builder.setPositiveButton("YES"){dialog, which ->
                    ApiRequestReportFeedBack(item.id)
                }

                builder.setNegativeButton("No"){dialog,which ->
                    Toast.makeText(this@StopPointInfo,"Declined",Toast.LENGTH_SHORT).show()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var name: TextView
            internal var content: TextView
            internal var avatar: CircleImageView
            internal var rating : RatingBar
            internal var date : TextView

            init {
                name = itemView.findViewById(R.id.reviewerName) as TextView
                content = itemView.findViewById(R.id.reviewContent) as TextView
                date = itemView.findViewById(R.id.reviewDate) as TextView
                rating = itemView.findViewById(R.id.reviewRating) as RatingBar
                avatar = itemView.findViewById(R.id.reviewerAvatar) as CircleImageView
            }
        }
    }



    fun ApiRequest() {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetStopPointInfo::class.java)

            val call = service.getTourInfo(token,serviceId)
            call.enqueue(object : Callback<ResponseStopPointInfo> {
                override fun onFailure(call: Call<ResponseStopPointInfo>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseStopPointInfo>,
                    response: Response<ResponseStopPointInfo>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                    } else {

                        // update map
                        val stoppointLatLng : LatLng = LatLng(response.body()!!.lat!!,response.body()!!.long!!)
                        val update = CameraUpdateFactory.newLatLng(stoppointLatLng)
                        val zoom = CameraUpdateFactory.zoomTo(15f)
                        if (mapReady) {
                            mGoogleMap.moveCamera(update)
                            mGoogleMap.animateCamera(zoom)
                        }
                        else {
                            stopPointLatLng = stoppointLatLng
                        }

                        // Creating a marker
                        val markerOptions = MarkerOptions()

                        // Setting the position for the marker
                        markerOptions.position(stoppointLatLng)

                        // Setting the title for the marker.
                        // This will be displayed on taping the marker
                        markerOptions.title(response.body()!!.name)
                        name = response.body()!!.name!!

                        mGoogleMap.addMarker(markerOptions)
                        var type = util.StopPointTypeToString(response.body()!!.serviceTypeId!!)
                        serviceTypeText.text = type
                        stpInfoName.text = response.body()!!.name
                        stpInfoAddress.text = response.body()!!.address
                        stpInfoContact.text = response.body()!!.contact
                        var cost = response.body()!!.minCost.toString() + " - " + response.body()!!.maxCost.toString()
                        stpInfoCost.text = cost
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestGetListFeedBack(page : Int, item : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetServiceFeedBack::class.java)

            val call = service.getFeedback(token,serviceId,page,item.toString())
            call.enqueue(object : Callback<ResponseListFeedBackService> {
                override fun onFailure(call: Call<ResponseListFeedBackService>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseListFeedBackService>,
                    response: Response<ResponseListFeedBackService>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                    } else {
                        listFeedback.clear()
                        listFeedback.addAll(response.body()!!.feedbackList)
                        mReviewAdapter.notifyDataSetChanged()
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestGetPoints() {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetStopPointPoints::class.java)
            val call = service.getPoints(token,serviceId)
            call.enqueue(object : Callback<ResponseStopPointRatingPoints> {
                override fun onFailure(call: Call<ResponseStopPointRatingPoints>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseStopPointRatingPoints>,
                    response: Response<ResponseStopPointRatingPoints>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                    } else {


                        val ratingReviews = findViewById<RatingReviews>(R.id.rating_reviews)

                        val raters = intArrayOf(
                            response.body()!!.pointStats[0].total,
                            response.body()!!.pointStats[1].total,
                            response.body()!!.pointStats[2].total,
                            response.body()!!.pointStats[3].total,
                            response.body()!!.pointStats[4].total
                        )

                        var tempsum = 0
                        for (i in 0..raters.size-1) {
                            tempsum+= (i+1)*raters[i]
                        }


                        var maxValue = raters.max()
                        var sum = raters.sum()
                        var average = "%.1f".format(tempsum.toFloat()/sum)
                        ratingAveragePoint.text = average
                        textView2.text = sum.toString()
                        ratingBar.rating = average.toFloat()

                        ratingReviews.createRatingBars(maxValue!!, BarLabels.STYPE1, Constant.colors, raters.reversedArray())
                    }
                }
            })
        }.execute()
    }

    fun ApiRequestSendFeedBack(serviceId : Int, feedback: String, point : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceSendServiceFeedback::class.java)
            val body = JsonObject()
            body.addProperty("serviceId", serviceId)
            body.addProperty("feedback", feedback)
            body.addProperty("point", point)

            val call = service.sendFeedback(token,body)
            call.enqueue(object : Callback<ResponseFeedbackService> {
                override fun onFailure(call: Call<ResponseFeedbackService>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseFeedbackService>,
                    response: Response<ResponseFeedbackService>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(applicationContext, errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                        ApiRequestGetPoints()
                    }
                }
            })
        }.execute()
    }

    fun ApiRequestReportFeedBack(feedbackId : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceReportFeedback::class.java)
            val body = JsonObject()
            body.addProperty("feedbackId", feedbackId)
            val call = service.report(token,body)
            call.enqueue(object : Callback<ResponseReport> {
                override fun onFailure(call: Call<ResponseReport>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponseReport>,
                    response: Response<ResponseReport>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "Report success!", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }.execute()
    }
}
