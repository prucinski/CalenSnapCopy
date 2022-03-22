package com.example.ocrhotel

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.ocrhotel.databinding.ActivityMainBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    private val adRequest = AdRequest.Builder().build()
    private var mRewardedAd: RewardedAd? = null
    private var TAG = "MainActivity"

    //init to random values
    var premiumAccount = false
    var scans = 1

    //TODO: MOVE THIS INTO SETTINGS. AFTER MOVING, IMPLEMENT CHOOSING
    //TODO: maybe keep a stub to choose a default calendar upon launch
    fun getCalendarId() : Long? {
        //via https://stackoverflow.com/questions/16242472/retrieve-the-default-calendar-id-in-android
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
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
                Log.d("CAL","Calendar name = $calName Calendar ID = $calID")
                val helloTutorial = Toast.makeText(applicationContext, "Event is created at this calendar: $calName", Toast.LENGTH_SHORT)
                helloTutorial.show()
                calCursor.close()
                return calID.toLong()
            }
        }
        return null
    }
    private fun setupSharedPrefs(){
        // Storing data into SharedPreferences
        //Initialization on first app launch.
        //This file is present only on the device and not in this project.
        var sh = getSharedPreferences("CalenSnapSharedPreferences", MODE_PRIVATE)
        //check if file already present. if not, create it
        val filePresent = sh.getBoolean("fileExists", false)
        if(!filePresent) {
            val myEdit = sh.edit()
            //VALUES INITIALIZED DURING LAUNCH.
            myEdit.putBoolean("isPremiumUser", false)
            myEdit.putBoolean("filePresent", true)
            myEdit.putInt("numberOfScans", 1)
            //check if there is a calendar permission. This is kind of dead code right now.
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                if(sh.getLong("calendarID", -1) == -1L){
                    myEdit.putLong("calendarID", getCalendarId()!!)
                }
            }
            myEdit.commit()
        }
    }
    //MOVED HERE from EventCreator() as we want to choose the calendar somewhere else.
    fun findCalendarID(){

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSharedPrefs()
        //retrieve values that we want.
        var sh = getSharedPreferences("CalenSnapSharedPreferences", MODE_PRIVATE)
        premiumAccount = sh.getBoolean("isPremiumUser", false)
        scans = sh.getInt("numberOfScans", 1)



        Log.e("ACT","onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        // Initialize the navigation host
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
        navController = navHostFragment.navController

        // Initialize the bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        if(!premiumAccount){
            //Code for ads
            MobileAds.initialize(this) {}

            //Banner ad
            val mAdView = findViewById<AdView>(R.id.adView)
            mAdView.loadAd(adRequest)

            //Load reward ad
            loadRewardedAd()
        }

        binding.fab.setOnClickListener {
            // Go to scanning
            if(!premiumAccount)
                if(scans > 0) {
                    binding.bottomNavigation.selectedItemId = R.id.placeholder_fab
                    navController.navigate(R.id.SecondFragment)
                }
                else{
                  noScanDialog()
                }
            else {
                binding.bottomNavigation.selectedItemId = R.id.placeholder_fab
                navController.navigate(R.id.SecondFragment)
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                // Go to home
                R.id.navigation_home -> navController.navigate(R.id.home)

                // Go to tutorial / help page
                R.id.navigation_help -> Toast.makeText(peekAvailableContext(), "This will lead to a tutorial!", Toast.LENGTH_SHORT).show()

                // Go to the history page
                R.id.navigation_history -> navController.navigate(R.id.eventsHistoryFragment)

                // Go to the settings page
                R.id.navigation_settings -> navController.navigate(R.id.settingsMenu)

            }
            return@setOnItemSelectedListener true
        }

    }
    fun updateScanNumber(){
        var sh = getSharedPreferences("CalenSnapSharedPreferences", MODE_PRIVATE)
        val myEdit = sh.edit()
        myEdit.putInt("numberOfScans", scans)
        myEdit.apply()
    }

    // Used in Modify Event to subtract the amount of scans
    fun scanCountSub() {
        if(!premiumAccount){
            scans--
            updateScanNumber()
        }

    }
    private fun scanCountAdd() {
        scans+=3
        updateScanNumber()
    }

    // Dialog for when there is no leftover scans
    fun noScanDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.alert_dialog_title))
            .setMessage(resources.getString(R.string.alert_dialog_message))
            .setNegativeButton(resources.getString(R.string.decline)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.watch_ad)) { _, _ ->
                showRewardedVideo()
                Log.d(TAG,"You watched the ad")
            }
            .show()
    }

    // Function for loading the reward ad
    private fun loadRewardedAd() {
        RewardedAd.load(this,getString(R.string.ad_id_reward), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mRewardedAd = null
            }
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Reward Ad was loaded.")
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

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
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




}
