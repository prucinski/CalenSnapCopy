package com.example.ocrhotel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentModifyEventBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ModifyEvent : Fragment() {

    // The events list. It is modifiable.
    private var eventsList: List<Event> = emptyList()

    // Keep track which event we're looking at now. By default we're looking at the first event.
    private var currentEvent = 0

    // This will decide how many entries will be generated with the spinner.
    private var numberOfEvents = 0

    // Custom date format that we may use while displaying the date to clients.
    private val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-uuuu")

    private var _binding: FragmentModifyEventBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adRequest = AdRequest.Builder().build()
    private var mInterstitialAd: InterstitialAd? = null
    private val TAG = "Interstitial Ad"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        super.onCreate(savedInstanceState)
        // Use the Kotlin extension in the fragment-ktx artifact
//        setFragmentResultListener("eventData") { requestKey, bundle ->
//
//            // Receive both results, this is here to display the usage but can be definitely improved.
//            val results = bundle.get("ocrResults") as ReadOperationResult
//            val textResults = bundle.getString("ocrStringResults","")
//            title = algo.extractTitleFromReadOperationResult(results)
//            dates = algo.extractDates(textResults) as MutableList<Long>
//            // Do something with the result
//        }
        
        //Interstitial Add code
        loadInterAd()

        _binding = FragmentModifyEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // TODO: GET THAT EVENT ARRAY - change key
        // Receive the event list from SecondFragment.
        eventsList = arguments?.getSerializable("data") as List<Event>
        numberOfEvents = eventsList.size
        Log.d("List size", " in ModifyEvent on creation: $numberOfEvents")

        val fillEvents = resources.getStringArray(R.array.events)
        super.onViewCreated(view, savedInstanceState)

        //Applying the spinner
        val spinner: Spinner = binding.foundEventsSelector
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fillEvents.slice(0 until numberOfEvents))
            .also{adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter}

        binding.EventDate.setOnClickListener{
            val currentDate = LocalDate.parse(binding.EventDate.text.toString(), dateFormatter)
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)*1000)
                    .build()

            datePicker.show(parentFragmentManager,"DATE")

            datePicker.addOnPositiveButtonClickListener {
                binding.EventDate.text = LocalDateTime.ofEpochSecond(
                    datePicker.selection!!/1000,0, ZoneOffset.UTC)
                    .format(dateFormatter)
            }
        }

        binding.EventHour.setOnClickListener {
            val clockFormat = if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            val currentTime = LocalTime.parse(binding.EventHour.text.toString())
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(currentTime.hour)
                .setMinute(currentTime.minute)
                .setTitleText("Select time")
                .build()

            timePicker.show(parentFragmentManager,"TIME")

            timePicker.addOnPositiveButtonClickListener{
                binding.EventHour.text = LocalTime.of(timePicker.hour,timePicker.minute)
                    .toString()
            }
        }


        //button "Continue".
        binding.continued.setOnClickListener {
            activity?.let { activity ->
                //request permission from user to access their calendars
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1)
                if(checkIfHasPermission()) {
                    val eventCreator = EventCreator(eventsList, activity)
                    //this is slightly wonky and has to be pressed twice - but it's a minor bug
                    if (eventCreator.addEvent()) {
                        (getActivity() as MainActivity?)?.scanCountSub() //removes a scan
                        showInterAd()
                        findNavController().navigate(R.id.action_modifyEvent_to_successfulScan)
                    }
                    else{
                        Toast.makeText(context, "Something went horribly wrong with adding the event. Please restart the app.", Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    Toast.makeText(context, "Sorry, you don't have permissions for your calendar enabled.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //spinner choices. Called on view creation at index 0 (as we want it)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                Toast.makeText(context, "This has been selected $pos", Toast.LENGTH_SHORT).show()
                currentEvent = pos
                binding.EventDate.text = eventsList[currentEvent].eventDate
                binding.EventHour.text = eventsList[currentEvent].eventHour
                binding.eventTitle.setText(eventsList[currentEvent].eventName)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //Something will always be selected
            }
        }

        //Button "Submit".
        binding.submit.setOnClickListener {
            // Once the button is pressed, modify the values inside the list.
            eventsList[currentEvent].eventName = binding.eventTitle.text.toString()

            val date: LocalDate = LocalDate.parse(binding.EventDate.text, dateFormatter)
            val time: LocalTime = LocalTime.parse(binding.EventHour.text.toString())
            eventsList[currentEvent].eventDateTime = LocalDateTime.of(
                date.year, date.monthValue, date.dayOfMonth, time.hour, time.minute)

            // I'm not sure if the next two lines are necessary, but I think they are since
            // the object has already been constructed.
            eventsList[currentEvent].eventDate = binding.EventDate.text.toString()
            eventsList[currentEvent].eventHour = binding.EventHour.text.toString()

            //TODO: Automatically move user to next event. Fairly simple but left  it for now
        }
    }
    private fun checkIfHasPermission() :Boolean{
        val result = context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_CALENDAR) }
        Log.e("PERMISSION", "$result" )
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
   //Function for loading the interstitial ad 
   private fun loadInterAd(){
        InterstitialAd.load(activity,getString(R.string.ad_id_interstitial), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }
    
    //Function for showing the interstitial ad
    private fun showInterAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    mInterstitialAd = null
                    loadInterAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d(TAG, "Ad failed to show.")
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                }
            }
        }
    } 
}
