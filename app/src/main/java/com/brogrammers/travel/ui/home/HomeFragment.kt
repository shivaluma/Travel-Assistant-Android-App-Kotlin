package com.brogrammers.travel.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.brogrammers.travel.R
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.tourview.*
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    var listTours :ArrayList<tours> ?= null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)






        val root = inflater.inflate(R.layout.fragment_home, container, false)
        var tourAdapter = myTourAdapter(listTours!!, this.context!!)
        tourListView.adapter = tourAdapter
        return root
    }

    inner class myTourAdapter : BaseAdapter {

        var listTourArr = ArrayList<tours>()
        var context: Context? = null

        constructor(listTourArr: ArrayList<tours>, context: Context) : super() {
            this.listTourArr = listTourArr
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            //dua item vao
            var myView = layoutInflater.inflate(R.layout.tourview, null)
            var myTour = listTourArr[position]
            titleItem.text = myTour.name
            dateItem.text = (myTour.startDate.toString() + " - " + myTour.endDate.toString())
            peopleItem.text = (myTour.aduls.toString() + " adults" + " " + myTour.childs.toString() + " childs")
            costItem.text = (myTour.minCost.toString() + " - " + myTour.maxCost.toString())
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

    }

    inner class tours {
        var id : Int ?= null
        var status : Int ?= null
        var name : String ?= null
        var minCost : Int ?= null
        var maxCost : Int ?= null
        var startDate: Date ?= null
        var endDate: Date ?= null
        var aduls : Int ?= null
        var childs : Int ?= null
        var isPrivate: Boolean ?= null
        var avatar : String ?= null
    }
}