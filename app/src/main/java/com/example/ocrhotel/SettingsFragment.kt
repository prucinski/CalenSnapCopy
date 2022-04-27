package com.example.ocrhotel

import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.joda.time.DateTime
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

class SettingsFragment : PreferenceFragmentCompat() {

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
        val eventProjection: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT            // 3
        )

        // The indices for the projection array above.
        val projectionIDIndex = 0
        // val projectionAccountNameIndex = 1
        val projectionDisplayNameIndex = 2
        // val projectionOwnerAccountIndex = 3
        val uri = CalendarContract.Calendars.CONTENT_URI
        val cur = requireContext().contentResolver.query(uri, eventProjection, "", null, null)
        //return first n calendars (if it even gets to this point)
        var counter = 0
        while (cur!!.moveToNext() && counter < size) {
            // Get the field values
            val calID: Long = cur.getLong(projectionIDIndex)
            val displayName: String = cur.getString(projectionDisplayNameIndex)
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
        val a = requireActivity() as MainActivity

        val pExpDay = sh.getInt("premiumExpirationDay", -1)
        val pExpMth = sh.getInt("premiumExpirationMonth", -1)
        val bExpDay = sh.getInt("businessExpirationDay", -1)
        val bExpMth = sh.getInt("businessExpirationMonth", -1)
        if (a.premiumAccount) {
            premiumPreference!!.title = "You are a Premium user"

            val pStringSummary =
                if (pExpDay == -1 || pExpMth == -1) "You have lifetime access to CalenSnap."
                else {
                    val endDate = LocalDate.of(now().year,pExpMth,pExpDay).format(DateTimeFormatter.ofPattern("dd MMMM"))

                    "Your subscription will expire on $endDate. \nPress to cancel."
                }
            premiumPreference.summary = pStringSummary
            premiumPreference.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Cancelling premium")
                    .setMessage("Are you sure you want to cancel your subscription? You will" +
                            "lose any remaining days of premium you might have.")
                    .setPositiveButton("OK"){_,_->
                        //sadly this coupling is required to minimize opening sharedPrefs
                        (activity as MainActivity).premiumAccount = false
                        removePremium()
                    }
                    .setNegativeButton("Take me back"){_,_->
                    }.show()
                true
            }
        }
        //if user is a business one, that will override the premium account anyway!
        if (a.businessAccount) {
            premiumPreference!!.title = "You are a Business user"

            val bStringSummary =
                if (bExpDay == -1 || bExpMth == -1) "You have lifetime access to CalenSnap."
                else {
                    val endDate = LocalDate.of(now().year,bExpMth,bExpDay).format(DateTimeFormatter.ofPattern("dd MMMM"))

                    "Your subscription will expire on $endDate. \nPress to cancel."
                }

            premiumPreference.summary = bStringSummary
            premiumPreference.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Cancelling business")
                    .setMessage("Are you sure you want to cancel your subscription? You will " +
                            "lose any remaining days of business access you might have.")
                    .setPositiveButton("OK"){_,_->
                        removeBusiness()
                    }
                    .setNegativeButton("Take me back"){_,_->
                    }.show()
                true
            }
            premiumPreference.summary = bStringSummary
            businessPreference!!.title = "Business features"
            businessPreference.summary = "Press to inspect"
            businessPreference.fragment = "com.example.ocrhotel.BusinessHeatmap"
        }

        val login : Preference? = findPreference("login")
        val logout : Preference? = findPreference("logout")
        if(a.loggedIn){
            login?.isVisible = false
            logout?.isVisible = true
        }
        else{
            login?.isVisible = true
            logout?.isVisible = false
        }

        // Once you implement it, better set it inside the onCreatePref
        // instead of letting it sit here
        logout?.setOnPreferenceClickListener {
            // TODO ("Implement Log out"), if this behaviour is not enough.
            (activity as MainActivity).logOut()
            refreshFragment()
            true
        }
        //check whether the subscriptions have expired. Right now, it doesn't automatically extend
        //due to the technicalities with the payment provider, but it is easily amendable.
        if (pExpDay == DateTime.now().dayOfMonth) {
            if (pExpMth == DateTime.now().monthOfYear) {
                removePremium()
            }
        }
        if (bExpDay == DateTime.now().dayOfMonth) {
            if (bExpMth == DateTime.now().monthOfYear) {
                removeBusiness()
            }
        }
    }
    private fun removePremium() {
        val sh = requireActivity().getSharedPreferences(
            getString(R.string.preferences_address),
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sh.edit()
        val m = (activity as MainActivity)
        m.premiumAccount = false
        editor.putInt("premiumExpirationDay", -1)
        editor.putInt("premiumExpirationMonth", -1)
        //no need to update the expiration dates as they will not be read anyway
        editor.apply()
        refreshFragment()

    }

    private fun removeBusiness() {
        val sh = requireActivity().getSharedPreferences(
            getString(R.string.preferences_address),
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sh.edit()
        val m = (activity as MainActivity)
        m.businessAccount = false
        m.premiumAccount = false
        //cautionary update of the other values.
        editor.putInt("premiumExpirationDay)", -1)
        editor.putInt("premiumExpirationMonth)", -1)
        editor.putInt("businessExpirationDay)", -1)
        editor.putInt("businessExpirationMonth)", -1)

        editor.apply()
        refreshFragment()
    }

    //refresh by re-navigating to this fragment
    private fun refreshFragment(){
        val navController: NavController =
            NavHostFragment.findNavController(this)
        navController.run {
            popBackStack()
            navigate(R.id.settingsMenu)
        }
    }
}
