package com.example.ocrhotel

import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*



class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        //different settings can be setup at root_preferences.
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        //set the "calendar"
        populateCalendarList()
        checkAndUpdate()
    }

    //TODO: have it not be called every time. Add a preference button "Update calendar list"?

    private fun populateCalendarList(){
        //these have to be strings sadly
        val size = 10
        var calendarNames = Array<String?>(size){"null"}
        var calendarIDs =Array<String?>(size){"null"}

        // Projection array. Creating indices for this array instead of doing
        // dynamic lookups improves performance.
        val EVENT_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT            // 3
        )
// The indices for the projection array above.
        val PROJECTION_ID_INDEX = 0
        val PROJECTION_ACCOUNT_NAME_INDEX = 1
        val PROJECTION_DISPLAY_NAME_INDEX = 2
        val PROJECTION_OWNER_ACCOUNT_INDEX = 3
        val uri = CalendarContract.Calendars.CONTENT_URI
        val cur = requireContext().contentResolver.query(uri, EVENT_PROJECTION, "", null, null)
        //return first n calendars (if it even gets to this point)
        var counter = 0
        while (cur!!.moveToNext() && counter < size) {
            // Get the field values
            val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
            // Add the relevant values to relevant arrays.
            Log.e("CALENDAR", calID.toString())
            calendarNames[counter] = displayName
            calendarIDs[counter] = calID.toString()
            counter++
        }
        calendarNames = calendarNames.copyOf(counter)
        calendarIDs = calendarIDs.copyOf(counter)
        val calendarListPreference : ListPreference? = findPreference("calendarID")
        calendarListPreference!!.entries = calendarNames
        calendarListPreference.entryValues = calendarIDs


        cur.close()
    }
    override fun onResume(){
        super.onResume()
        checkAndUpdate()

    }
    //then, upon clicking the list and choosing a value, a sharedPreference "calendarID" will get
    //updated with the desired calendar ID.

    private fun checkAndUpdate(){
        val premiumPreference : Preference? = findPreference("premium")
        val businessPreference : Preference? = findPreference("business")
        val sh = activity?.getSharedPreferences(getString(R.string.preferences_address),
            AppCompatActivity.MODE_PRIVATE)

        val prem = sh!!.getBoolean("isPremiumUser", false)
        val bus = sh!!.getBoolean("isBusinessUser", false)
        if(prem){
            premiumPreference!!.title = "You are a Premium user"
            //TODO: build a string
            premiumPreference!!.summary = "Your subscription expires on xxx"
            premiumPreference.isSelectable = false
        }
        if(bus){
            businessPreference!!.title = "Business features"
            businessPreference!!.summary = "Press to inspect"
            businessPreference.setFragment("com.example.ocrhotel.BusinessHeatmap")
        }

    }


}