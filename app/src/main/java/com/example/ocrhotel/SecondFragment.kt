package com.example.ocrhotel

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.ocrhotel.databinding.FragmentSecondBinding
import com.example.ocrhotel.models.Algorithm
import com.example.ocrhotel.models.Event
import com.example.ocrhotel.models.ImageProvider
import com.example.ocrhotel.models.OCRAzureREST
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    class EventDataViewModel : ViewModel() {
        var eventData: MutableLiveData<List<Event>> = MutableLiveData(null)
        var progressIndicator: MutableLiveData<Progress> = MutableLiveData(Progress.NotStarted)
        var errorOccurred: MutableLiveData<Boolean> = MutableLiveData(false)
    }

    enum class Progress{
        NotStarted,
        Started,
        Resizing,
        Analyzing,
        // Finished
    }

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var imageProvider: ImageProvider

    private val algorithmModel: EventDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!(activity as MainActivity).loggedIn) {
            // User has to be logged in to scan images
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.loginFragment)
        }

        imageProvider = ImageProvider(this, activity, this::handleImage)
        binding.uploadImage.setOnClickListener {
            imageProvider.useGallery()
        }

        binding.camera.setOnClickListener {
            imageProvider.useCamera()
        }

        algorithmModel.progressIndicator.observe(viewLifecycleOwner) { status ->
            binding.loadingSpinner.visibility = when (status) {
                Progress.Started, Progress.Resizing, Progress.Analyzing -> View.VISIBLE
                else -> View.GONE
            }

            binding.progressIndicatorText.text = when(status) {
                Progress.Resizing -> "Processing image..."
                Progress.Analyzing -> "Finding events..."
                else -> ""
            }
        }

        algorithmModel.errorOccurred.observe(viewLifecycleOwner) { isError ->
            if (isError) {
                Toast.makeText(
                    context,
                    "Sorry, there has been an error while processing your image.",
                    Toast.LENGTH_SHORT
                ).show()
                algorithmModel.errorOccurred.postValue(false)
                Log.w("OCRSecondFragment", "There has been an error!")
            }
        }

        algorithmModel.eventData.observe(viewLifecycleOwner) { res ->
            if (res != null) {
                // Reset the model for the next time
                algorithmModel.eventData.postValue(null)
                algorithmModel.progressIndicator.postValue(Progress.NotStarted)
                algorithmModel.errorOccurred.postValue(false)


                // Prepare data to be passed to ModifyEvent
                val bundle =
                    bundleOf(
                        "data" to res
                        //"date" to res[0].eventDateTime,
                        //"title" to algorithmModel.eventData.value!![0]
                    )

                val navController = NavHostFragment.findNavController(this)
                navController.navigate(R.id.modifyEvent, bundle)

            }
        }
    }

    private fun handleImage(uri: Uri) {
        /**Clears the timer and puts out an error message.*/
        fun handleError() {

            algorithmModel.errorOccurred.postValue(true)
            algorithmModel.progressIndicator.postValue(Progress.NotStarted)
        }

        activity?.let { a ->

            // Tell the loading spinner to start spinning.
            algorithmModel.progressIndicator.postValue(Progress.Started)

            a.contentResolver.openInputStream(uri)?.let { inputStream ->


                val bytes: ByteArray = inputStream.readBytes()

                Log.d("App", "Image size: ${bytes.size / 1_000_000} MB.")

                // Create a temporary file that may be used to reduce the size of the image.
                val tempFile = File.createTempFile("__tempOCR_", "")
                // Delete the temporary file once it is no longer needed.
                tempFile.deleteOnExit()

                lifecycleScope.launch {

                    // Reduce to fit 4MB if the image is too big.
                    val image =
                        if (bytes.size > 4_000_000) context?.let {

                            algorithmModel.progressIndicator.postValue(Progress.Resizing)
                            // Only write bytes in case you actually have to create a new image.
                            tempFile.writeBytes(bytes)

                            Compressor.compress(it,tempFile){
                                size(maxFileSize = 4_000_000, maxIteration = 10)

                            }

                        }!!.readBytes()
                        else
                            bytes

                    Log.d("OCR" , "Final image size: ${image.size/1_000_000} MB.")

                    val ocr = OCRAzureREST()
                    val algo = Algorithm()

                    algorithmModel.progressIndicator.postValue(Progress.Analyzing)

                    ocr.getImageTextData(image, { s ->
                        s?.let { result ->
                            Log.d("OCR", result)
                            try {
                                val data = algo.execute(ocr.resultsText, ocr.results)
                                algorithmModel.eventData.postValue(data)
                            } catch (_: Exception) {
                                handleError()
                            }
                        }
                    })
                    // Error handling callback
                    {
                        handleError()
                    }
                }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
