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
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentModifyEventBinding
import com.example.ocrhotel.databinding.FragmentSecondBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ModifyEvent : Fragment() {

    private var eventName = "null"
    private var eventDate = "null"
    private var eventHour = "null"

    private var _binding: FragmentModifyEventBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentModifyEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: if event found, put the values from output of algo
        if(false){

        }
        //TODO: if NO event found, put in dummy values
        else{
            val current = LocalDateTime.now()
            val currentDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val currentHourFormatter = DateTimeFormatter.ofPattern("HH:mm")
            eventDate = current.format(currentDateFormatter)
            eventHour = current.format(currentHourFormatter)
            binding.EventDate.setText(eventDate.toString())
            binding.EventHour.setText(eventHour.toString())
        }



        binding.continued.setOnClickListener {
            //TODO - continue button clicked. Call a class that will create an event with variables and send
            //TODO - a calendar invitation that user may accept. Then, navigate


            activity?.let { activity ->
                //request permission from user to access their calendars
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1)
                if(checkIfHasPermission()) {
                    var eventCreator = EventCreator(eventName, eventDate, eventHour, 2, activity)
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