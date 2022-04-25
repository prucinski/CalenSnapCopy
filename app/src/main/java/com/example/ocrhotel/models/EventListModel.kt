package com.example.ocrhotel.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
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
        eventsList = eventsList.plus(newEvent)
    }

    fun removeEvent(event: Event){
        eventsList = eventsList.minus(event)
    }

    fun modifyEvent(event: Event){
        eventsList = eventsList.map{
            if(it==event){

                // TODO: In this case, open the ModifyEvent fragment, modify it, etc
                it.eventName = "Modified"
                return@map it
            }
            else it
        }
    }
}