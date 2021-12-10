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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.ocrhotel.databinding.FragmentSecondBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    class EventDataViewModel : ViewModel() {
        var eventData: MutableLiveData<Algorithm.Result?> = MutableLiveData(null)
        var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
        var errorOccured: MutableLiveData<Boolean> = MutableLiveData(false)

    }

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var imageProvider: ImageProvider

    private val algorithmModel: EventDataViewModel by activityViewModels()


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

        imageProvider = ImageProvider(this, activity, this::handleImage)
        binding.uploadImage.setOnClickListener {
            imageProvider.useGallery()
        }

        binding.camera.setOnClickListener {
            imageProvider.useCamera()
        }

        algorithmModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        algorithmModel.errorOccured.observe(viewLifecycleOwner, { isError ->
            if (isError) {
                Toast.makeText(
                    context,
                    "Sorry, there has been an error while processing your image.",
                    Toast.LENGTH_SHORT
                ).show()
                algorithmModel.errorOccured.postValue(false)
                Log.w("OCR", "there has been an error!")
            }
        })

        algorithmModel.eventData.observe(viewLifecycleOwner, { res ->
            if (res != null) {
                // Reset the model for the next time
                algorithmModel.eventData.postValue(null)
                algorithmModel.isLoading.postValue(false)
                algorithmModel.errorOccured.postValue(false)


                // Prepare data to be passed to ModifyEvent
                val bundle =
                    bundleOf(
                        "date" to res.dateTime,
                        "title" to algorithmModel.eventData.value!!.name
                    )

                // Proceed to the ModifyEvent fragment
                findNavController().navigate(
                    R.id.action_SecondFragment_to_modifyEvent,
                    bundle
                )
            }
        })
    }

    private fun handleImage(uri: Uri) {
        fun handleError() {

            algorithmModel.errorOccured.postValue(true)
            algorithmModel.isLoading.postValue(false)
        }

        activity?.let { a ->
            a.contentResolver.openInputStream(uri)?.let { inputStream ->
                val bytes = inputStream.readBytes();
                val ocr = OCRAzureREST()
                val algo = Algorithm()
                algorithmModel.isLoading.postValue(true) // Tell the loading spinner that it can stop
                ocr.getImageTextData(bytes, { s ->
                    s?.let { result ->
                        Log.d("OCR", result)
                        try {
                            val data = algo.execute(ocr.resultsText, ocr.results)
                            algorithmModel.eventData.postValue(data)
                        } catch (e: Exception) {
                            handleError()
                        }
                    }
                }) { e -> handleError() }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
