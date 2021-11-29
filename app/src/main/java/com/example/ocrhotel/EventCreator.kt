package com.example.ocrhotel

import com.microsoft.azure.cognitiveservices.vision.computervision.models.AnalyzeResults
import java.util.*

class EventCreator {

    class EventPlaceholder(public val dateMilliseconds: Long, public val name: String)

    fun createEvent(rawText: String): EventPlaceholder {


        val date = Date().time
        val name = "Placeholder"

        val event = EventPlaceholder(date, name)

        return event
    }

}