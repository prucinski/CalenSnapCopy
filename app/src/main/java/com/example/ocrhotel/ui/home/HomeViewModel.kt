package com.example.ocrhotel.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ocrhotel.Event

class HomeViewModel : ViewModel (){
    val eventsList = MutableLiveData<MutableList<Event>>()

    fun addEvent(newEvent: Event){
        eventsList.value?.add(newEvent)
    }

    fun removeEvent(event: Event){
        eventsList.value?.remove(event)
    }
}