package com.example.ocrhotel

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
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

        imageProvider = ImageProvider(this, activity, this::handleImage)
        binding.uploadImage.setOnClickListener {
            imageProvider.useGallery()
        }

        binding.camera.setOnClickListener {
            imageProvider.useCamera()
        }

//        handleImageURL("https://s3.amazonaws.com/thumbnails.venngage.com/template/112a39f4-2d97-44aa-ae3a-0e95a60abbce.png")

    }

    private fun handleImage(uri: Uri) {
        Toast.makeText(context, uri.toString(), Toast.LENGTH_LONG).show()


        activity?.let { a ->
            a.contentResolver.openInputStream(uri)?.let { inputStream ->
                val bytes = inputStream.readBytes();
                val ocr = OCRAzureREST()

                ocr.getImageTextData(bytes) { s ->
                    s?.let { result ->
                        Log.d("OCR", result);
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
