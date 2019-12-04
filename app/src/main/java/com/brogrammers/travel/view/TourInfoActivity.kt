package com.brogrammers.travel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.transition.Slide
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.brogrammers.travel.manager.doAsync
import com.brogrammers.travel.model.StopPoint
import com.brogrammers.travel.network.model.*
import com.brogrammers.travel.util.util
import com.brogrammers.travel.view.member.MemberListOfTour
import com.brogrammers.travel.view.stoppoint.StopPointInfo
import com.brogrammers.travel.view.tourinfo.comment.TourCommentList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_member_list_of_tour.*
import kotlinx.android.synthetic.main.activity_tour_info.*
import kotlinx.android.synthetic.main.layout_tour_comment.*
import kotlinx.android.synthetic.main.layout_tour_comment.view.*
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Member
import kotlin.reflect.typeOf

class TourInfoActivity : AppCompatActivity() {


    lateinit var token : String
    var tourId : Int = 0
    var listStopPoint = ArrayList<StopPoint>()
    var listComment = ArrayList<comment>()
    var listReviews = ArrayList<review>()
    var typeCount = arrayOf(0,0,0,0)
    lateinit var StpLV : RecyclerView
    lateinit var StpAdt : StopPointAdapter
    lateinit var CommentAdt : CommentAdapter
    lateinit var ReviewAdt : ReviewAdapter
    lateinit var CommentNumCountView : TextView
    lateinit var ReviewNumCountView : TextView
    var hasInitCommentNumCountView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_info)
        token = this.intent.extras!!.getString("token","notoken")!!
        tourId = this.intent.extras!!.getInt("tourID",100)
        supportActionBar!!.hide()
        tourRating.rating = 3.2f
        Log.d("abab", tourId.toString())

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        stopPointRecyclerView.layoutManager = layoutManager
        StpAdt = StopPointAdapter(listStopPoint)
        stopPointRecyclerView.adapter = StpAdt
        CommentAdt = CommentAdapter(listComment)
        ReviewAdt = ReviewAdapter(listReviews)
        ApiRequest()
        ApiRequestGetReviewList(tourId)




        tourListMembers.setOnClickListener {
            val intent = Intent(this, MemberListOfTour::class.java)
            intent.putExtra("tourId",tourId)
            intent.putExtra("token",token)
            startActivity(intent)
        }

        tourListComments.setOnClickListener {
            popupComment()
        }

        tourListReviews.setOnClickListener {
            popupReview()
        }

        tourMenu.setOnClickListener {
            popupSetting()
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


            holder.menubtn.setOnClickListener {
                //creating a popup menu
                val popup = PopupMenu(applicationContext, holder.menubtn)
                //inflating menu from xml resource
                popup.inflate(R.menu.stop_point_menu)
                //adding click listener
                popup.setOnMenuItemClickListener(object:  PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(menuitem: MenuItem?): Boolean {
                    if (menuitem!!.itemId == R.id.edit_stoppoint) {
                        Toast.makeText(applicationContext, "Chọn sửa", Toast.LENGTH_SHORT).show()
                    }
                    else if (menuitem.itemId == R.id.remove_stoppoint) {
                        ApiRequestRemoveStopPoint(tourId,item.id)
                    }

                    return false
                }
            })
        //displaying the popup
                popup.show()
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(applicationContext, StopPointInfo::class.java)
                intent.putExtra("token", token)
                intent.putExtra("stpid", item.id)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var name: TextView
            internal var time: TextView
            internal var type: TextView
            internal var cost: TextView
            internal var menubtn: ImageButton

            init {
                name = itemView.findViewById(R.id.itemstpname) as TextView
                time = itemView.findViewById(R.id.itemstptime) as TextView
                type = itemView.findViewById(R.id.itemstptype) as TextView
                cost = itemView.findViewById(R.id.itemstpcost) as TextView
                menubtn = itemView.findViewById(R.id.tourMenu) as ImageButton
            }
        }
    }


    inner class CommentAdapter(data: ArrayList<comment>) :
        RecyclerView.Adapter<CommentAdapter.RecyclerViewHolder>() {

        var data = ArrayList<comment>()

        init {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_comments_layout, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val item = data.get(position)
            holder.content.setText(item.comment)

            if (!item.name.isNullOrEmpty()) {
                holder.name.setText(item.name)
                return
            }
            else {
                holder.name.setText("<Không tên> : ID = ${item.id}")
            }

            if (!item.avatar.isNullOrEmpty()) {
                Picasso.get()
                    .load(item.avatar)
                    .resize(50, 50)
                    .centerCrop()
                    .into(holder.avatar)
            }




        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var name: TextView
            internal var content: TextView
            internal var avatar: CircleImageView

            init {
                name = itemView.findViewById(R.id.commentName) as TextView
                content = itemView.findViewById(R.id.commentContent) as TextView
                avatar = itemView.findViewById(R.id.commentAvatar) as CircleImageView
            }
        }
    }

    inner class ReviewAdapter(data: ArrayList<review>) :
        RecyclerView.Adapter<ReviewAdapter.RecyclerViewHolder>() {

        var data = ArrayList<review>()

        init {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_comments_layout, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val item = data.get(position)
            holder.content.setText(item.review)

            if (!item.name.isNullOrEmpty()) {
                holder.name.setText(item.name)
                return
            }
            else {
                holder.name.setText("<Không tên> : ID = ${item.id}")
            }

            if (!item.avatar.isNullOrEmpty()) {
                Picasso.get()
                    .load(item.avatar)
                    .resize(50, 50)
                    .centerCrop()
                    .into(holder.avatar)
            }




        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var name: TextView
            internal var content: TextView
            internal var avatar: CircleImageView

            init {
                name = itemView.findViewById(R.id.commentName) as TextView
                content = itemView.findViewById(R.id.commentContent) as TextView
                avatar = itemView.findViewById(R.id.commentAvatar) as CircleImageView
            }
        }
    }


    fun ApiRequest() {
        doAsync {
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
                        Log.d("abab",response.body().toString())
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
                        listStopPoint.clear()
                        listStopPoint.addAll(data.stopPoints)
                        listComment.clear()
                        listComment.addAll(data.comments)
                        StpAdt.notifyDataSetChanged()
                        CommentAdt.notifyDataSetChanged()
                        tourMemberNum.setText(data.members.size.toString())
                        tourCommentNum.setText(data.comments.size.toString())
                        if (hasInitCommentNumCountView) {
                            CommentNumCountView.setText(data.comments.size.toString() + " comments")
                        }
                        countTypeStopPoint()
                    }
                }
            })
        }.execute()
    }

    fun ApiRequestNewComment(tourId : String, userId : String, cm : String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceTourComment::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("tourId", tourId)
            jsonObject.addProperty("userId", userId)
            jsonObject.addProperty("comment", cm)
            val call = service.comment(token,jsonObject)
            call.enqueue(object : Callback<ResponseToComment> {
                override fun onFailure(call: Call<ResponseToComment>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseToComment>,
                    response: Response<ResponseToComment>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        ApiRequest()
                    }
                }
            })
        }.execute()
    }

    fun ApiRequestGetReviewList(tourId : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetTourReview::class.java)
            val call = service.getReview(token,tourId,1,"9999")
            call.enqueue(object : Callback<ResponseGetReviewsTour> {
                override fun onFailure(call: Call<ResponseGetReviewsTour>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponseGetReviewsTour>,
                    response: Response<ResponseGetReviewsTour>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("abab",response.body().toString())
//                        listReviews.clear()
//                        listReviews.addAll(response.body()!!.reviews)
//                        mainReviewCount.text = listReviews.size.toString()
//                        ReviewAdt.notifyDataSetChanged()
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestRemoveStopPoint(tourId : Int, stpid : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceAddStopPointToTour::class.java)
            val json = JsonObject()
            json.addProperty("tourId", tourId)
            var array = JsonArray()
            array.add(stpid)
            json.add("deleteIds",array)

            val call = service.postData(token, json)
            call.enqueue(object : Callback<ResponseAddStopPoint> {
                override fun onFailure(call: Call<ResponseAddStopPoint>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponseAddStopPoint>,
                    response: Response<ResponseAddStopPoint>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("abab",response.body().toString())
                        Toast.makeText(applicationContext, "Đã xoá", Toast.LENGTH_LONG).show()
                        ApiRequest()
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestAddReview(tourId : Int, point: Int, review: String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceAddReview::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("tourId", tourId)
            jsonObject.addProperty("point", point)
            jsonObject.addProperty("review", review)

            val call = service.addReview(token,jsonObject)

            call.enqueue(object : Callback<ResponseToAddReview> {
                override fun onFailure(call: Call<ResponseToAddReview>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponseToAddReview>,
                    response: Response<ResponseToAddReview>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("abab", point.toString() + " " + review)
                        ApiRequestGetReviewList(tourId)
                    }
                }
            })
        }.execute()
    }

    fun countTypeStopPoint() {

        for (i in typeCount.indices) {
            typeCount[i] = 0
        }
        for (i in listStopPoint) {
            typeCount[i.serviceTypeId!!-1]++
        }
        countRes.setText(typeCount[0].toString())
        countHotel.setText(typeCount[1].toString())
        countRest.setText(typeCount[2].toString())
        countOthers.setText(typeCount[3].toString())
    }

    fun popupComment() {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_tour_comment, null)

        var commentView = view.findViewById<RecyclerView>(R.id.commentRecyclerView)
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        commentView.layoutManager = layoutManager
        commentView.adapter = CommentAdt


        var commentNumField = view.findViewById<TextView>(R.id.commentNum)
        CommentNumCountView = commentNumField
        hasInitCommentNumCountView = true
        commentNumField.setText(listComment.size.toString() + " comments")

        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Window height
        true
        )

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
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut

        }

        // Get the widgets reference from custom view
        //val tv = view.findViewById<TextView>(R.id.text_view)

        val sendbtn = view.findViewById<ImageView>(R.id.send)
        val contentCM = view.findViewById<EditText>(R.id.commenttext)

        sendbtn.setOnClickListener {
            val data:String = contentCM.text.toString()
            ApiRequestNewComment(tourId.toString(),"126",data)
            contentCM.setText("")
            contentCM.clearFocus()
        }

        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
            Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
        }


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            tourInfoMainLayout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }

    fun popupReview() {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_tour_review, null)

        var reviewView = view.findViewById<RecyclerView>(R.id.reviewRecyclerView)
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        reviewView.layoutManager = layoutManager
        reviewView.adapter = ReviewAdt


        var reviewNumField = view.findViewById<TextView>(R.id.reviewNum)
        ReviewNumCountView = reviewNumField
        hasInitCommentNumCountView = true
        reviewNumField.setText(listReviews.size.toString() + " reviews")

        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Window height
            true
        )

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
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut

        }

        // Get the widgets reference from custom view
        //val tv = view.findViewById<TextView>(R.id.text_view)
        val btn = view.findViewById<Button>(R.id.addReviewBtn)

        btn.setOnClickListener {
            val ratingview = inflater.inflate(R.layout.layout_send_rating_tour, null)

            val ratingPopup = PopupWindow(
                ratingview, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
                true
            )

            // Set an elevation for the popup window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ratingPopup.elevation = 10.0F
            }

            // Set a dismiss listener for popup window
            ratingPopup.setOnDismissListener {
                Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
            }

            val sendrvbutton = ratingview.findViewById<Button>(R.id.sendReviewBtn)
            val pointrv = ratingview.findViewById<RatingBar>(R.id.tourRatingStarSelect)
            val reviewcontent = ratingview.findViewById<EditText>(R.id.editTourRatingContent)

            sendrvbutton.setOnClickListener {
                ApiRequestAddReview(tourId,pointrv.rating.toInt(), reviewcontent.text.toString())
            }

            // Finally, show the popup window on app
            ratingPopup.showAtLocation(
                tourInfoMainLayout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
        }


        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
            Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
        }


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            tourInfoMainLayout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }


    fun popupSetting() {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_setting_bottom, null)



        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
            true
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }


        // If API level 23 or higher then execute the code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.BOTTOM
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.BOTTOM
            popupWindow.exitTransition = slideOut

        }

        // Get the widgets reference from custom view
        //val tv = view.findViewById<TextView>(R.id.text_view)



        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
            Toast.makeText(applicationContext, "Popup closed", Toast.LENGTH_SHORT).show()
        }


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            tourInfoMainLayout, // Location to display popup window
            Gravity.BOTTOM, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }


}
