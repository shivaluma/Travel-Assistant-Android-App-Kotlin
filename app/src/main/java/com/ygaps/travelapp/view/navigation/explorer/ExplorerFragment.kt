package com.ygaps.travelapp.view.navigation.explorer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ygaps.travelapp.*
import com.ygaps.travelapp.manager.doAsync
import com.ygaps.travelapp.model.StopPoint
import com.ygaps.travelapp.network.model.ApiServiceSearchDestination
import com.ygaps.travelapp.network.model.ApiServiceSuggestDestination
import com.ygaps.travelapp.network.model.WebAccess
import com.ygaps.travelapp.util.util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.taufiqrahman.reviewratings.BarLabels
import com.taufiqrahman.reviewratings.RatingReviews
import com.ygaps.travelapp.network.model.ApiServiceGetStopPointPoints
import kotlinx.android.synthetic.main.activity_get_coordinate.*
import kotlinx.android.synthetic.main.activity_stop_point_info.*
import kotlinx.android.synthetic.main.fragment_explorer.*
import kotlinx.android.synthetic.main.fragment_explorer.view.*
import kotlinx.android.synthetic.main.popup_stoppoint_suggest.view.*
import kotlinx.android.synthetic.main.stoppoint.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class ExplorerFragment : Fragment() {

    lateinit var root : View
    lateinit var mGoogleMap: GoogleMap
    var listStopPointSuggest = ArrayList<StopPoint>()
    var listStopPointSuggestMarker = ArrayList<Marker>()

    var listSuggestPoint = ArrayList<LatLng>()
    var listSuggestPointMarker = ArrayList<Marker>()
    var listStopPointSearch = ArrayList<StopPoint>()
    var suggestionsList = ArrayList<String>()

    val colors = intArrayOf(
        Color.parseColor("#0e9d58"),
        Color.parseColor("#bfd047"),
        Color.parseColor("#ffc105"),
        Color.parseColor("#ef7e14"),
        Color.parseColor("#d36259")
    )

    var token : String  = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        root = inflater.inflate(R.layout.fragment_explorer, container, false)

        token = activity!!.intent.extras!!.getString("userToken", "notoken")
        root.explorermap.onCreate(savedInstanceState)
        root.explorermap.getMapAsync {
            mGoogleMap = it
            root.explorermap.onResume()

            mGoogleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(10.762913, 106.6821717),
                    15.0f
                )
            )

            mGoogleMap.setOnMapClickListener {
                var marker = mGoogleMap.addMarker(MarkerOptions().position(it).title((listSuggestPoint.size + 1).toString()))
                listSuggestPoint.add(it)
                marker.tag = "suggestpoint"
                listSuggestPointMarker.add(marker)
            }

            mGoogleMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {

                override fun onMarkerClick(p0: Marker?): Boolean {
                    if (p0!!.tag.toString().contains("suggeststoppoint")) {
                        var splitList = p0.tag.toString().split(" ")
                        var serviceId : String = splitList[1]
                        Log.d("abab",serviceId)
                        popupSuggestPointInfo(serviceId.toInt())
                    }
                    return false
                }
            })
        }


        root.clearSuggestPointBtn.setOnClickListener {
            listSuggestPoint.clear()
            for (i in listSuggestPointMarker) i.remove()
            listSuggestPointMarker.clear()
            clearSuggestPointOnMap()
        }

        root.showSuggestPointBtn.setOnClickListener {
            if (listSuggestPoint.size == 0) {
                Toast.makeText(
                    context,
                    "No Suggest Point on the map",
                    Toast.LENGTH_LONG
                ).show()
            }
            else if (listSuggestPoint.size == 1) {
                var body = JsonObject()
                var coor = JsonObject()
                coor.addProperty("lat", listSuggestPoint[0].latitude)
                coor.addProperty("long", listSuggestPoint[0].longitude)
                body.addProperty("hasOneCoordinate", true)
                body.add("coordList", coor)
                ApiRequestGetNearbyPoint(body)
            }

            else if (listSuggestPoint.size > 1) {
                if (listSuggestPoint.size % 2 == 1) {
                    Toast.makeText(context, "Số điểm chấm phải là số chẵn", Toast.LENGTH_LONG).show()
                }
                else {
                    var body = JsonObject()
                    var coorlist = JsonArray()
                    var array = JsonArray()

                    for (i in 0..listSuggestPoint.size-1) {
                        var coor = JsonObject()
                        coor.addProperty("lat", listSuggestPoint[i].latitude)
                        coor.addProperty("long", listSuggestPoint[i].longitude)
                        array.add(coor)
                        if (i > 0 && i % 2 == 1) {
                            var coorListObject = JsonObject()
                            coorListObject.add("coordinateSet", array)
                            array = JsonArray()
                            coorlist.add(coorListObject)
                        }
                    }
                    body.addProperty("hasOneCoordinate", false)
                    body.add("coordList", coorlist)
                    ApiRequestGetNearbyPoint(body)
                }
            }
        }


        root.searchMapBarExplorer.setOnSearchActionListener(
            object : MaterialSearchBar.OnSearchActionListener {
                override fun onButtonClicked(buttonCode: Int) {
                    if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                        //opening or closing a navigation drawer
                    } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                        root.searchMapBarExplorer.disableSearch()
                    }
                    root.searchMapBarExplorer.clearSuggestions()
                }

                override fun onSearchStateChanged(enabled: Boolean) {

                }

                override fun onSearchConfirmed(text: CharSequence?) {
                    searchLocation(text.toString())
                    root.searchMapBarExplorer.clearSuggestions()
//                    ApiRequestSearchDestination(text.toString())

                }
            }
        )


        root.searchMapBarExplorer.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ApiRequestSearchDestination(s.toString())
            }
        })

        root.searchMapBarExplorer.setSuggestionsClickListener(object : SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemDeleteListener(position: Int, v: View?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun OnItemClickListener(position: Int, v: View?) {
                popupSuggestPointInfoFromSearch(position)
            }
        })


        return root
    }



    fun ApiRequestGetNearbyPoint(body : JsonObject) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceSuggestDestination::class.java)

            val call = service.getSuggest(token,body)
            call.enqueue(object : Callback<ResponseSuggestDestination> {
                override fun onFailure(call: Call<ResponseSuggestDestination>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponseSuggestDestination>,
                    response: Response<ResponseSuggestDestination>
                ) {
                    Log.d("abab",response.message())
                    if (response.code() != 200) {
                        Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("abab",response.body().toString())
                        listStopPointSuggest.addAll(response.body()!!.stopPoints)
                        showSuggestPointToMap()
                    }
                }
            })
        }.execute()
    }

    fun showSuggestPointToMap() {
        clearSuggestPointOnMap()
        var item : StopPoint
        for (i in 0..listStopPointSuggest.size-1) {
            item = listStopPointSuggest[i]
            var marker : Marker
            var latLng = LatLng(item.lat!!,item.long!!)
            if (item.serviceTypeId == 1) {
                marker = addMarker(mGoogleMap, latLng, item.name, R.drawable.ic_restaurant)
            }
            else if (item.serviceTypeId == 2) {
                marker = addMarker(mGoogleMap, latLng, item.name, R.drawable.ic_hotel)
            }
            else if (item.serviceTypeId == 3) {
                marker = addMarker(mGoogleMap, latLng, item.name, R.drawable.ic_bedtime)
            }
            else {
                marker = addMarker(mGoogleMap, latLng, item.name, R.drawable.ic_pin)
            }
            marker.tag = "suggeststoppoint ${i}"
            listStopPointSuggestMarker.add(marker)
        }
    }

    fun clearSuggestPointOnMap() {
        for (i in listStopPointSuggestMarker) i.remove()
        listStopPointSuggestMarker.clear()
    }

    fun popupSuggestPointInfo(pos : Int) {
        val inflater: LayoutInflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_stoppoint_suggest, null)

        view.addSuggestToTour.visibility = View.GONE
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
            true
        )

        ApiRequestGetPoints(view, listStopPointSuggest[pos].id)

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


        }


        view.stpSuggestName.text = listStopPointSuggest[pos].name
        view.stpSuggestAddress.text = listStopPointSuggest[pos].address
        view.stpSuggestContact.text = listStopPointSuggest[pos].contact
        var costString = listStopPointSuggest[pos].minCost.toString() + " - " + listStopPointSuggest[pos].maxCost.toString()
        view.stpSuggestCost.text = costString
        view.serviceSuggestTypeText.text = util.StopPointTypeToString(listStopPointSuggest[pos].serviceTypeId!!)

        


        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {



        }


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            explorer_rootview, // Location to display popup window
            Gravity.BOTTOM, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }

    fun popupSuggestPointInfoFromSearch(pos : Int) {
        val inflater: LayoutInflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_stoppoint_suggest, null)

        view.addSuggestToTour.visibility = View.GONE
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Window height
            true
        )

        ApiRequestGetPoints(view, listStopPointSearch[pos].id)

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


        }


        view.stpSuggestName.text = listStopPointSearch[pos].name
        view.stpSuggestAddress.text = listStopPointSearch[pos].address
        view.stpSuggestContact.text = listStopPointSearch[pos].contact
        var costString = listStopPointSearch[pos].minCost.toString() + " - " + listStopPointSearch[pos].maxCost.toString()
        view.stpSuggestCost.text = costString
        view.serviceSuggestTypeText.text = util.StopPointTypeToString(listStopPointSearch[pos].serviceTypeId!!)




        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {



        }


        // Finally, show the popup window on app
        popupWindow.showAtLocation(
            explorer_rootview, // Location to display popup window
            Gravity.BOTTOM, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }


    fun addMarker(ggMap: GoogleMap, pos: LatLng, name: String, drawable: Int): Marker {
        return ggMap.addMarker(
            MarkerOptions()
                .position(pos)
                .icon(bitmapDescriptorFromVector(context!!, drawable))
                .title(name)
        )
    }


    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun searchLocation(location: String) {
        var addressList: List<Address>? = null

        if (location == null || location == "") {
            Toast.makeText(context, "provide location", Toast.LENGTH_SHORT).show()
        } else {
            val geoCoder = Geocoder(context)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (addressList == null || addressList.isEmpty()) {
                Toast.makeText(context, "Khong tim thay dia diem nay", Toast.LENGTH_LONG)
                    .show()
            } else {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mGoogleMap.addMarker(MarkerOptions().position(latLng).title(location))
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f))
                Toast.makeText(
                    context,
                    address.latitude.toString() + " " + address.longitude,
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }


    fun ApiRequestSearchDestination(searchKey : String) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceSearchDestination::class.java)
            val call = service.search(token,searchKey,1,"9999")
            call.enqueue(object : Callback<ResponseSearchDestination> {
                override fun onFailure(call: Call<ResponseSearchDestination>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }
                override fun onResponse(
                    call: Call<ResponseSearchDestination>,
                    response: Response<ResponseSearchDestination>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("abab",response.body().toString())
                        listStopPointSearch.clear()
                        listStopPointSearch.addAll(response.body()!!.stopPoints)
                        suggestionsList.clear()
                        for (i in listStopPointSearch.indices) {
                            var line : String = "id : " + listStopPointSearch[i].id + " - " + listStopPointSearch[i].name + "\n" +
                                    "Address : "  + listStopPointSearch[i].address
                            suggestionsList.add(line)
                        }

                        root.searchMapBarExplorer.updateLastSuggestions(suggestionsList)
                        if (!root.searchMapBarExplorer.isSuggestionsVisible) {
                            root.searchMapBarExplorer.showSuggestionsList()
                        }

                    }
                }
            })
        }.execute()
    }


    fun ApiRequestGetPoints(root : View, serviceId : Int) {
        doAsync {
            val service = WebAccess.retrofit.create(ApiServiceGetStopPointPoints::class.java)
            val call = service.getPoints(token,serviceId)
            call.enqueue(object : Callback<ResponseStopPointRatingPoints> {
                override fun onFailure(call: Call<ResponseStopPointRatingPoints>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseStopPointRatingPoints>,
                    response: Response<ResponseStopPointRatingPoints>
                ) {
                    if (response.code() != 200) {
                        Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
                    } else {
                        val ratingReviews = root.findViewById<RatingReviews>(R.id.rating_reviews)

                        val raters = intArrayOf(
                            response.body()!!.pointStats[0].total,
                            response.body()!!.pointStats[1].total,
                            response.body()!!.pointStats[2].total,
                            response.body()!!.pointStats[3].total,
                            response.body()!!.pointStats[4].total
                        )
                        var average = "%.1f".format(raters.average())
                        root.ratingAveragePoint.text = average.toString()
                        var maxValue = raters.max()
                        var sum = raters.sum()
                        root.textView2.text = sum.toString()
                        root.ratingBar.rating = average.toFloat()

                        ratingReviews.createRatingBars(maxValue!!, BarLabels.STYPE1, colors, raters)
                    }
                }
            })
        }.execute()
    }

}
