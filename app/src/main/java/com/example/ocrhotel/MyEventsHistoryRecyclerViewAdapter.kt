package com.example.ocrhotel

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

import com.example.ocrhotel.placeholder.PlaceholderContent.PlaceholderItem
import com.example.ocrhotel.databinding.FragmentEventsHistoryBinding
import java.time.format.DateTimeFormatter


/**
 * [RecyclerView.Adapter] that can display a [Event].
 */
class MyEventsHistoryRecyclerViewAdapter(private val values: List<Event>):
    RecyclerView.Adapter<MyEventsHistoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    return ViewHolder(FragmentEventsHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.composeView.setContent {
            EventTile(
                // item
            )
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentEventsHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val composeView = binding.composeViewEventTile

        override fun toString(): String {
            return super.toString() + " '" + composeView.toString() + "'"
        }
    }

}

// @Preview
@ExperimentalMaterialApi
@Composable
fun EventTile(
    // event : Event
){
    // Test variable because the Preview won't render if it's a function parameter
    val event = Event()

    var expanded by remember {mutableStateOf(false)}
    var delDialog by remember {mutableStateOf(false)}
    // Here we can also make it a gesture detector,
    // e.g. for deleting an event just by long pressing, etc.
    return Card(
        content={
            ListItem(
                icon={
                    Icon(Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        modifier=Modifier.size(40.dp))
                },
                text={Text(event.eventName)},
                secondaryText={
                    val dFormat = DateTimeFormatter.ofPattern("d MMM uuuu")
                    val eventDate = event.eventDateTime.format(dFormat)
                    Text(eventDate + " at " + event.eventHour)
                },
                trailing={
                    IconButton(onClick = {expanded=true}){
                        Icon(Icons.Outlined.Settings,
                            contentDescription="Reschedule event")
                    }

                    // TODO: Modify the values to lead to the respective menus
                    //  and delete etc.
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                    ) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                        }){
                            Text("Reschedule")
                        }
                        DropdownMenuItem(onClick = {
                            expanded = false
                            delDialog = true
                        }){
                            Text("Delete",
                                color=Color.Red
                            )
                        }
                    }
                },
            )
            if(delDialog) AlertDialog(
                onDismissRequest = {delDialog = false},
                title = {
                    Text(text = "Are you sure you want to delete this event?")
                },
                // text = {
                //     Text("Are you sure you want to delete this event?")
                // },
                confirmButton = {
                    Button(
                        onClick = {delDialog = false},
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red,contentColor=Color.White)
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            delDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray,contentColor=Color.White)
                    ) {
                        Text("No")
                    }
                }
            )
        },
        elevation = 1.dp
    )
}
