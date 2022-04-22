package com.example.ocrhotel

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

class BusinessHeatmap : Fragment() {

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        //check it out or normal operation
        val trial = false
        if (trial) {
            val sydney = LatLng(-34.0, 151.0)
            addTrialHeatMap(googleMap)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        } else {
            val aberdeen = LatLng(57.14, -2.09)
            addHeatMap((googleMap))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(aberdeen))
        }


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

    //pass by reference
    private fun addTrialHeatMap(map: GoogleMap) {
        var latLngs: List<LatLng?>? = null
        // Get the data: latitude/longitude positions of police stations in Sydney
        try {
            latLngs = readTrialItems(R.raw.police_stations)
        } catch (e: JSONException) {
            Toast.makeText(context, "Problem reading list of locations.", Toast.LENGTH_LONG)
                .show()
        }

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        val provider = HeatmapTileProvider.Builder()
            .data(latLngs)
            .build()
        // Add a tile overlay to the map, using the heat map tile provider.
        val overlay = map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
    }

    private fun addHeatMap(map: GoogleMap) {
        var latLngs: List<LatLng?>? = null
        // Get the data: latitude/longitude positions of points of event scans
        try {
            readItems { result ->
                activity?.runOnUiThread{
                    latLngs = result
                    // Create a heat map tile provider, passing it the latlngs of the scans.
                    val provider = HeatmapTileProvider.Builder()
                        .data(latLngs)
                        .build()
                    // Add a tile overlay to the map, using the heat map tile provider.
                    val overlay = map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
                }
            }
        } catch (e: JSONException) {
            Toast.makeText(context, "Problem reading list of locations.", Toast.LENGTH_LONG)
                .show()
        }
    }

    @Throws(JSONException::class)
    private fun readTrialItems(@RawRes resource: Int): List<LatLng?> {
        val result: MutableList<LatLng?> = ArrayList()
        val inputStream = requireContext().resources.openRawResource(resource)
        val json = Scanner(inputStream).useDelimiter("\\A").next()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val `object` = array.getJSONObject(i)
            val lat = `object`.getDouble("lat")
            val lng = `object`.getDouble("lng")
            result.add(LatLng(lat, lng))
        }
        return result
    }

    @Throws(JSONException::class)
    private fun readItems(callback: (List<LatLng>) -> Unit) {

        val m = activity as MainActivity

        //get the data, then go through the data and create a list of LatLangs
        if (m.loggedIn) {
            val result: MutableList<LatLng> = ArrayList()
            //TODO: wait for values being returned instead of hanging the app
            activity?.runOnUiThread {
                Log.d("jwt", "jwt found.")
                var found: Boolean = false;
                readEvents(m.jwt) { apiEvents ->
                    for (event in apiEvents!!.events) {
                        found = true;
                        Log.d("heatmap::event", event.snap_location.toString())
                        val lat = event.snap_location.N
                        val lng = -event.snap_location.W
                        result.add(LatLng(lat, lng))
                    }
                    callback(result)
                }
            }
        } else {

            Toast.makeText(
                context, "Error retreiving events. Please check if you're logged in",
                Toast.LENGTH_SHORT
            ).show()
            //ensuring there's at least one result so as not to crash the programme
        }
    }
}