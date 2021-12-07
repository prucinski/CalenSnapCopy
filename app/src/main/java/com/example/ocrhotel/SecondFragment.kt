package com.example.ocrhotel

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
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

    private var eventData: Algorithm.Result? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.continueButton.isEnabled = false
//        binding.continueButton.isClickable = false
        binding.continueButton.setOnClickListener { handleContinue() }

        imageProvider = ImageProvider(this, activity, this::handleImage)
        binding.uploadImage.setOnClickListener {
            imageProvider.useGallery()
        }

        binding.camera.setOnClickListener {
            imageProvider.useCamera()
        }

//        handleImageURL("https://s3.amazonaws.com/thumbnails.venngage.com/template/112a39f4-2d97-44aa-ae3a-0e95a60abbce.png")

    }

    private fun handleContinue() {
        if (eventData != null) {
            val bundle =
                bundleOf("date" to eventData!!.dateTime, "title" to eventData!!.name)
            eventData = null
            findNavController().navigate(
                R.id.action_SecondFragment_to_modifyEvent,
                bundle
            )
        } else {
            Toast.makeText(context, "Result not available yet!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun handleImage(uri: Uri) {
        activity?.let { a ->
            a.contentResolver.openInputStream(uri)?.let { inputStream ->
                val bytes = inputStream.readBytes();
                val ocr = OCRAzureREST()
                val algo = Algorithm()
                ocr.getImageTextData(bytes) { s ->
                    s?.let { result ->
                        Log.d("OCR", result)
                        eventData = algo.execute(ocr.resultsText, ocr.results)
//                        binding.continueButton.isEnabled = true
//                        binding.continueButton.isClickable = true

                    }
                }
            }

        }

    }

    private fun handleImageURL(url: String) {
        val ocr = OCRAzureREST()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
