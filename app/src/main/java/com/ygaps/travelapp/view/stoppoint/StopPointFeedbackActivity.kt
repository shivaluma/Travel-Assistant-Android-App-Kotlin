package com.ygaps.travelapp.view.stoppoint

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.Api
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import com.ygaps.travelapp.R
import com.ygaps.travelapp.ResponseListFeedBackService
import com.ygaps.travelapp.ResponseReport
import com.ygaps.travelapp.feedback
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.network.model.ApiServiceGetServiceFeedBack
import com.ygaps.travelapp.network.model.ApiServiceReportFeedback
import com.ygaps.travelapp.network.model.WebAccess
import com.ygaps.travelapp.util.util
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_stop_point_feedback.*
import kotlinx.android.synthetic.main.activity_stop_point_info.*
import kotlinx.android.synthetic.main.item_reviews_layout.*
import kotlinx.android.synthetic.main.item_reviews_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StopPointFeedbackActivity : AppCompatActivity() {

    lateinit var mReviewAdapter : ReviewAdapter
    var mListFeedback = ArrayList<feedback>()
    var token: String = ""
    var serviceId: Int = 0
    var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_point_feedback)
        supportActionBar?.hide()
        mReviewAdapter = ReviewAdapter(mListFeedback)
        token = intent.extras!!.getString("token")!!
        serviceId = intent.extras!!.getInt("serviceId")

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        reviewStopPointName.text = intent.extras!!.getString("name", "No name service!")!!
        reviewRecyclerView.layoutManager = layoutManager
        reviewRecyclerView.adapter = mReviewAdapter

        finishBtn.setOnClickListener {
            finish()
        }

        ApiRequestGetListFeedBack(1,10)
        currentPage = 1

        reviewRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    currentPage++
                    ApiRequestGetListFeedBack(currentPage,10)
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })



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
            holder.date.text = util.longToDate(item.createOn)
            if (!item.name.isNullOrEmpty()) {
                holder.name.text = item.name
            }
            else {
                holder.name.text = "ID = ${item.id}"
            }

            if (!item.avatar.isNullOrEmpty()) {
                Picasso.get()
                    .load(item.avatar)
                    .resize(40, 40)
                    .centerCrop()
                    .into(holder.avatar)
            }


            holder.itemView.btnReport.setOnClickListener {
                val builder = AlertDialog.Builder(this@StopPointFeedbackActivity)
                builder.setTitle("Report this feedback")
                builder.setMessage("Are you sure to report this feedback?")
                builder.setPositiveButton("YES"){dialog, which ->
                    ApiRequestReportFeedBack(item.id)
                }
                builder.setNegativeButton("No"){dialog,which ->
                    Toast.makeText(this@StopPointFeedbackActivity,"Declined",Toast.LENGTH_SHORT).show()
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


    fun ApiRequestGetListFeedBack(page : Int, item : Int) {

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
                        mListFeedback.addAll(response.body()!!.feedbackList)
                        if (mListFeedback.size == 0) stoppointSeeAllReview.visibility = View.GONE
                        mReviewAdapter.notifyDataSetChanged()
                    }
                }
            })
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
