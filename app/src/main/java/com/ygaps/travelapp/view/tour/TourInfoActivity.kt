package com.ygaps.travelapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.model.StopPoint
import com.ygaps.travelapp.network.model.*
import com.ygaps.travelapp.util.util
import com.ygaps.travelapp.view.createtour.ChangeTourInfo
import com.ygaps.travelapp.view.member.MemberListOfTour
import com.ygaps.travelapp.view.stoppoint.StopPointInfo


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.ygaps.travelapp.view.stoppoint.StopPointEditActivity
import com.ygaps.travelapp.view.tour.TourFollowActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_member_list_of_tour.*
import kotlinx.android.synthetic.main.activity_tour_info.*
import kotlinx.android.synthetic.main.item_choose_destination.view.*
import kotlinx.android.synthetic.main.layout_tour_comment.*
import kotlinx.android.synthetic.main.layout_tour_comment.view.*
import kotlinx.android.synthetic.main.popup_choose_destination.*
import kotlinx.android.synthetic.main.popup_choose_destination.view.*
import kotlinx.android.synthetic.main.popup_setting_bottom.view.*
import org.jetbrains.anko.image
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Member
import kotlin.math.log
import kotlin.reflect.typeOf

class TourInfoActivity : AppCompatActivity() {


    lateinit var token : String
    var tourId : Int = 0
    var listStopPoint = ArrayList<StopPoint>()
    var listComment = ArrayList<comment>()
    var listReviews = ArrayList<review>()
    var listMembers = ArrayList<member>()
    var typeCount = arrayOf(0,0,0,0)
    var currentUserId = 126
    lateinit var startLatLang : LatLng
    lateinit var endLatLng: LatLng
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
        ApiRequestGetComment(tourId.toString())

        val sharePref : SharedPreferences = getSharedPreferences("logintoken", Context.MODE_PRIVATE)
        currentUserId = sharePref.getInt("userId",126)

        Log.d("abab", "Curernt id : ${currentUserId}")

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


