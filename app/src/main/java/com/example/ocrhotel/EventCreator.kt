package com.example.ocrhotel

import com.microsoft.azure.cognitiveservices.vision.computervision.models.AnalyzeResults
import java.util.*

const val exampleString =
    "Organised by SPE International Aberdeen Section Well Decommisioning - The Future 20 April 2022, P&J Live SUBMIT YOUR ABSTRACT TODAY!"

/**
 *  TODO
 *  - Handle american style dates, i.e. MM.DD.YYYY
 *  - Handle multiple dates for the same event, e.g. 21-22 April 2022
 */



class EventCreator {

    class EventPlaceholder(public val dateMilliseconds: Long, public val name: String)

    fun extractDates(text: String): MutableList<Long> {
        var result= mutableListOf<Long>()


        return result
    }

    fun createEvent(rawText: String): EventPlaceholder {


        val date = Date().time
        val name = "Placeholder"

        val event = EventPlaceholder(date, name)

        return event
    }

}

fun main() {
    System.out.println("Hello World!")
}