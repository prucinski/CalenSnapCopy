package com.example.ocrhotel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ocrhotel.databinding.FragmentEventHistoryBinding
import java.util.*

class EventHistoryFragment : Fragment() {
    class EventHistoryViewModel : ViewModel() {
        val events: MutableLiveData<APIUserEvents?> = MutableLiveData(null)
    }

    private val model: EventHistoryViewModel by activityViewModels()

    private var _binding: FragmentEventHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var adapter: ArrayAdapter<APIUserEvent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentEventHistoryBinding.inflate(inflater, container, false)

        adapter = ArrayAdapter<APIUserEvent>(
            requireContext(),
            R.layout.fragment_event_history,
            R.id.eventTextView
        )
        binding.historyListView.adapter = adapter

        model.events.observe(viewLifecycleOwner) { events ->
            if (events != null) {
                adapter.clear()
                adapter.addAll(*events.events)
            }
        }

//        readUserEvents(UUID.randomUUID()) {
//            model.events.postValue(it)
//        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}