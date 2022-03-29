package com.example.ocrhotel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentSuccessfulScanBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SuccessfulScan: Fragment() {

    private var _binding: FragmentSuccessfulScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSuccessfulScanBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.scanAgain.setOnClickListener {
//            if((activity as MainActivity?)?.scans!! > 0) {
//                findNavController().navigate(R.id.action_successfulScan_to_SecondFragment)
//            }
//            else{
//                (activity as MainActivity?)?.noScanDialog()
//            }
//        }

        binding.navigateHome.setOnClickListener {
            findNavController().navigate(R.id.action_successfulScan_to_Home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
