package com.brogrammers.travel.view.navigation.explorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.brogrammers.travel.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_explorer.view.*


class ExplorerFragment : Fragment() {

    lateinit var root : View
    lateinit var mGoogleMap: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        root = inflater.inflate(R.layout.fragment_explorer, container, false)

        root.explorermap.onCreate(savedInstanceState)
        root.explorermap.getMapAsync {
            mGoogleMap = it
            root.explorermap.onResume()
        }

        return root
    }

}
