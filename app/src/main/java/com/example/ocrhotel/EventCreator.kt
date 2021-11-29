package com.example.ocrhotel

import java.util.*

const val exampleString =
    "Organised by SPE International Aberdeen Section Well Decommisioning - The Future 20 April 2022, P&J Live SUBMIT YOUR ABSTRACT TODAY!"

/**
 *  TODO
 *  - Handle American style dates, i.e. MM.DD.YYYY
 *  - Handle multiple dates for the same event, e.g. 21-22 April 2022
 */

val months = arrayOf(
    "january",
    "february",
    "march",
    "april",
    "may",
    "june",
    "july",
    "august",
    "september",
    "october",
    "november",
    "december"
)
val monthsShort =
    arrayOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")

class EventCreator {

    class EventPlaceholder(public val dateMilliseconds: Long, public val name: String)

    fun extractDates(text: String): MutableList<Long> {
        var result = mutableListOf<Long>()

        val monthsString = months.joinToString(prefix = "(", postfix = ")", separator = "|")
        val regex = Regex("""\d\d[/ .]$monthsString[/ .]\d\d\d\d""")

        println(regex.pattern)
        val matches = regex.findAll(text.lowercase())
        for (match in matches) {
            println(match.value)
        }

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
    val creator = EventCreator()

    creator.extractDates(exampleString)
}