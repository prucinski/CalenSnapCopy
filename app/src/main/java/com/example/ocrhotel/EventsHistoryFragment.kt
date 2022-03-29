package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.ocrhotel.databinding.FragmentEventsHistoryBinding
import com.example.ocrhotel.placeholder.PlaceholderContent
import java.time.LocalDateTime


/**
 * Fragment representing the events history.
 */
class EventsHistoryFragment : Fragment() {

    private var _binding: FragmentEventsHistoryBinding? = null

    private val binding get() = _binding!!

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentEventsHistoryBinding.inflate(inflater,container,false)
        val view = binding.root

        val act = activity as MainActivity

        var userEvents = readUserEvents( /*jwt */)

        val historyItems: MutableList<Event> = arrayListOf()
        for (i in 1..userEvents.size)
        {
            historyItems.add(Event(userEvents[i][1], userEvents[i][2]))
        }




        // TODO: You can do something like this to filter events.
        // val now = LocalDateTime.now()
        // historyItems.filter{
        //     it.eventDateTime.isBefore(now)
        // }

        binding.composeView.apply{
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent{
                MaterialTheme{
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical= if(!act.premiumAccount) 55.dp else 0.dp)
                    ){
                        items(historyItems){ item ->
                            EventTile(item)
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