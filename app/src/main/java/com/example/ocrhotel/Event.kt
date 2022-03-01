package com.example.ocrhotel
import java.time.*
import java.time.format.DateTimeFormatter


data class Event(var eventName: String = "Event", var eventDateTime: LocalDateTime = LocalDateTime.now(), var duration: Int = 2) {
    //strings - used only to display the data.
    var eventDate = eventDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    var eventHour = eventDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}