@file:OptIn(ExperimentalUnitApi::class, ExperimentalFoundationApi::class)

package com.example.ocrhotel.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ocrhotel.widgets.EventTile
import com.example.ocrhotel.MainActivity
import com.example.ocrhotel.R
import com.example.ocrhotel.databinding.FragmentHomeBinding
import com.example.ocrhotel.models.EventListModel
import com.google.android.material.composethemeadapter.MdcTheme

@ExperimentalMaterialApi
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val model: EventListModel by activityViewModels()
    private val premium = mutableStateOf(false)
    private val business = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val composeView = binding.composeHome
        // If the user is not logged in, they should be redirected to the login page.
        val m = (activity as MainActivity)
        if (!m.loggedIn) {
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.loginFragment)
        }
        premium.value = m.premiumAccount
        business.value = m.businessAccount
        composeHomeView()
        return root
    }

    override fun onResume() {
        super.onResume()
        Log.d("RESUMING", "onResume() called")

        premium.value = (activity as MainActivity).premiumAccount
        business.value = (activity as MainActivity).businessAccount
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun composeHomeView(){
        val composeView = binding.composeHome
        composeView.apply{

            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                Home()
            }
        }

    }

    @Composable
    fun Home(){
        val events by remember {
            mutableStateOf(model)
        }

                MdcTheme {
                    Scaffold(
                        modifier = Modifier
                            .padding(top = if (!premium.value && !business.value) 55.dp else 0.dp)
                        ,
                        topBar = {
                            ProfileScreen()
                        }
                    ) { contentPadding ->

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(contentPadding)

                        ) {
                            Divider(thickness=2.dp)

                            Text(
                                "Upcoming Events",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(5.dp)
                            )
                            Divider(thickness=2.dp)
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(items = events.getFutureEvents()) { event ->
                                    EventTile(event) {
                                        // On press of delete button
                                        events.removeEvent(event)
                                    }
                                }
                            }
                        }
                    }
                }

}

@ExperimentalFoundationApi
@Preview
@Composable
fun ProfileScreen() {
    Column() {
        TopBar(
            name = "Home",
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(4.dp))
        ProfileSection()
    }
}

@Composable
fun TopBar(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            Modifier
                .background(color=MaterialTheme.colors.primary)
                .fillMaxWidth(),
            Alignment.Center,
        ){
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier.padding(10.dp)
            )
        }

    }
}

@Composable
@Preview
fun ProfileSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            RoundImage(
                image = painterResource(id = R.drawable.ic_baseline_person_24),
                modifier = Modifier
                    .size(70.dp)
                    .weight(5f)
            )
            Spacer(modifier = Modifier.width(20.dp))
            ProfileDescription(
                displayName = "John Smith",
                description = "“Pleasure in the job puts perfection in the work.”\n - Aristotle",
            )
        }
    }
}

@Composable
fun RoundImage(
    image: Painter,
    modifier: Modifier = Modifier
) {
    Image(
        painter = image,
        contentDescription = null,
        modifier = modifier
            .aspectRatio(ratio = 1f, matchHeightConstraintsFirst = true)
            .border(
                width = 2.dp,
                color = MaterialTheme.colors.primary,
                shape = CircleShape)

            .padding(3.dp)
            .clip(CircleShape)
    )
}

// @Composable
// fun StatSection(modifier: Modifier = Modifier) {
//     Row(
//         verticalAlignment = Alignment.CenterVertically,
//         horizontalArrangement = Arrangement.SpaceAround,
//         modifier = modifier
//     ) {
//         ProfileStat(numberText = "601", text = "Posts")
//         ProfileStat(numberText = "100K", text = "Followers")
//         ProfileStat(numberText = "72", text = "Following")
//     }
// }
//
// @Composable
// fun ProfileStat(
//     numberText: String,
//     text: String,
//     modifier: Modifier = Modifier
// ) {
//     Column(
//         verticalArrangement = Arrangement.Center,
//         horizontalAlignment = Alignment.CenterHorizontally,
//         modifier = modifier
//     ) {
//         Text(
//             text = numberText,
//             fontWeight = FontWeight.Bold,
//             fontSize = 20.sp
//         )
//         Spacer(modifier = Modifier.height(4.dp))
//         Text(text = text)
//     }
// }

@Composable
fun ProfileDescription(
    displayName: String,
    description: String,
) {
    val letterSpacing = 0.5.sp
    val lineHeight = 20.sp
    Column(
        modifier = Modifier
            // .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .width(220.dp)
    ) {
        Text(
            text = displayName,
            fontWeight = FontWeight.Bold,
            letterSpacing = letterSpacing,
            lineHeight = lineHeight
        )
        Text(
            text = description,
            letterSpacing = letterSpacing,
            lineHeight = lineHeight
        )
    }
}