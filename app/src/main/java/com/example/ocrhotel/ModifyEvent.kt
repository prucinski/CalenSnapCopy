package com.example.ocrhotel

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentModifyEventBinding
import com.example.ocrhotel.databinding.FragmentSecondBinding

class ModifyEvent : Fragment() {

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

        binding.continued.setOnClickListener {
            findNavController().navigate(R.id.action_modifyEvent_to_succesfulScan)
        }

        binding.submit.setOnClickListener {
            //idk why but NOTHING shows. I'll look into this later tonight
            Toast.makeText(context, "working", Toast.LENGTH_SHORT).show()
            var eventName = binding.EventTitle.text
            var printEventDetails= Toast.makeText(context, eventName, Toast.LENGTH_SHORT)
            printEventDetails.show()


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}