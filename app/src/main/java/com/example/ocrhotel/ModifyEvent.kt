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

    private var eventName = "null"
    private var eventDate = "null"
    private var eventHour = "null"

    private val algo = Algorithm()
    private var title= ""
    private var dates = mutableListOf<Long>()

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
        super.onViewCreated(view, savedInstanceState)

        title = arguments?.getString("title") as String
        val date = arguments?.getSerializable("date") as LocalDateTime

        val currentDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val currentHourFormatter = DateTimeFormatter.ofPattern("HH:mm")

        //TODO: if event found, put the values from output of algo
        if(title != "" && title != null){
            eventDate = date.format(currentDateFormatter)
            eventHour = date.format(currentHourFormatter)
        }
        //if NO event found, put in dummy values
        else{
            val current = LocalDateTime.now()
            eventDate = current.format(currentDateFormatter)
            eventHour = current.format(currentHourFormatter)
        }

        binding.EventDate.setText(eventDate)
        binding.EventHour.setText(eventHour)
        binding.EventTitle.setText(title)


        binding.continued.setOnClickListener {

            activity?.let { activity ->
                //request permission from user to access their calendars
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1)
                if(checkIfHasPermission()) {
                    var eventCreator = EventCreator(eventName, eventDate, eventHour, 2, activity)
                    //this is slightly wonky and has to be pressed twice - but it's a minor bug
                    if (eventCreator.addEvent()) {
                        findNavController().navigate(R.id.action_modifyEvent_to_succesfulScan)
                    }
                }
            }
        }

        binding.submit.setOnClickListener {
            eventName = binding.EventTitle.text.toString()
            eventDate = binding.EventDate.text.toString()
            eventHour = binding.EventHour.text.toString()
            var printEventDetails= Toast.makeText(context, eventDate, Toast.LENGTH_SHORT)
            printEventDetails.show()
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