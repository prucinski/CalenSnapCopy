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
import com.example.ocrhotel.models.Event
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.MINUTES

class ModifyEvent : Fragment() {

    // The events list. It is modifiable.
    private lateinit var eventsList: MutableList<Event>

    // Keep track which event we're looking at now. By default we're looking at the first event.
    private var selectedIdx = 0
    private lateinit var selectedEvent: Event

    // Custom date format that we may use while displaying the date to clients.
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-uuuu")
    private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-uuuuHH:mm")

    private var _binding: FragmentModifyEventBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adRequest = AdRequest.Builder().build()
    private var mInterstitialAd: InterstitialAd? = null
    private val debugTag = "Interstitial Ad"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentModifyEventBinding.inflate(inflater, container, false)

        //Interstitial Add code
        loadInterAd()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Receive the event list from SecondFragment. Suppressing it because it's annoying me.
        @Suppress("UNCHECKED_CAST")
        eventsList = arguments?.getSerializable("data") as MutableList<Event>

        if (eventsList.size == 0) {
            Toast.makeText(context, "No events were found.", Toast.LENGTH_LONG).show()
            eventsList.add(Event())
        }
        // Set the current event
        selectedEvent = eventsList[0]

        // TODO: Use this whenever the algorithm has been modified to actually understand multiple titles.
        // val fillEvents = eventsList.map{it.eventName}.toMutableList()
        val fillEvents = eventsList.mapIndexed { idx, _ -> "Event ${idx + 1}" }.toMutableList()

        super.onViewCreated(view, savedInstanceState)

        // Set the default values for the text fields.
        setTextFields()

