package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ocrhotel.databinding.FragmentEventsHistoryBinding
import com.example.ocrhotel.models.EventListModel
import com.example.ocrhotel.widgets.EventTile
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(top = if (!act.premiumAccount && !act.businessAccount) 55.dp else 0.dp)


                    ) {
                        Divider(thickness = 2.dp)

                        Text(
                            "Past Events",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(5.dp)
                        )
                        Divider(thickness = 2.dp)

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {

                            items(items = events.getPastEvents()) { event ->
                                EventTile(event) {
                                    events.removeEvent(event)
                                }
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