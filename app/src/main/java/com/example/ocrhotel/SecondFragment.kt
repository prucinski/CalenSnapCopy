package com.example.ocrhotel

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ocrhotel.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var imageProvider: ImageProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageProvider = ImageProvider(this, activity, this::handleImage)
        binding.uploadImage.setOnClickListener {
            imageProvider.useGallery()
        }

        binding.camera.setOnClickListener {
            imageProvider.useCamera()
        }
    }

    private fun handleImage(uri: Uri) {
        // TODO: handle creation of image
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
