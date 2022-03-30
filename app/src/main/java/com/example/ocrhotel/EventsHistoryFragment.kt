package com.example.ocrhotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.ocrhotel.databinding.FragmentEventsHistoryBinding
import com.example.ocrhotel.placeholder.PlaceholderContent
import java.time.LocalDateTime
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


/**
 * Fragment representing the events history.
 */
class EventsHistoryFragment : Fragment() {

    private var _binding: FragmentEventsHistoryBinding? = null

    private val binding get() = _binding!!

    private val viewModel by viewModels<EventHistoryViewModel>()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsHistoryBinding.inflate(inflater, container, false)
        val view = binding.root

        val act = activity as MainActivity

//        val historyItems = PlaceholderContent.ITEMS
        val jwt = getJwtFromPreferences(requireContext())
//        val historyItems: MutableList<Event> = arrayListOf()
//        val historyItems by mutableStateListOf<Event>()
        val historyItems = MutableLiveData(listOf<Event>())
        if (jwt != null) {
            readUserEvents(jwt) { userEvents ->
                val events = mutableListOf<Event>()
                if (userEvents != null) {
                    for (event in userEvents.events) {
                        events.add(Event(event.title))
                    }
                }
                viewModel.historyItems.postValue(events)
            }
        } else {
            // If there is no JWT, navigate to the login page.
            val navHostFragment =
                activity?.supportFragmentManager?.findFragmentById(R.id.main_content) as NavHostFragment
            navHostFragment.navController.navigate(R.id.loginFragment)
        }


        // TODO: You can do something like this to filter events.
        // val now = LocalDateTime.now()
        // historyItems.filter{
        //     it.eventDateTime.isBefore(now)
        // }


        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                MaterialTheme {
                    EventHistoryScreen(act.premiumAccount, viewModel)
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventHistoryScreen(premium: Boolean, model: EventHistoryViewModel) {
    val items: List<Event> by model.historyItems.observeAsState(listOf())

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = if (!premium) 55.dp else 0.dp)
    ) {
        items(items) { item ->
            EventTile(item)
        }
    }

}

class EventHistoryViewModel : ViewModel() {
    val historyItems = MutableLiveData(listOf<Event>())
}