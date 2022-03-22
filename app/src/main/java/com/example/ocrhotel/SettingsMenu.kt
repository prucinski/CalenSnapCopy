package com.example.ocrhotel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ocrhotel.databinding.FragmentSettingsMenuBinding

//TODO: AS OF 22.03, THIS FRAGMENT IS OBSOLETE. Delete
/**
 * A simple [Fragment] subclass as the destination from the settings icon.
 */
class SettingsMenu : Fragment() {

    private var _binding: FragmentSettingsMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsMenuBinding.inflate(inflater, container, false)
        return binding.root
    }



}