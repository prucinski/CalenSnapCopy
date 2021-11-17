package com.example.ocrhotel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentFirstBinding

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.scan.setOnClickListener {
            val camera = CameraProcessor()
            camera.takeImage()
            //this navigation doesn't mean anything yet
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            //I have no idea how this is going to go here. We might call a custom method invokeCamera(),
            //which is a separate class. That thing is then done and we go to a new screen with
            //confirmation/rejection
            //https://developer.android.com/training/camera/photobasics useful documentation
        }
        binding.upload.setOnClickListener {
            //again - this section  needs expanding
            val helloUpload = Toast.makeText(context, "This will lead to the gallery", Toast.LENGTH_SHORT)
            helloUpload.show()

        }
        binding.tutorial.setOnClickListener {
            //toast is kind of like printing to console, but in android - idk how to get this to work
            val helloTutorial = Toast.makeText(context, "@string/tutorial_button_message", Toast.LENGTH_SHORT)
            helloTutorial.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}