        btnStartGoingTour.setOnClickListener {
            popupStartGoing()
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
            holder.name.text = item.name
            holder.time.text = Html.fromHtml(util.longToDateTime(item.arrivalAt!!) + " <br> " + util.longToDateTime(item.leaveAt!!))
            holder.type.text = util.StopPointTypeToString(item.serviceTypeId!!)
            holder.cost.text = item.minCost.toString() + " - " + item.maxCost.toString()


            holder.menubtn.setOnClickListener {
                //creating a popup menu
                val popup = PopupMenu(applicationContext, holder.menubtn)
                //inflating menu from xml resource
                popup.inflate(R.menu.stop_point_menu)
                //adding click listener
                popup.setOnMenuItemClickListener(object:  PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(menuitem: MenuItem?): Boolean {
                    if (menuitem!!.itemId == R.id.edit_stoppoint) {
                        val intent = Intent(applicationContext, StopPointEditActivity::class.java)
                        intent.putExtra("id", item.id)
                        intent.putExtra("serviceId", item.serviceId)
                        intent.putExtra("serviceTypeId", item.serviceTypeId!!)
                        intent.putExtra("name", item.name)
                        intent.putExtra("lat", item.lat)
                        intent.putExtra("long", item.long)
                        intent.putExtra("arriveAt", item.arrivalAt!!)
                        intent.putExtra("leaveAt", item.leaveAt!!)
                        intent.putExtra("minCost", item.minCost)
                        intent.putExtra("maxCost", item.maxCost)
                        intent.putExtra("index", item.index)
                        intent.putExtra("address", item.address)
                        intent.putExtra("maxindex", data.size)
                        intent.putExtra("token", token)
                        startActivity(intent)
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
                intent.putExtra("stpid", item.serviceId)
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
            holder.content.text = item.comment

            if (!item.name.isNullOrEmpty()) {
                holder.name.text = item.name
                return
            }
            else {
                holder.name.text = "<Không tên> : ID = ${item.id}"
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
            holder.content.text = item.review

            if (!item.name.isNullOrEmpty()) {
                holder.name.text = item.name
                return
            }
            else {
                holder.name.text = "<Không tên> : ID = ${item.id}"
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




    inner class ChooseDestinationAdapter(data: ArrayList<StopPoint>) :
        RecyclerView.Adapter<ChooseDestinationAdapter.RecyclerViewHolder>() {

        var data = ArrayList<StopPoint>()

        init {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_choose_destination, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val item = data.get(position)
            holder.name.setText(Html.fromHtml("To : <b>${item.name}</b>"))
            holder.type.image = getDrawable(util.getAssetByStopPointType(item.serviceTypeId!!))

            holder.itemView.setOnClickListener {
                var intent = Intent(applicationContext, TourFollowActivity::class.java)
                intent.putExtra("destinationLat", item.lat!!)
                intent.putExtra("destinationLng", item.long!!)
                intent.putExtra("tourId", tourId)
                intent.putExtra("token", token)
                intent.putExtra("desId", item.id)

                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var name: TextView
            internal var type: ImageView

            init {
                name = itemView.destinationName
                type = itemView.destinationTypeIcon
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
                        tourInfoName.text = data.name
                        var drawable = resources.getDrawable(util.getAssetByStatus(data.status))
                        tourInfoName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                        tourInfoDate.text = util.longToDate(data.startDate) + " - " + util.longToDate(data.endDate)
                        var people : String = ""
                        if (data.adults >= 0) people += data.adults.toString() + " adults"
                        if (data.childs >= 0) people += " - " + data.childs.toString() + " childs"
                        tourInfoPeople.text = people
                        tourInfoCost.text = data.minCost.toString() + " - " + data.maxCost
                        listStopPoint.clear()
                        listStopPoint.addAll(data.stopPoints)

                        listMembers.clear()
                        listMembers.addAll(data.members)

                        var hasJoined = false
                        for (i in listMembers) {
                            if (i.id == currentUserId) {
                                hasJoined = true
                            }
                        }

                        if (!hasJoined) {
                            btnStartGoingTour.text = "Join this tour"
                            btnStartGoingTour.setOnClickListener {
                                ApiRequestAddUserToTour(tourId, currentUserId, data.isPrivate)
                            }
                        }
                        else {
                            btnStartGoingTour.text = "Start Going"
                            btnStartGoingTour.setOnClickListener {
                                popupStartGoing()
                            }
                        }



                        StpAdt.notifyDataSetChanged()

                        tourMemberNum.text = data.members.size.toString()


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
                        ApiRequestGetComment(tourId)
                    }
                }
            })
        }.execute()
    }

    fun ApiRequestGetComment(tourId : String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetCommentList::class.java)
            val call = service.getList(token,tourId,1,"9999")
            call.enqueue(object : Callback<ResponseCommentList> {
                override fun onFailure(call: Call<ResponseCommentList>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseCommentList>,
                    response: Response<ResponseCommentList>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        var data = response.body()
                        listComment.clear()
                        listComment.addAll(data!!.commentList)
                        CommentAdt.notifyDataSetChanged()
                        tourCommentNum.text = data.commentList.size.toString()
                        if (hasInitCommentNumCountView) {
                            CommentNumCountView.text = listComment.size.toString() + " comments"
                        }
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


    fun ApiRequestAddReview(tourId : Int, point: Int, review: String, ratingPopup : PopupWindow) {
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
                        ratingPopup.dismiss()
                        ApiRequestGetReviewList(tourId)
                    }
                }
            })
        }.execute()
    }

    fun ApiRequestAddUserToTour(tourId : Int, userId: Int, isTourPrivate: Boolean) {
        doAsync {
            val serviceuser = WebAccess.retrofit.create(ApiServiceAddUserToTour::class.java)
            val jsonObject = JsonObject()
            Log.d("abab", tourId.toString() + " " + userId.toString()+ " " + isTourPrivate)

            jsonObject.addProperty("tourId", tourId.toString())
            jsonObject.addProperty("invitedUserId", "")
            jsonObject.addProperty("isInvited", isTourPrivate)

            val callu = serviceuser.addUser(token,jsonObject)
            callu.enqueue(object : Callback<ResponseAddUserToTour> {
                override fun onFailure(call: Call<ResponseAddUserToTour>, t: Throwable) {

                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseAddUserToTour>,
                    response: Response<ResponseAddUserToTour>
                ) {
                    if (response.code() != 200) {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorResponse>() {}.type
                        var errorResponse: ErrorResponse? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        Toast.makeText(applicationContext, errorResponse!!.message, Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("addm",response.body()!!.message)
                        Toast.makeText(applicationContext, "Join Successful!!", Toast.LENGTH_LONG).show()
                        ApiRequest()
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
        countRes.text = typeCount[0].toString()
        countHotel.text = typeCount[1].toString()
        countRest.text = typeCount[2].toString()
        countOthers.text = typeCount[3].toString()
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
        commentNumField.text = listComment.size.toString() + " comments"

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
        reviewNumField.text = listReviews.size.toString() + " reviews"

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
                ApiRequestAddReview(tourId,pointrv.rating.toInt(), reviewcontent.text.toString(), ratingPopup)
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

        view.btnTourEditInfomation.setOnClickListener {
            popupWindow.dismiss()
            var editIntent = Intent(applicationContext, ChangeTourInfo::class.java)
            editIntent.putExtra("token", token)
            editIntent.putExtra("userId", tourId)
            startActivityForResult(editIntent,6969)

        }

        view.btnTourSubscribe.setOnClickListener {
            FirebaseMessaging.getInstance().subscribeToTopic("tour-id-$tourId").addOnCompleteListener { task ->
                var msg = "Subscribe successfully"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
            popupWindow.dismiss()
        }

        // Set a dismiss listener for popup window


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            tourInfoMainLayout, // Location to display popup window
            Gravity.BOTTOM, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }

    fun popupStartGoing() {
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_choose_destination, null)





        var DestinationAdapter = ChooseDestinationAdapter(listStopPoint)
        view.chooseDestinationRecycleView.adapter = DestinationAdapter
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        view.chooseDestinationRecycleView.layoutManager = layoutManager

        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
            true
        )


        popupWindow.showAsDropDown(btnStartGoingTour)




        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }



        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 6969) {
            if (resultCode == Activity.RESULT_OK) {
                ApiRequest()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
