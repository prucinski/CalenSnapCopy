package com.example.ocrhotel

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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

    var scans = 1
    private val premiumAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                R.id.navigation_settings -> Toast.makeText(peekAvailableContext(), "This will lead to the settings menu!", Toast.LENGTH_SHORT).show()

            }
            return@setOnItemSelectedListener true
        }

    }

    // Used in Modify Event to subtract the amount of scans
    fun scanCountSub() {
        scans--
    }
    private fun scanCountAdd() {
        scans++
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
