package com.example.ocrhotel

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter;
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentModifyEventBinding
import com.example.ocrhotel.databinding.FragmentSecondBinding
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOptionalParameter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ModifyEvent : Fragment() {


    //This will decide how many entries will be generated with the spinner.
    private var numberOfEvents = 3
    //The events list. It is modifiable.
    private var eventsList: List<Event> = emptyList()
    //Keep track which event we're looking at now. By default we're looking at the first event.
    private var currentEvent = 0;


    private var _binding: FragmentModifyEventBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        _binding = FragmentModifyEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //TODO: GET THAT EVENT ARRAY - change key
        //receive the event list from SecondFragment.
        //eventsList = arguments?.getSerializable("dat") as List<Event>
        //numberOfEvents = eventsList.size
        Log.e("List size", " in ModifyEvent on creation: $numberOfEvents")
        val fillEvents = getResources().getStringArray(R.array.events)
        super.onViewCreated(view, savedInstanceState)
        //Applying the spinner
        val spinner: Spinner = binding.spinner
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fillEvents.slice(0..numberOfEvents-1))
            .also{adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter}

        //set the name with the first event's information. This is I think unnecessary

        //button "Continue".
        binding.continued.setOnClickListener {
            activity?.let { activity ->
                //request permission from user to access their calendars
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1)
                if(checkIfHasPermission()) {
                    var eventCreator = EventCreator(eventsList, activity)
                    //this is slightly wonky and has to be pressed twice - but it's a minor bug
                    if (eventCreator.addEvent()) {
                        findNavController().navigate(R.id.action_modifyEvent_to_succesfulScan)
                    }
                    else{
                        Toast.makeText(context, "Something went horribly wrong with adding the event. Please restart the app.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        //spinner choices. Called on view creation at index 0 (as we want it)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                Toast.makeText(context, "This has been selected $pos", Toast.LENGTH_SHORT).show()
                currentEvent = pos
                binding.EventDate.setText(eventsList[currentEvent].eventDate)
                binding.EventHour.setText(eventsList[currentEvent].eventHour)
                binding.EventTitle.setText(eventsList[currentEvent].eventName)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //Something will always be selected
            }
        }

        //Button "submit".
        binding.submit.setOnClickListener {
            //once the button is pressed, modify the values inside the list.
            eventsList[currentEvent].eventName = binding.EventTitle.text.toString()
            //spaghetti
            //TODO - throw this out and fix UI
            val year = binding.EventDate.text.toString().slice(6..9).toInt()
            val month = binding.EventDate.text.toString().slice(3..4).toInt()
            val day = binding.EventDate.text.toString().slice(0..1).toInt()
            val hr = binding.EventHour.text.toString().slice(0..1).toInt()
            val min = binding.EventHour.text.toString().slice(2..3).toInt()
            eventsList[currentEvent].eventDateTime = LocalDateTime.of(year, month, day, hr, min)
            //I'm not sure if the next two lines are necessary, but I think they are since
            //the object has already been constructed.
            eventsList[currentEvent].eventDate = binding.EventDate.text.toString()
            eventsList[currentEvent].eventHour = binding.EventHour.text.toString()
            //printEventDetails.show()
            //TODO: Automatically move user to next event. Fairly simple but left  it for now
        }
    }
    private fun checkIfHasPermission() :Boolean{
        var result = context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_CALENDAR) }
        Log.e("PERMISSION", "$result" )
        return result == PackageManager.PERMISSION_GRANTED

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}