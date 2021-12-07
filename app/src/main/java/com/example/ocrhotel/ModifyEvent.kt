package com.example.ocrhotel

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
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
        setFragmentResultListener("eventData") { requestKey, bundle ->

            // Receive both results, this is here to display the usage but can be definitely improved.
            val results = bundle.get("ocrResults") as ReadOperationResult
            val textResults = bundle.getString("ocrStringResults","")
            title = algo.extractTitleFromReadOperationResult(results)
            dates = algo.extractDates(textResults) as MutableList<Long>
            // Do something with the result
        }

        _binding = FragmentModifyEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: if event found, put the values from output of algo
        if(title != ""){

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
            var eventCreator = EventCreator(eventName, eventDate, eventHour)
            Toast.makeText(context, "event created", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_modifyEvent_to_succesfulScan)
        }

        binding.submit.setOnClickListener {



            eventName = binding.EventTitle.text.toString()
            eventDate = binding.EventDate.text.toString()
            eventHour = binding.EventHour.text.toString()
            var printEventDetails= Toast.makeText(context, eventDate, Toast.LENGTH_SHORT)
            printEventDetails.show()


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}