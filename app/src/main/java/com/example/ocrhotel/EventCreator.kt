package com.example.ocrhotel

import android.app.Activity
import android.util.Log
import android.widget.Toast
import java.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.text.slice
import android.provider.CalendarContract.Events

import android.content.ContentValues
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract


class EventCreator(eventName: String, eventDate: String, eventTime: String, eventDuration: Int = 2, activity: Activity){

    //TODO: Create an object of type CalendarContract and add an element to it. Looks scary!

        //TODO: WORKS ONLY FOR ONE DATE FORMAT FOR NOW
        private val eventName = eventName
        private var activity = activity
        private val year = eventDate.slice(6..9)
        private val month = eventDate.slice(3..4)
        private val day = eventDate.slice(0..1)
        private val hour = eventTime.slice(0..1)
        private val minute = eventTime.slice(3..4)
        private val hourEnd = (hour.toInt() + eventDuration).toString()




     public fun addEvent(){
        Log.e("Y/D/M/H/MIN", "$year $day $month $hour $minute")


         val beginTime = Calendar.getInstance()
         beginTime.set(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt())
         val startMillis = beginTime.timeInMillis

         val endTime = Calendar.getInstance()
         endTime.set(year.toInt(), month.toInt(), day.toInt(), hourEnd.toInt(), minute.toInt())
         val endMillis = endTime.timeInMillis


         //this is choosing a calendar provider, I think. 1 for default calendar? It's complicated
         val calID : Long = 1
         //I have no idea how to access ContentResolver -TODO: HELP! I think it needs to be
         //TODO: accessed from MainActivity

         val values = ContentValues().apply {
             put(Events.DTSTART, startMillis)
             put(Events.DTEND, endMillis)
             put(Events.TITLE, eventName)
             put(Events.DESCRIPTION, "Thank you for using our app")
             put(Events.CALENDAR_ID, calID)
             put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
         }


        val uri: Uri? = activity.contentResolver.insert(Events.CONTENT_URI, values)


         // get the event ID that is the last element in the Uri
         //val eventID: Long = uri.lastPathSegment.toLong()
        //
        // ... do something with event ID
        // TODO: VERY LATE ONE - YOU CAN ADD SHARING EVENTS WITH OTHERS HERE
        //

    }



}