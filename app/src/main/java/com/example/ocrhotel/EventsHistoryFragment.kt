package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.ocrhotel.databinding.FragmentEventsHistoryBinding
import com.example.ocrhotel.ui.home.EventListModel
import com.google.android.material.composethemeadapter.MdcTheme



/**
 * Fragment representing the events history.
 */
class EventsHistoryFragment : Fragment() {

    private var _binding: FragmentEventsHistoryBinding? = null

    private val binding get() = _binding!!

    private val model: EventListModel by activityViewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsHistoryBinding.inflate(inflater, container, false)
        val view = binding.root

        val act = activity as MainActivity

        binding.composeView.apply{
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent{

                val events by remember{
                    mutableStateOf(model)
                }

                MdcTheme{
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top= if(!act.premiumAccount && !act.businessAccount) 55.dp else 0.dp)
                    ){
                        items(items=events.getPastEvents()){event->
                            EventTile(event){
                                events.removeEvent(event)
                            }
                        }
                    }

                }
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}