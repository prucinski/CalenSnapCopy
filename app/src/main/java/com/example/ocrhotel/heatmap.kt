package com.example.ocrhotel

import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import org.json.JSONException
class heatmap {
    private fun addHeatMap() {
        var latLngs: List<LatLng?>? = null

        // Get the data: latitude/longitude positions of police stations.
        try {
            latLngs = readItems(R.raw.police_stations)
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

    @Throws(JSONException::class)
    private fun readItems(@RawRes resource: Int): List<LatLng?> {
        val result: MutableList<LatLng?> = ArrayList()
        val inputStream = context.resources.openRawResource(resource)
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

}

class HeatmapTileProvider {

}