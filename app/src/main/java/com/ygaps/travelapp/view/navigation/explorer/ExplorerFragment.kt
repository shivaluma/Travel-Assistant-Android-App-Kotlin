package com.ygaps.travelapp.view.navigation.explorer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.transition.Slide
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.taufiqrahman.reviewratings.BarLabels
import com.taufiqrahman.reviewratings.RatingReviews
import com.ygaps.travelapp.network.model.ApiServiceGetStopPointPoints
import com.ygaps.travelapp.view.stoppoint.StopPointInfo
import kotlinx.android.synthetic.main.activity_get_coordinate.*
import kotlinx.android.synthetic.main.activity_stop_point_info.*
import kotlinx.android.synthetic.main.bottomsheet.*
import kotlinx.android.synthetic.main.bottomsheet.view.*
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
    lateinit var mRecycleSuggestAdapter : StopPointAdapter

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

        if (ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

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



        root.btnShowListSuggest.visibility = View.GONE

        root.clearSuggestPointBtn.setOnClickListener {
            listSuggestPoint.clear()
            for (i in listSuggestPointMarker) i.remove()
            listSuggestPointMarker.clear()
            clearSuggestPointOnMap()
            root.btnShowListSuggest.visibility = View.GONE
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
                val key = s.toString()
                root.searchMapBarExplorer.clearSuggestions()
                if (key.isNotEmpty()) {
                    ApiRequestSearchDestination(key)
                }
            }
        })

        root.searchMapBarExplorer.setSuggestionsClickListener(object : SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemDeleteListener(position: Int, v: View?) {

            }

            override fun OnItemClickListener(position: Int, v: View?) {
                popupSuggestPointInfoFromSearch(position)
            }
        })


        mRecycleSuggestAdapter = StopPointAdapter(listStopPointSuggest)
        val layoutManager = LinearLayoutManager(context)
        root.showSuggestRecyclerView.adapter = mRecycleSuggestAdapter
        root.showSuggestRecyclerView.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        var bottomSheetBehavior = BottomSheetBehavior.from(root.bottom_sheet_layout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN


//        root.chipGroup.setOnCheckedChangeListener { chipGroup, i ->
//            Log.d("abab", i.toString())
//        }
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // React to state change
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        root.btnShowListSuggest.visibility = View.VISIBLE
                        root.clearSuggestPointBtn.show()
                        root.showSuggestPointBtn.show()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })

        root.showMap.setOnClickListener {
            root.btnShowListSuggest.visibility = View.VISIBLE
            root.clearSuggestPointBtn.show()
            root.showSuggestPointBtn.show()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }


        root.btnShowListSuggest.setOnClickListener {
            it.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            root.clearSuggestPointBtn.hide()
            root.showSuggestPointBtn.hide()

        }

        return root
    }


    fun onFilterChipClicked(v : View) {
        val chip : Chip = v as Chip
        Log.d("abab", chip.id.toString())
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
                        listStopPointSuggest.clear()
                        listStopPointSuggest.addAll(response.body()!!.stopPoints)
                        if (listStopPointSuggest.size > 0) {
                            root.btnShowListSuggest.visibility = View.VISIBLE
                        }
                        mRecycleSuggestAdapter.notifyDataSetChanged()
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

        val intent = Intent(context, StopPointInfo::class.java)
        intent.putExtra("token", token)
        intent.putExtra("stpid", listStopPointSuggest[pos].id)
        startActivity(intent)
    }

    fun popupSuggestPointInfoFromSearch(pos : Int) {
        val intent = Intent(context, StopPointInfo::class.java)
        intent.putExtra("token", token)
        intent.putExtra("stpid", listStopPointSearch[pos].id)
        startActivity(intent)
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


    inner class StopPointAdapter(data: ArrayList<StopPoint>) :
        RecyclerView.Adapter<StopPointAdapter.RecyclerViewHolder>() {

        var data = ArrayList<StopPoint>()

        init {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.stoppointsuggestview, parent, false)
            return RecyclerViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val item = data.get(position)

            try {
                holder.name.text = item.name
                holder.type.text = util.StopPointTypeToString(item.serviceTypeId!!)
                holder.cost.text = item.minCost.toString() + " - " + item.maxCost.toString()
                holder.address.text = item.address


                holder.itemView.setOnClickListener {
                    val intent = Intent(context, StopPointInfo::class.java)
                    intent.putExtra("token", token)
                    intent.putExtra("stpid", item.id)
                    startActivity(intent)
                }
            }
            catch (e : Exception) {
                e.printStackTrace()
            }

        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var name: TextView
            internal var type: TextView
            internal var cost: TextView
            internal var address: TextView

            init {
                name = itemView.findViewById(R.id.titleItem) as TextView
                address = itemView.findViewById(R.id.addressItem) as TextView
                type = itemView.findViewById(R.id.typeItem) as TextView
                cost = itemView.findViewById(R.id.costItem) as TextView
            }
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

}
