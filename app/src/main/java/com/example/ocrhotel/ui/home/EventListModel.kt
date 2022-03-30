package com.example.ocrhotel.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.example.ocrhotel.*
import com.example.ocrhotel.placeholder.PlaceholderContent
import java.time.LocalDateTime

class EventListModel : ViewModel() {
    var eventsList by mutableStateOf<List<Event>>(listOf())

    fun getFutureEvents(): List<Event>{
        val currentTime = LocalDateTime.now()
        return eventsList.filter{
            it.eventDateTime.isAfter(currentTime)
        }
    }

    fun getPastEvents(): List<Event>{
        val currentTime = LocalDateTime.now()
        return eventsList.filter{it.eventDateTime.isBefore(currentTime)}
    }

    fun addEvent(newEvent: Event){
        // TODO: Adding of an event via the database needs to be added here.
        eventsList = eventsList.plus(newEvent)
    }

    fun removeEvent(event: Event){
        // TODO: Removal of an event via the database needs to be added here.
        eventsList = eventsList.minus(event)
    }

    fun modifyEvent(event: Event){
        eventsList = eventsList.map{
            if(it==event){

                // TODO: In this case, open the ModifyEvent fragment, modify it,
                    //  update it in the database, and go back here.
                it.eventName = "Modified"
                return@map it
            }
            else it
        }
    }
}