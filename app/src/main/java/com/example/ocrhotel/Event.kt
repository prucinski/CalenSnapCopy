package com.example.ocrhotel
// import android.os.Parcel
// import android.os.Parcelable
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



// Some commented out parts are for possible custom serialization of the class
/**
 * Holds basic properties of an event, e.g. name, time, and duration.
 * */
data class Event(var eventName: String = "Event", var eventDateTime: LocalDateTime = LocalDateTime.now(), var duration: Long = 30) : Serializable
// ,Parcelable
     {
    //strings - used only to display the data.
    var eventDate = eventDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    var eventHour = eventDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    //
    // constructor(parcel: Parcel) : this(
    //     parcel.readString()!!,
    //     LocalDateTime.parse(parcel.readString()),
    //     parcel.readInt()
    // ) {
    //     eventDate = parcel.readString()
    //     eventHour = parcel.readString()
    // }
    //
    // override fun writeToParcel(parcel: Parcel, flags: Int) {
    //     parcel.writeString(eventName)
    //     parcel.writeInt(duration)
    //     parcel.writeString(eventDateTime.toString())
    //     parcel.writeString(eventDate)
    //     parcel.writeString(eventHour)
    // }
    //
    // override fun describeContents(): Int {
    //     return 0
    // }
    //
    // companion object CREATOR : Parcelable.Creator<Event> {
    //     override fun createFromParcel(parcel: Parcel): Event {
    //         return Event(parcel)
    //     }
    //
    //     override fun newArray(size: Int): Array<Event?> {
    //         return arrayOfNulls(size)
    //     }
    // }
}