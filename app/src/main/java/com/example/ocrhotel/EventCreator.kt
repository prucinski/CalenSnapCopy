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


class EventCreator(eventName: String, eventDate: String, eventTime: String, eventDuration: Int = 2, activity: Activity){



        //TODO: WORKS ONLY FOR ONE DATE FORMAT FOR NOW - this will be handled in ModifyEvent.
        private val eventName = eventName
        private var activity = activity
        private val year = eventDate.slice(6..9)
        private val month = eventDate.slice(3..4)
        private val day = eventDate.slice(0..1)
        private val hour = eventTime.slice(0..1)
        private val minute = eventTime.slice(3..4)
        private val hourEnd = (hour.toInt() + eventDuration).toString()


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
        Log.e("Y/D/M/H/MIN", "$year $day $month $hour $minute")


         val beginTime = Calendar.getInstance()
         beginTime.set(year.toInt(), month.toInt() - 1, day.toInt(), hour.toInt(), minute.toInt())
         val startMillis = beginTime.timeInMillis

         val endTime = Calendar.getInstance()
         endTime.set(year.toInt(), month.toInt() - 1, day.toInt(), hourEnd.toInt(), minute.toInt())
         val endMillis = endTime.timeInMillis


         //try choosing a calendar ID. If null returned, throw exception.
         val calID : Long = getCalendarId()!!


         val values = ContentValues().apply {
             put(Events.DTSTART, startMillis)
             put(Events.DTEND, endMillis)
             put(Events.TITLE, eventName)
             put(Events.DESCRIPTION, "Thank you for using CalenSnap")
             put(Events.CALENDAR_ID, calID)
             put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
         }

        val uri: Uri? = activity.contentResolver.insert(Events.CONTENT_URI, values)
         if (uri != null) {
             return true
         }
        return false

         // get the event ID that is the last element in the Uri
         //val eventID: Long = uri.lastPathSegment.toLong()
        //
        // ... do something with event ID
        // TODO: VERY LATE ONE - YOU CAN ADD SHARING EVENTS WITH OTHERS HERE
        //

    }



}