        //Applying the spinner
        val spinner: Spinner = binding.foundEventsSelector
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fillEvents)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }


        // Spinner choices. Called on view creation at index 0 (as we want it)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // Update [eventsList] with the current value.
                updateEventsList()

                // Change the values in the fields, starts here:
                selectedIdx = pos

                // Keep the reference to the current event so we can change it on demand.
                selectedEvent = eventsList[selectedIdx]

                // Update the text fields with the value from the changed index.
                setTextFields()

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }


        //update event date
        binding.EventDate.setOnClickListener {
            val currentDate = LocalDate.parse(binding.EventDate.text.toString(), dateFormatter)
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000)
                    .build()

            datePicker.show(parentFragmentManager, "DATE")

            datePicker.addOnPositiveButtonClickListener {
                binding.EventDate.text = LocalDateTime.ofEpochSecond(
                    datePicker.selection!! / 1000, 0, ZoneOffset.UTC
                )
                    .format(dateFormatter)

            }
        }
        //update event hour
        binding.EventHour.setOnClickListener {
            val clockFormat =
                if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            val currentTime = LocalTime.parse(binding.EventHour.text.toString())
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(currentTime.hour)
                .setMinute(currentTime.minute)
                .setTitleText("Select time")
                .build()

            timePicker.show(parentFragmentManager, "TIME")

            timePicker.addOnPositiveButtonClickListener {
                binding.EventHour.text = LocalTime.of(timePicker.hour, timePicker.minute)
                    .toString()

            }
        }

        //update event date
        binding.endEventDate.setOnClickListener {
            val endDate =
                eventsList[selectedIdx].eventDateTime.plusMinutes(eventsList[selectedIdx].duration)
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(endDate.toEpochSecond(ZoneOffset.UTC) * 1000)
                    .build()

            datePicker.show(parentFragmentManager, "DATE")

            datePicker.addOnPositiveButtonClickListener {
                binding.endEventDate.text = LocalDateTime.ofEpochSecond(
                    datePicker.selection!! / 1000, 0, ZoneOffset.UTC
                )
                    .format(dateFormatter)

            }
        }
        //update event hour
        binding.endEventHour.setOnClickListener {
            val clockFormat =
                if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            val endHour =
                eventsList[selectedIdx].eventDateTime.plusMinutes(eventsList[selectedIdx].duration)
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(endHour.hour)
                .setMinute(endHour.minute)
                .setTitleText("Select time")
                .build()

            timePicker.show(parentFragmentManager, "TIME")

            timePicker.addOnPositiveButtonClickListener {
                binding.endEventHour.text = LocalTime.of(timePicker.hour, timePicker.minute)
                    .toString()

            }
        }

        // Button "Add" adds a new event in case some were missed.
        binding.addButton.setOnClickListener {

            eventsList.add(Event())
            fillEvents.add("Event")
            //recreate the spinner
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fillEvents)
                .also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
            //select the event that was just added
            spinner.setSelection(eventsList.size - 1)
        }

        // Button "Delete". Used to delete an unwanted event.
        binding.deleteButton.setOnClickListener {
            //leave the screen if all the events have been deleted.
            if (eventsList.size == 1) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.deleteLastTitle)
                    .setMessage(R.string.deleteLastMessage)
                    .setNegativeButton(R.string.deleteLastNo) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Delete anyway") { _, _ ->
                        findNavController().popBackStack()
                        // findNavController().navigate(R.id.action_modifyEvent_to_home)
                    }
                    .show()
            }
            // Delete the event
            else {
                eventsList.removeAt(selectedIdx)
                fillEvents.removeAt(selectedIdx)
                //repopulate the spinner
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fillEvents)
                    .also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }

                if (selectedIdx > 0) selectedIdx -= 1
                else if (eventsList.size == 1) selectedIdx = 0
                // if index is already 0 and it's not the only element in list, we do not change anything

                selectedEvent = eventsList[selectedIdx]
                setTextFields()

                spinner.setSelection(selectedIdx)
            }
        }


        //button "Finish". Used by the user to confirm that they want to add the events to the
        //calendar
        binding.finish.setOnClickListener {
            activity?.let { activity ->
                //request permission from user to access their calendars
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
                    1
                )
                if (checkIfHasPermission()) {

                    // Update the list again with the current value before completing.
                    updateEventsList()

                    val eventCreator = EventCreator(eventsList, activity)

                    if (eventCreator.addEvent()) {
                        if (!((activity as MainActivity).premiumAccount || activity.businessAccount)) {
                            activity.scanCountSub() //removes a scan
                            Toast.makeText(
                                context,
                                "You can buy premium to get rid of these pesky ads and support our work!",
                                Toast.LENGTH_LONG
                            ).show()
                            showInterAd()
                        }
                        // Update the database with the new entries
                        activity.reloadEvents()

                        // Process to splash screen.
                        findNavController().navigate(R.id.action_modifyEvent_to_successfulScan)
                    } else {
                        Toast.makeText(
                            context,
                            "Something went wrong with adding the event. Please restart the app.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Sorry, you don't have permissions for your calendar enabled. Try restarting the app.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }

    private fun setTextFields() {
        // Set the new title
        binding.eventTitle.setText(selectedEvent.eventName)

        // Set the new start time
        binding.EventDate.text = selectedEvent.eventDate
        binding.EventHour.text = selectedEvent.eventHour

        // Set the new end time
        selectedEvent.eventDateTime.plusMinutes(selectedEvent.duration).let {
            binding.endEventDate.text = it.format(dateFormatter)
            binding.endEventHour.text = it.format(hourFormatter)
        }
    }

    private fun updateEventsList() {
        selectedEvent.eventName = binding.eventTitle.text.toString()
        val startTime = LocalDateTime.parse(
            binding.EventDate.text.toString() + binding.EventHour.text.toString(),
            dateTimeFormatter
        )
        selectedEvent.eventDateTime = startTime
        selectedEvent.duration = MINUTES.between(
            startTime,
            LocalDateTime.parse(
                binding.endEventDate.text.toString() + binding.endEventHour.text.toString(),
                dateTimeFormatter
            )
        )

        // Update the current event.
        eventsList[selectedIdx] = selectedEvent
    }

    private fun checkIfHasPermission(): Boolean {
        val result = context?.let {
            ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_CALENDAR)
        }
        Log.e("PERMISSION", "$result")
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Function for loading the interstitial ad
    private fun loadInterAd() {
        InterstitialAd.load(
            requireContext(),
            getString(R.string.ad_id_interstitial),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

    // Displays the interstitial ad to the user.
    private fun showInterAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(requireActivity())
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(debugTag, "Ad was dismissed.")
                    mInterstitialAd = null
                    loadInterAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(debugTag, "Ad failed to show.")
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(debugTag, "Ad showed fullscreen content.")
                }
            }
        }
    }
}

