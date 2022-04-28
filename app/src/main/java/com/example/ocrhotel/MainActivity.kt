package com.example.ocrhotel

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ocrhotel.databinding.ActivityMainBinding
import com.example.ocrhotel.models.Event
import com.example.ocrhotel.models.EventListModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    private val adRequest = AdRequest.Builder().build()
    private var mRewardedAd: RewardedAd? = null
    private var logTag = "MainActivity"


    private var _premiumAccount = false
    private var _businessAccount = false
    private var _scans = 1
    private var _jwt = ""
    private var _name: String? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // All of these properties below are automatically synced with the shared preferences.
    val loggedIn: Boolean
        get() = jwt.isNotEmpty()

    var premiumAccount: Boolean
        get() {
            val sh = getSharedPreferences(getString(R.string.preferences_address), MODE_PRIVATE)
            return sh.getBoolean("isPremiumUser", false)
        }
        set(value) {
            _premiumAccount = value
            val edit = getEdit()
            edit.putBoolean("isPremiumUser", value)
            edit.apply()
        }

    var businessAccount: Boolean
        get() {
            val sh = getSharedPreferences(getString(R.string.preferences_address), MODE_PRIVATE)
            return sh.getBoolean("isBusinessUser", false)
        }
        set(value) {
            val edit = getEdit()
            edit.putBoolean("isBusinessUser", value)
            edit.apply()
        }

    var scans: Int
        get() {
            val sh = getSharedPreferences(getString(R.string.preferences_address), MODE_PRIVATE)
            return sh.getInt("numberOfScans", 0)
        }
        set(value) {
            val edit = getEdit()
            edit.putInt("numberOfScans", value)
            edit.apply()
        }

    var jwt: String
        get() {
            val sh = getSharedPreferences(getString(R.string.preferences_address), MODE_PRIVATE)
            return sh.getString("JWT", "")!!
        }
        set(value) {
            val edit = getEdit()
            edit.putString("JWT", value)
            edit.apply()
        }

    var name: String?
        get() = _name
        set(value) {
            _name = value
        }

    var currentLoc: Location? = null

    private fun getEdit(): SharedPreferences.Editor {
        val sh = getSharedPreferences(getString(R.string.preferences_address), MODE_PRIVATE)
        return sh.edit()!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // First check if the necessary permissions have been granted.
        // The below function also initializes sharedPrefs.
        checkPermissions(
            listOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR, Manifest.permission.ACCESS_FINE_LOCATION
            ),
            "We will need access to your calendar in order to add the events you have scanned." +
                    "\n\nWe also require permission to use your location in order to locate your picture " +
                    "in accordance with our Terms and Conditions."
        ) {
            setupSharedPrefs()
        }

        // create an instance of the Fused Location Provider Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Retrieve values that we want.
        retrieveAppSettings()
        initializeAds()
        setupNavigation()
        reloadEvents()
        retrieveLoc()
    }

    private fun retrieveLoc() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLoc = location
                }
            }
    }

    private fun initializeAds() {
        if (!(premiumAccount || businessAccount)) {
            //Code for ads
            MobileAds.initialize(this) {}

            //Banner ad
            val mAdView = findViewById<AdView>(R.id.adView)
            mAdView.loadAd(adRequest)
            mAdView.visibility = VISIBLE

            //Load reward ad
            loadRewardedAd()
        }
    }

    override fun onPause() {
        super.onPause()
        removeAds()
    }

    override fun onResume() {
        super.onResume()
        initializeAds()
    }

    fun updateAds() {
        if (premiumAccount || businessAccount) {
            removeAds()
        } else {
            removeAds()
        }
    }

    fun removeAds() {
        val mAdView = findViewById<AdView>(R.id.adView)
        mAdView.visibility = GONE
    }

    fun synchronizeChanges() {
        // Send changes made to the preferences to the
    }

    fun logOut() {
        synchronizeChanges()
        jwt = "" // this means user is logged out
        premiumAccount = false
        businessAccount = false
        resetEvents()
    }

    fun reloadEvents() {
        Log.d(logTag, "Reloading events.")
        getJwtFromPreferences(this)?.let { jwt ->
            readUserEvents(jwt) { userEvents ->
                val events = mutableListOf<Event>()

                if (userEvents != null) {
                    for (event in userEvents.events) {
                        events.add(Event(event.title, extractDate(event.event_time), id = event.id))
                    }
                } else {
                    navController.navigate(R.id.loginFragment)
                }
                runOnUiThread {
                    this.viewModels<EventListModel>().value.eventsList = events
                    for (event in events) {
                        Log.d("OCR EVENT", event.eventName)
                    }
                    Log.d(logTag, "Reloaded Events")
                }
            }
            readProfile(jwt) { profile ->
                if (profile != null) {
                    // Update shared preferences from profile
                    businessAccount = profile.business_user
                    premiumAccount = profile.premium_user
                    scans = profile.remaining_free_uses
                    name = profile.username
                } else {
                    // In this case, the server likely responded with a 401 because the JWT timed out. Hence the user should be logged out.
                    logOut()
                }
            }
        } ?: navController.navigate(R.id.loginFragment)

    }

    //function to be called after logging out to clear the tables.
    private fun resetEvents() {
        this.viewModels<EventListModel>().value.eventsList = emptyList()
    }


    private fun setupSharedPrefs() {
        // Storing data into SharedPreferences
        // Initialization on first app launch.
        // This file is present only on the device and not in this project.

        val sh = getSharedPreferences(getString(R.string.preferences_address), MODE_PRIVATE)

        // Check if file already present. if not, create it
        premiumAccount = sh.getBoolean("isPremiumUser", false)
        businessAccount = sh.getBoolean("isBusinessAccount", false)
        jwt = sh.getString("JWT", "")!!
        scans = sh.getInt("numberOfScans", 1)

        if (!sh.contains("isPremiumUser")) {
            val myEdit = sh.edit()

            // VALUES INITIALIZED DURING LAUNCH.
            myEdit.putBoolean("isPremiumUser", false)
            myEdit.putInt("numberOfScans", 1)
            myEdit.putString("calendarID", getCalendarId()!!.toString())

            myEdit.apply()
        }
    }

    @Suppress("SameParameterValue")
    private fun checkPermissions(
        permissions: List<String>,
        explanation: String,
        whenPermissionGranted: () -> Unit
    ) {

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                if (results.all { (_, permission) -> permission }) {
                    // Permissions granted. Set the shared preferences up.
                    whenPermissionGranted()
                } else {
                    this.finish()
                }
            }


        if (permissions.all {
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            // You can use the API that requires the permission.
            whenPermissionGranted()
        } else {
            // Directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.

            MaterialAlertDialogBuilder(this)
                .setTitle("Requesting permissions")
                .setMessage(explanation)
                .setPositiveButton("I understand") { _, _ ->
                    requestPermissionLauncher.launch(permissions.toTypedArray())
                }
                .setNegativeButton("I disagree") { _, _ ->
                    //turn off the app if permissions are not granted.
                    this.finish()
                }.show()
        }
    }

    private fun setupNavigation() {
        // Initialize the navigation host
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
        navController = navHostFragment.navController

        // Initialize the bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        binding.fab.setOnClickListener {
            // Go to scanning

            if (!premiumAccount && !businessAccount)
                if (scans > 0) {
                    binding.bottomNavigation.selectedItemId = R.id.placeholder_fab
                    retrieveLoc() // Update location
                    navController.navigate(R.id.SecondFragment)
                } else {
                    noScanDialog()
                }
            else {
                binding.bottomNavigation.selectedItemId = R.id.placeholder_fab
                retrieveLoc()
                navController.navigate(R.id.SecondFragment)
            }

        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                // Go to home
                R.id.navigation_home -> navController.navigate(R.id.home)

                // Go to tutorial / help page
                R.id.navigation_help -> navController.navigate(R.id.tutorialFragment)
                // Go to the history page
                R.id.navigation_history -> navController.navigate(R.id.eventsHistoryFragment)

                // Go to the settings page
                R.id.navigation_settings ->
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_CALENDAR
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        navController.navigate(R.id.settingsMenu)
                    } else {
                        Toast.makeText(
                            this,
                            "You don't have calendar permissions enabled. The app will " +
                                    "not function without them. Please restart the application and grant these permissions.",
                            Toast.LENGTH_LONG
                        ).show()
                    }


            }
            return@setOnItemSelectedListener true
        }
    }

    private fun retrieveAppSettings() {
        val sh = getSharedPreferences(getString(R.string.preferences_address), MODE_PRIVATE)
        premiumAccount = sh.getBoolean("isPremiumUser", false)
        scans = sh.getInt("numberOfScans", 1)
    }

    private fun updateScanNumber() {
        updateScans(jwt, scans) {}
    }

    // Used in Modify Event to subtract the amount of scans
    fun scanCountSub() {
        scans--
        updateScanNumber()
    }

    private fun scanCountAdd() {
        scans += 3
        updateScanNumber()
    }

    // Dialog for when there is no leftover scans
    private fun noScanDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.alert_dialog_title))
            .setMessage(resources.getString(R.string.alert_dialog_message))
            .setNegativeButton(resources.getString(R.string.decline)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.watch_ad)) { _, _ ->
                showRewardedVideo()
                Log.d(logTag, "You watched the ad")
            }
            .show()
    }

    // Function for loading the reward ad
    private fun loadRewardedAd() {
        if (premiumAccount || businessAccount) {
            return
        }
        RewardedAd.load(
            this,
            getString(R.string.ad_id_reward),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(logTag, adError.message)
                    mRewardedAd = null
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(logTag, "Reward Ad was loaded.")
                    mRewardedAd = rewardedAd
                }
            })
    }

    // Function for showing the reward video and handling callbacks
    private fun showRewardedVideo() {
        if (mRewardedAd != null) {
            mRewardedAd?.show(this) {
                scanCountAdd() //reward
                Log.d("Reward AD", "User earned the reward.")
            }

            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("Reward AD", "Ad was dismissed.")
                    mRewardedAd = null
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("Reward AD", "Ad failed to show.")
                    mRewardedAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("Reward AD", "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                }
            }
        }
    }

    //TODO: MOVE THIS INTO SETTINGS?
    //TODO: maybe keep a stub to choose a default calendar upon launch
    fun getCalendarId(): Long? {
        //via https://stackoverflow.com/questions/16242472/retrieve-the-default-calendar-id-in-android
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )
        var calCursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.IS_PRIMARY + "=1",
            null,
            CalendarContract.Calendars._ID + " ASC"
        )
        if (calCursor != null && calCursor.count <= 0) {
            calCursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                CalendarContract.Calendars._ID + " ASC"
            )
        }
        if (calCursor != null) {
            if (calCursor.moveToFirst()) {
                val calName: String
                val calID: String
                val nameCol = calCursor.getColumnIndex(projection[1])
                val idCol = calCursor.getColumnIndex(projection[0])
                calName = calCursor.getString(nameCol)
                calID = calCursor.getString(idCol)
                Log.d("CAL", "Calendar name = $calName Calendar ID = $calID")
                val helloTutorial = Toast.makeText(
                    applicationContext,
                    "Events will be created at this calendar: $calName",
                    Toast.LENGTH_SHORT
                )
                helloTutorial.show()
                calCursor.close()
                return calID.toLong()
            }
        }
        return null
    }

    override fun onStart() {
        super.onStart()
        tutorial()
    }

    private fun tutorial() {
        val sh = getSharedPreferences(getString(R.string.preferences_address), MODE_PRIVATE)
        val myEdit = sh.edit()

        if (!sh.contains("prevRun")) {
            myEdit.putBoolean("prevRun", true)
            myEdit.apply()
            navController.navigate(R.id.tutorialFragment)
        }
    }

}
