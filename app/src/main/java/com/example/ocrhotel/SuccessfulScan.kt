package com.example.ocrhotel

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentSuccessfulScanBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SuccessfulScan: Fragment() {

    private var _binding: FragmentSuccessfulScanBinding? = null
    private lateinit var bottomBar : BottomAppBar
    private lateinit var fab : FloatingActionButton
    private lateinit var layout : FragmentContainerView
    var margin = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bottomBar = requireActivity().findViewById(R.id.bottom_bar)
        fab = requireActivity().findViewById(R.id.fab)

        bottomBar.visibility = GONE
        fab.visibility = GONE

        layout = requireActivity().findViewById(R.id.main_content)

        with(layout.layoutParams as ConstraintLayout.LayoutParams){
            margin = this.bottomMargin
            this.bottomMargin = 0
        }

        _binding = FragmentSuccessfulScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.navigateHome.setOnClickListener {
            findNavController().navigate(R.id.action_successfulScan_to_Home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        (layout.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = margin
        bottomBar.visibility = VISIBLE
        fab.visibility = VISIBLE
    }
}
