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
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

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

        //Code for the banner ads
        MobileAds.initialize(this) {}
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        binding.fab.setOnClickListener {
            // Go to scanning
            binding.bottomNavigation.selectedItemId = R.id.placeholder_fab
            navController.navigate(R.id.SecondFragment)
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

}