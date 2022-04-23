package com.example.ocrhotel.models

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter



// Some commented out parts are for possible custom serialization of the class
/**
 * Holds basic properties of an event, e.g. name, time, and duration.
 * */
data class Event(var eventName: String = "Event", var eventDateTime: LocalDateTime = LocalDateTime.now(), var duration: Long = 30) : Parcelable
{
    // Getters for parts of the DateTime variable.
    val eventDate get() : String? = eventDateTime.format(DateTimeFormatter.ofPattern("dd-MM-uuuu"))
    val eventHour get() : String? = eventDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

    /// Constructors for parcelling.
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        LocalDateTime.ofEpochSecond(parcel.readLong(),0, ZoneOffset.UTC),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventName)
        parcel.writeLong(eventDateTime.toEpochSecond(ZoneOffset.UTC))
        parcel.writeLong(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }
}