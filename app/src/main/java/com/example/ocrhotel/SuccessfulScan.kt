package com.example.ocrhotel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentFirstBinding
import com.example.ocrhotel.databinding.FragmentSuccsefulScanBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SuccessfulScan: Fragment() {

    private var _binding: FragmentSuccsefulScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSuccsefulScanBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.scanAgain.setOnClickListener {
            findNavController().navigate(R.id.action_succesfulScan_to_SecondFragment)
        }

        binding.navigateHome.setOnClickListener {
            findNavController().navigate(R.id.action_succesfulScan_to_MainMenu)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
