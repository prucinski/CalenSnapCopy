package com.example.ocrhotel

import androidx.fragment.*
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
import android.provider.Settings.Global.getString
import com.google.common.io.Resources
import java.time.ZoneId


class EventCreator(private val eventArray: List<Event>, private val activity: Activity){

    //boolean as I use the success of the adding action as a means to move to next screen
     public fun addEvent(): Boolean {
        //go through all the confirmed events and add them into the calendar
        for(events in eventArray) {

            //Extract start time in milliseconds (that is the format that is accepted by the calendar)
            val startMillis = events.eventDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            //end time of event.
            val endMillis = events.eventDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + events.duration*60*60*1000;

            //retrieve the calendar ID from shared preferences.
            var sh = activity?.getSharedPreferences("com.example.ocrhotel_preferences.xml", Context.MODE_PRIVATE)
            var calIDstr = sh.getString("calendarID", "-1")
            var calID = calIDstr!!.toLong()
            //Calendar not found in shared prefs, try again. To get to this screen, permissions must have been granted.
            //TODO: BAD COUPLING, THINK OF A FIX.
            if(calID == -1L){
                calID = (activity as MainActivity).getCalendarId()!!
                val editor = sh.edit()
                //sadly these have to be as strings (see SettingsFragment.kt)
                editor.putString("calendarID", calIDstr)
                editor.commit()

            }
            //legacy:
            //try choosing a calendar ID. If null returned, throw exception.
            //val calID: Long = getCalendarId()!!


            val values = ContentValues().apply {
                put(Events.DTSTART, startMillis)
                put(Events.DTEND, endMillis)
                put(Events.TITLE, events.eventName)
                put(Events.DESCRIPTION, "Thank you for using CalenSnap")
                put(Events.CALENDAR_ID, calID)
                put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri: Uri? = activity.contentResolver.insert(Events.CONTENT_URI, values)
            if (uri == null) {
                return false
            }
         }
        return true
         // get the event ID that is the last element in the Uri
         //val eventID: Long = uri.lastPathSegment.toLong()
        //
        // ... do something with event ID
        // TODO: VERY LATE ONE - YOU CAN ADD SHARING EVENTS WITH OTHERS HERE
        //
    }



}