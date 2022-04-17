package com.example.ocrhotel

import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.preference.*
import org.joda.time.DateTime


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

    private fun populateCalendarList() {
        //these have to be strings sadly
        val size = 10
        var calendarNames = Array<String?>(size) { "null" }
        var calendarIDs = Array<String?>(size) { "null" }

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
        val calendarListPreference: ListPreference? = findPreference("calendarID")
        calendarListPreference!!.entries = calendarNames
        calendarListPreference.entryValues = calendarIDs


        cur.close()
    }

    override fun onResume() {
        super.onResume()
        checkAndUpdate()

    }
    //then, upon clicking the list and choosing a value, a sharedPreference "calendarID" will get
    //updated with the desired calendar ID.

    //Settings on the premium && business accounts.
    private fun checkAndUpdate() {
        val premiumPreference: Preference? = findPreference("premium")
        val businessPreference: Preference? = findPreference("business")
        val sh = requireActivity().getSharedPreferences(
            getString(R.string.preferences_address),
            AppCompatActivity.MODE_PRIVATE
        )

        val prem = sh.getBoolean("isPremiumUser", false)
        val bus = sh.getBoolean("isBusinessUser", false)
        val pExpDay = sh.getInt("premiumExpirationDay)", -1)
        val pExpMth = sh.getInt("premiumExpirationMonth)", -1)
        val bExpDay = sh.getInt("businessExpirationDay)", -1)
        val bExpMth = sh.getInt("businessExpirationMonth)", -1)
        if (prem) {
            premiumPreference!!.title = "You are a Premium user"
            //TODO: build a string
            val pStringSummary =
                "Your subscription will expire on $pExpDay.$pExpMth. Press to cancel"
            premiumPreference.summary = pStringSummary
            premiumPreference.isSelectable = false
        }
        //if user is a business one, that will override the premium account anyway!
        if (bus) {
            premiumPreference!!.title = "You are a Business user"
            val bStringSummary =
                "Your subscription will expire on $bExpDay.$bExpMth. Press to cancel"
            businessPreference!!.title = "Business features"
            businessPreference!!.summary = "Press to inspect"
            businessPreference!!.setFragment("com.example.ocrhotel.BusinessHeatmap")
        }
        //check whether the subscriptions have expired. Right now, it doesn't automatically extend
        //due to the technicalities with the payment provider, but it is easily amendable.
        if (pExpDay == DateTime.now().dayOfMonth) {
            if (pExpMth == DateTime.now().monthOfYear) {
                removePremium()
                refreshFragment()
            }
        }
        if (bExpDay == DateTime.now().dayOfMonth) {
            if (bExpMth == DateTime.now().monthOfYear) {
                removeBusiness()
                refreshFragment()
            }
        }
    }
    private fun removePremium() {
        val sh = requireActivity().getSharedPreferences(
            getString(R.string.preferences_address),
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sh.edit()
        editor.putBoolean("isPremiumUser", false)
        editor.putInt("premiumExpirationDay)", -1)
        editor.putInt("premiumExpirationMonth)", -1)
        //no need to update the expiration dates as they will not be read anyway
        editor.commit()

    }

    private fun removeBusiness() {
        val sh = requireActivity().getSharedPreferences(
            getString(R.string.preferences_address),
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sh.edit()
        editor.putBoolean("isBusinessUser", false)
        editor.putBoolean("isPremiumUser", false)
        //cautinary update of the other values.
        editor.putInt("premiumExpirationDay)", -1)
        editor.putInt("premiumExpirationMonth)", -1)
        editor.putInt("businessExpirationDay)", -1)
        editor.putInt("businessExpirationMonth)", -1)

        editor.commit()
    }

    //refresh by renavigating to this fragment
    private fun refreshFragment(){
        val navController: NavController =
            requireActivity().findNavController(R.id.settingsMenu)
        navController.run {
            popBackStack()
            navigate(R.id.settingsMenu)
        }
    }
}
