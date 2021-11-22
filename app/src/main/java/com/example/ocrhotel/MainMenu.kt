package com.example.ocrhotel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.ocrhotel.databinding.FragmentFirstBinding
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainMenu : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
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

        binding.scan.setOnClickListener {
            // Create a temporary file for storing the picture about to be taken
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
        binding.upload.setOnClickListener {
            filePickerLauncher.launch("image/*")
        }
        binding.tutorial.setOnClickListener {
            //toast is kind of like printing to console, but in android - idk how to get this to work
            val helloTutorial =
                Toast.makeText(context, "@string/tutorial_button_message", Toast.LENGTH_SHORT)
            helloTutorial.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}