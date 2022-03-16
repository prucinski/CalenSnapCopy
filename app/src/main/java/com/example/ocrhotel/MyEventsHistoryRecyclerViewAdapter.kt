package com.example.ocrhotel

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

import com.example.ocrhotel.placeholder.PlaceholderContent.PlaceholderItem
import com.example.ocrhotel.databinding.FragmentEventsHistoryBinding


/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
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
            EventTile(item)
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


@ExperimentalMaterialApi
@Composable
fun EventTile(
    event : Event
){
    // val event : Event = Event()
    return ListItem(
        text={Text(event.eventName)}
    )
}
