package com.example.ocrhotel

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ocrhotel.databinding.FragmentSecondBinding
import java.io.File


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

//        imageProvider = ImageProvider(this, activity, this::handleImage)
//        binding.uploadImage.setOnClickListener {
//            imageProvider.useGallery()
//            handleImage(imageProvider)
//        }
//
//        binding.camera.setOnClickListener {
//            imageProvider.useCamera()
//        }

        handleImageURL("https://s3.amazonaws.com/thumbnails.venngage.com/template/112a39f4-2d97-44aa-ae3a-0e95a60abbce.png")

    }

    private fun handleImage(uri: String) {
//        val file = File(uri.path)
        val ocr = OCR_Azure_REST()

        ocr.GetImageTextDataFromURL(uri)

        val output = ocr.resultsText;

        Log.d("OCR", output)
        // TODO: handle creation of image
    }

    private fun handleImageURL(url: String) {
        val ocr = OCR_Azure_REST()
        ocr.GetImageTextDataFromURL(url)
        val output = ocr.resultsText

        Log.d("OCR", output)
        // TODO: handle creation of image
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
