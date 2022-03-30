package com.example.ocrhotel.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.twotone.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.example.ocrhotel.*
import com.example.ocrhotel.R
import com.example.ocrhotel.databinding.FragmentHomeBinding
import com.example.ocrhotel.placeholder.PlaceholderContent

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()

    @OptIn(ExperimentalMaterialApi::class, androidx.compose.ui.unit.ExperimentalUnitApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val composeView = binding.composeHome

        fun goToLogin() {
            val navHostFragment =
                activity?.supportFragmentManager?.findFragmentById(R.id.main_content) as NavHostFragment
            navHostFragment.navController.navigate(R.id.loginFragment)

        }

        val jwt = getJwtFromPreferences(requireContext())
        if (jwt != null) {
            readUserEvents(jwt) { userEvents ->
                val events = mutableListOf<Event>()
                if (userEvents != null) {
                    for (event in userEvents.events) {
                        events.add(Event(event.title, extractDate(event.event_time)))
                    }
                } else {
                    goToLogin()
                }
                viewModel.historyItems.postValue(events)
            }
            readProfile(jwt) { profile ->
                if (profile != null) {
                    viewModel.profileData.postValue(profile)
                }

            }
        } else {
            // If there is no JWT, navigate to the login page.
            goToLogin()
        }

        composeView.setContent {
            MaterialTheme {
                Home(
                    model = viewModel
                )
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


class HomeViewModel : ViewModel() {
    val historyItems = MutableLiveData(listOf<Event>())
    val profileData = MutableLiveData(APIProfile())
}


@ExperimentalUnitApi
@ExperimentalMaterialApi
@Composable
fun Home(
    icon: ImageVector = Icons.Rounded.Person,
    model: HomeViewModel,
) {
    val items: List<Event> by model.historyItems.observeAsState(listOf())
    val profile: APIProfile by model.profileData.observeAsState(APIProfile())
    Scaffold(
        modifier = Modifier.padding(vertical = if (!profile.premium_user) 55.dp else 0.dp),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Image(
                        icon,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                },
                title = {
                    Column {
                        Text(profile.username)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (profile.premium_user) {
                                Icon(
                                    Icons.Filled.Star, contentDescription = null,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    "Premium user",
                                    fontSize = TextUnit(2.3f, TextUnitType.Em),
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            } else {
                                Icon(
                                    Icons.TwoTone.Star, contentDescription = null,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    "Basic user",
                                    fontSize = TextUnit(2.3f, TextUnitType.Em),
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }

                        }
                    }
                },
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier.padding(contentPadding)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { item ->
                    EventTile(item)
                }
            }
        }
    }

}
