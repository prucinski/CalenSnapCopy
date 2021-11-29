package com.example.ocrhotel

import java.time.*
import java.util.*

const val exampleString =
    "Organised by SPE International Aberdeen Section Well Decommisioning - The Future 20 April 2022, P&J Live SUBMIT YOUR ABSTRACT TODAY!"

/**
 *  TODO
 *  - Handle American style dates, i.e. MM.DD.YYYY
 *  - Handle multiple dates for the same event, e.g. 21-22 April 2022
 *  - Handle cases where year is omitted
 */

val months = hashMapOf(
    "january" to 1,
    "february" to 2,
    "march" to 3,
    "april" to 4,
    "may" to 5,
    "june" to 6,
    "july" to 7,
    "august" to 8,
    "september" to 9,
    "october" to 10,
    "november" to 11,
    "december" to 12,
    "jan" to 1,
    "feb" to 2,
    "mar" to 3,
    "apr" to 4,
    "may" to 5,
    "jun" to 6,
    "jul" to 7,
    "aug" to 8,
    "sep" to 9,
    "oct" to 10,
    "nov" to 11,
    "dec" to 12
)

class EventCreator {

    class EventPlaceholder(public val dateMilliseconds: Long, public val name: String)

    private val monthsString = months.keys.joinToString(separator = "|")
    private val regex = Regex("""(\d\d)[/ .-]($monthsString|\d?\d)[/ .-]((?:\d\d)?\d\d|)""")

    fun extractDates(text: String): Sequence<Long> {
        val matches = regex.findAll(text.lowercase())
        val dates: Sequence<LocalDateTime> = matches.map { match ->
            val day = match.groups[1]?.value?.toInt()
            val month = match.groups[2]?.value?.let { m ->
                var res = m.toIntOrNull()
                if (res == null) {
                    res = months.get(m)
                }
                res
            }
            val year = match.groups[3]?.value?.toInt()
            if (year == null || month == null || day == null) {
            } else {
                // TODO: Handle times
                return@map LocalDateTime.of(year, month, day, 0, 0, 0)
            }
            return@map LocalDateTime.now()
        }
        return dates.map { it ->
            it.atZone(ZoneId.systemDefault()).toEpochSecond()
        }

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

    for (date in creator.extractDates(exampleString)) {
        println(Instant.ofEpochSecond(date))
    }
}