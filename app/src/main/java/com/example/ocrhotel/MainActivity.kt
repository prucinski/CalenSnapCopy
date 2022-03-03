package com.example.ocrhotel

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ocrhotel.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        //Code for the banner ads
        MobileAds.initialize(this) {}

        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val firstFragment = MainMenu()
        val secondFragment = SecondFragment()

        binding.fab.setOnClickListener {
            // Go to scanning
            setCurrentFragment(secondFragment)
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                // Go to home (currently just the previously declared SCAN+tutorial screen)
                R.id.navigation_home -> setCurrentFragment(firstFragment)

                // Go to tutorial / help page
                R.id.navigation_help -> Toast.makeText(baseContext, "This will lead to a tutorial!", Toast.LENGTH_SHORT).show()

                // Go to the history page
                R.id.navigation_history -> Toast.makeText(baseContext , "This will to the history!", Toast.LENGTH_SHORT).show()

                // Go to the settings page
                R.id.navigation_settings -> Toast.makeText(baseContext, "This will lead to the settings menu!", Toast.LENGTH_SHORT).show()

            }
            return@setOnItemSelectedListener true
        }



        // binding.fab.setOnClickListener {
        //     // Navigate form
        //     val navController = findNavController(R.id.nav_host_fragment_content_main)
        //
        //     navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
        //
        // }
    }

    private fun setCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_content,fragment)
            commit()
        }
    }

}
