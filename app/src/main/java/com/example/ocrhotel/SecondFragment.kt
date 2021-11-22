package com.example.ocrhotel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentSecondBinding
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // TODO: handle return value
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            // TODO: handle return value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uploadImage.setOnClickListener {
            //again - this section  needs expanding
            filePickerLauncher.launch("image/*")
//            val helloUpload = Toast.makeText(context, "Now this will lead to the gallery", Toast.LENGTH_SHORT)
//            helloUpload.show()
        }

        binding.camera.setOnClickListener {
            val file = File(context?.filesDir, "tempPictureFile")
            val a = activity
            a?.let {
                // Get uri for the file that was just created
                val uri = FileProvider.getUriForFile(
                    Objects.requireNonNull(a.applicationContext),
                    BuildConfig.APPLICATION_ID + ".provider", file
                );
                // Actually launch the camera
                cameraLauncher.launch(uri)
            }
        }

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
