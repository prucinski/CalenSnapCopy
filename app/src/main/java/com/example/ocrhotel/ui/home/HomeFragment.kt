package com.example.ocrhotel.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ocrhotel.EventTile
import com.example.ocrhotel.MainActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.ocrhotel.*
import com.example.ocrhotel.R
import com.example.ocrhotel.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val model: EventListModel by activityViewModels()
    private var premium = false

    @OptIn(ExperimentalMaterialApi::class, ExperimentalUnitApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val composeView = binding.composeHome

        val m = (activity as MainActivity)
        // If the user is not logged in, they should be redirected to the login page.
        if (!m.loggedIn) {
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.loginFragment)
        }

        premium = m.premiumAccount
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val events by remember {
                    mutableStateOf(model)
                }

                val futureEvents = events.getFutureEvents()
                MaterialTheme {
                    Scaffold(
                        modifier = Modifier.padding(vertical = if (!premium) 55.dp else 0.dp),
                        topBar = { CustomTopAppBar() }
                    ) { contentPadding ->

                        Box(
                            modifier = Modifier.padding(contentPadding)
                        ) {
                            if (futureEvents.isEmpty()) Box(Modifier.padding(5.dp)) { Text("You have no future events yet.") }
                            else
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(items = futureEvents) { event ->
                                        EventTile(event) {
                                            events.removeEvent(event)

                                        }
                                    }
                                }
                        }
                    }
                }

            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

@Preview
@Composable
fun CustomTopAppBar(
    name: String = "John Smith",
    premium: Boolean = false,
    icon: ImageVector = Icons.Rounded.Person,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text("Home")
        },
        actions = {
            IconButton(
                onClick = {
                    expanded = true

                }
            ) {
                Icon(icon, contentDescription = null)
            }
        }
    )
}
