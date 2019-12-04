package com.brogrammers.travel.view.navigation.dashboard

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brogrammers.travel.*
import com.brogrammers.travel.manager.doAsync
import com.brogrammers.travel.network.model.ApiServiceGetNotifications
import com.brogrammers.travel.network.model.ApiServiceGetTourInfo
import com.brogrammers.travel.network.model.ApiServiceResponseInvitaion
import com.brogrammers.travel.network.model.WebAccess
import com.brogrammers.travel.util.util
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.item_notification_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {
    lateinit var view : RecyclerView
    lateinit var notifiAdapter : NotificationAdapter
    var listNotification = ArrayList<TourNotification>()
    var token : String = ""
    lateinit var root : View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        token = activity!!.intent.extras!!.getString("userToken","notoken")
        Log.d("addm",token)
        view = root.findViewById(R.id.notificationOfUser)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        view.layoutManager = layoutManager
        notifiAdapter = NotificationAdapter(listNotification)
        view.adapter = notifiAdapter
        ApiRequest(root)
        return root
    }

    inner class NotificationAdapter(data: ArrayList<TourNotification>) :
        RecyclerView.Adapter<NotificationAdapter.RecyclerViewHolder>() {

        var data = ArrayList<TourNotification>()

        init {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_notification_layout, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val item = data.get(position)
            holder.content.setText(Html.fromHtml("<b>${item.hostName}</b> has invited you to join tour <b>${item.name}</b>"))
            holder.time.setText(util.longToDate(item.createOn))


            holder.itemView.notiAcceptButton.setOnClickListener {
                ApiRequestResponseInvitation(root,item.id,true)
            }

            holder.itemView.notiDeclineButton.setOnClickListener {
                ApiRequestResponseInvitation(root,item.id,false)
            }


        }

        override fun getItemCount(): Int {
            return data.size
        }


        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var content: TextView
            internal var time: TextView

            init {
                content = itemView.findViewById(R.id.InviteContent) as TextView
                time = itemView.findViewById(R.id.InviteTime) as TextView
            }
        }
    }


    fun ApiRequest(root : View) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetNotifications::class.java)
            val call = service.getNotif(token,1,"999")
            call.enqueue(object : Callback<ResponseUserNotification> {
                override fun onFailure(call: Call<ResponseUserNotification>, t: Throwable) {
                    Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseUserNotification>,
                    response: Response<ResponseUserNotification>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(activity, response.message(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("members",response.body()!!.toString())
                        listNotification.clear()
                        listNotification.addAll(response.body()!!.tours)
                        notifiAdapter.notifyDataSetChanged()
                        root.findViewById<TextView>(R.id.notificationCount).setText(listNotification.size.toString())
                    }
                }
            })
        }.execute()
    }


    fun ApiRequestResponseInvitation(root : View, tourId: Int, isAccept: Boolean) {
            val service = WebAccess.retrofit.create(ApiServiceResponseInvitaion::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("tourId", tourId.toString())
            jsonObject.addProperty("isAccepted", isAccept)
            val call = service.response(token,jsonObject)

            call.enqueue(object : Callback<ResponseToInvitation> {
                override fun onFailure(call: Call<ResponseToInvitation>, t: Throwable) {
                    Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseToInvitation>,
                    response: Response<ResponseToInvitation>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(activity, response.raw().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "Phản hồi thành công!", Toast.LENGTH_LONG).show()
                        ApiRequest(root)
                    }
                }
            })
        }
    }
