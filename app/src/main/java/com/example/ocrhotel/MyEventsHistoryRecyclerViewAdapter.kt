package com.example.ocrhotel

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import com.example.ocrhotel.databinding.FragmentEventsHistoryBinding

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
                item
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