package com.example.ocrhotel

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import org.json.JSONException
import kotlin.collections.ArrayList

class BusinessHeatmap : Fragment() {

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         *
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        /** Default location to pan the map to */
        val aberdeen = LatLng(57.14, -2.09)
        addHeatMap((googleMap))

        // Pan the camera to the default location and zoom in (12 is around town zoom)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aberdeen,11f))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_business_heatmap, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Toast.makeText(context, "Please wait. The map is loading...", Toast.LENGTH_SHORT).show()
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun addHeatMap(map: GoogleMap) {
        // Get the data: latitude/longitude positions of points of event scans
        try {
            readItems { coordinates ->
                activity?.runOnUiThread{
                    // Create a heat map tile provider, passing it the latLongs of the scans.
                    val provider = HeatmapTileProvider.Builder()
                        .data(coordinates)
                        .build()
                    // Add a tile overlay to the map, using the heat map tile provider.
                    // val overlay =
                    map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
                }
            }
        } catch (e: JSONException) {
            Toast.makeText(context, "Problem reading list of locations.", Toast.LENGTH_LONG)
                .show()
        }
    }

    @Throws(JSONException::class)
    private fun readItems(callback: (List<LatLng>) -> Unit) {

        val m = activity as MainActivity

        // Get the data, then go through it and create a list of Latitudes and Longitudes
        if (m.loggedIn) {
            val result: MutableList<LatLng> = ArrayList()

            m.runOnUiThread {
                Log.d("jwt", "jwt found.")
                readEvents(m.jwt) { apiEvents ->

                    Log.d("HeatMap",apiEvents?.events.toString())
                    // Check if events have actually been returned.
                    if(apiEvents!=null){
                        for (event in apiEvents.events) {
                            //another post-release fix. Lack of communication between two sections
                            //of the team led to invalid locations being recorded as (-1000,-1000)
                            //Map person (me) thought they wouldn't be recorded at all (good design)
                            //so this is quite literally a bandaid fix.
                            if(event.snap_location.N == -1000.0 || event.snap_location.W == -1000.0){
                                Log.e("InvPt", "Attempted drawing an invalid point")
                                continue
                            }
                            val lat = event.snap_location.N
                            val lng = event.snap_location.W
                            result.add(LatLng(lat, lng))
                        }
                        callback(result)
                    }
                    else{
                        Toast.makeText(context,"An error occurred. Probably the DB thinks you're not a Business user. Restart App and try again.",Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                context, "Error retrieving events. Please check if you're logged in",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}