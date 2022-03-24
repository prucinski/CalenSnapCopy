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
import java.time.ZoneId


class EventCreator(private val eventArray: List<Event>, private val activity: Activity){


    //resolve the primary calendar ID that the user has.
    private fun getCalendarId() : Long? {

        //via https://stackoverflow.com/questions/16242472/retrieve-the-default-calendar-id-in-android

        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

        var calCursor = activity.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.IS_PRIMARY + "=1",
            null,
            CalendarContract.Calendars._ID + " ASC"
        )

        if (calCursor != null && calCursor.count <= 0) {
            calCursor = activity.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                CalendarContract.Calendars._ID + " ASC"
            )
        }

        if (calCursor != null) {
            if (calCursor.moveToFirst()) {
                val calName: String
                val calID: String
                val nameCol = calCursor.getColumnIndex(projection[1])
                val idCol = calCursor.getColumnIndex(projection[0])
                calName = calCursor.getString(nameCol)
                calID = calCursor.getString(idCol)
                Log.d("CAL","Calendar name = $calName Calendar ID = $calID")
                val helloTutorial = Toast.makeText(activity.applicationContext, "Event is created at this calendar: $calName", Toast.LENGTH_SHORT)
                helloTutorial.show()

                calCursor.close()
                return calID.toLong()
            }
        }
        return null
    }

    //boolean as I use the success of the adding action as a means to move to next screen
     public fun addEvent(): Boolean {
        //go through all the confirmed events and add them into the calendar
        for(events in eventArray) {

            //Extract start time in milliseconds (that is the format that is accepted by the calendar)
            val startMillis = events.eventDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            //end time of event.
            val endMillis = events.eventDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + events.duration*60*60*1000;

            //try choosing a calendar ID. If null returned, throw exception.
            val calID: Long = getCalendarId()!!


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