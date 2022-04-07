package com.example.ocrhotel

import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.HOURS
import kotlin.math.abs


/**
 *  TODO
 *  - Handle American style dates, i.e. MM.DD.YYYY
 *  - Handle multiple dates for the same event, e.g. 21-22 April 2022
 */

private val months = hashMapOf(
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

class Algorithm {

    /** A subclass that holds the event data.
     Currently only a stub that bundles the event title and date.**/
    class Result(val dateTime: List<LocalDateTime>, val name: String) {
        override fun toString (): String {
            return "$name @ $dateTime"
        }
    }

    private val monthsString = months.keys.joinToString(prefix="\\b", separator = "\\b|\\b", postfix = "\\b")

    // Default expression:
    // Handles everything from DD-MM-YYYY, DD-MMM-YYYY, MM-DD-YYYY, MMM-DD-YYYY
    // along with short date formats (DD MM) and ordinal suffixes (1st, 2nd, etc.)
    // Doesn't work with YYYY-MM-DD.

    // Solution/refactoring for future: Check the region of the device and determine the setting automatically
    // or include it as an option in the settings menu.

    //                                  1  2  3       4  5  6
    // Regex groups are the following: DD.MM.YYYY || MM.DD.YYYY
    private val sep = "./\\- "

    private val dateRegex = Regex("""((?:\d{1,2}-)?\d{1,2})(?:st|nd|rd|th)?[$sep]($monthsString|\d{1,2})(?:[$sep](\d{2,4}))?|($monthsString|\d{1,2})[$sep]((?:\d{1,2}-)?\d{1,2})(?:st|nd|rd|th)?(?:[$sep](\d{2,4}))?""".trimMargin())

    private val timeRegex = Regex("""(\d{1,2})[.:,](\d{1,2})\s?(am|pm)?|(\d{1,2})\s?(am|pm)""")

    fun extractDates(string: String): List<Event> {

        val currentDate = LocalDateTime.now()
        val text = string.lowercase()
            //    Pointless since the OCR cleans the input relatively well
            // .replace("\\s+"," ")


        // Match the OCR text against regex to find all matches
        val matchesDates = dateRegex.findAll(text).toMutableList()

        val matchesTimes = timeRegex.findAll(text).toMutableList()

        // Remove times that have the same signature as dates
        matchesTimes.removeIf{time->matchesDates.any { it.value.contains(time.value) }}

        matchesDates.removeIf{ date->matchesTimes.any { it.value.contains(date.value) }}


        val dates: List<Event> = matchesDates.mapNotNull { match ->
            println("<Date Match> : "+match.groups)

            // Decides whether the format is month first or month second.
            val flag = match.groups[4]?.value == null || match.groups[5]?.value==null

            // Declare the variables at the start.
            var day : Int; var month: Int; var year: Int

            // group at index 1/5 is day; Elvis operator "?:" applies if it evaluates to Null, somehow
            // day = match.groups[if(flag) 1 else 5]?.value?.toIntOrNull() ?: currentDate.dayOfMonth

            // group at index 2/4 is month
            month = match.groups[if(flag) 2 else 4]?.value.let { m ->
                // Check and return if the month is represented as a number.
                // If it's a string, use the map to convert it. If this fails, return current month.
                return@let m?.toIntOrNull() ?: months[m] ?: 0
            }

            // group at index 3/6 is year
            val yearStr = match.groups[if(flag) 3 else 6]?.value ?: currentDate.year.toString()
            year = yearStr.toInt()
            if(yearStr.length == 2)  { // Only the 2 last digits of the year? Then add century.
                year += (currentDate.year / 100) * 100
            }

            val handleMultipleDatesString = match.groups[if(flag) 1 else 5]?.value!!

            // We need to check whether this is an expression containing a range of dates
            if('-' in handleMultipleDatesString) {

                val splitDates = handleMultipleDatesString.split('-')
                day = splitDates.elementAt(0).toInt()

                if ('.' in match.groups[0]!!.value ||
                    ' ' in match.groups[0]!!.value ||
                    '/' in match.groups[0]!!.value ||
                    '\\' in match.groups[0]!!.value ||
                    // We also need to perform a check whether the year string is actually counted
                    match.groups[if (flag) 3 else 6]?.value != null
                ) {

                    day = splitDates.elementAt(0).toInt()
                    val day2 = splitDates.elementAt(1).toInt()

                    val date1 = tryGetDateFromDatelike(year,month,day) ?: return@mapNotNull null
                    val date2 = tryGetDateFromDatelike(year,month,day2) ?: return@mapNotNull null

                    val duration = HOURS.between(date1,date2)

                    return@mapNotNull Event(eventDateTime = date1, duration=duration.toInt())
                }
                // If we've simply caught a date of the type DD-MM-YYYY, we need to take care of a
                // peculiar case/bug where it simply cuts off the Year part and thinks the month is the year.
                else {

                    year = currentDate.year

                    day = splitDates.elementAt(if (flag) 0 else 1).toIntOrNull()
                        ?: currentDate.dayOfMonth

                    month = splitDates.elementAt(if (flag) 1 else 0).toIntOrNull()
                        ?: currentDate.monthValue

                    val ret = tryGetDateFromDatelike(year,month,day) ?: return@mapNotNull null

                    return@mapNotNull Event(eventDateTime = ret)
                }
            }
            else day = handleMultipleDatesString.toInt()

            // Handles US date format.
            // TODO: Handle US format for real.
            //  (Preferably through a setting, although it should make do.)
            if (month > 12) { month=day.also{day=month} }

            val res = tryGetDateFromDatelike(year,month,day) ?: return@mapNotNull null

            return@mapNotNull Event(eventDateTime = res)
        }

        // Constructs the pairs of hour and minute.
        var firstTimeFound : Pair<Int,Int> = Pair(12,0)

        val times: List<Pair<Int,Int>> = matchesTimes.mapIndexed {i, match->
            println("<Time match> : "+match.groups)

            // Same as above, takes the hour from the first matching group if it's of the sort 10:00am,
            // otherwise it assumes something like 10am. ?: defaults the value to 0.
            var hour = match.groups[1]?.value?.toIntOrNull() ?: match.groups[4]?.value?.toInt() ?: 12

            var minute = match.groups[2]?.value?.toIntOrNull() ?: 0

            val ampm = match.groups[3]?.value ?: match.groups[5]?.value

            if (ampm == "pm") hour += 12

            if (hour > 23 || hour < 0 ||  minute > 59 || minute < 0){
                hour = firstTimeFound.first
                minute = firstTimeFound.second
            }

            if(i==0) firstTimeFound = Pair(hour,minute)

            return@mapIndexed Pair(hour,minute)
        }

        // Maps the dates with the times.
        val res = dates.mapIndexed { i, event ->
            // This unpacks the pair. If there is no pair to unpack, it defaults to 12:00pm.
            val (hour, min) = times.elementAtOrElse(i) {firstTimeFound}
            event.eventDateTime = event.eventDateTime.withHour(hour).withMinute(min)
            return@mapIndexed event
        }
        return res
    }

    private fun tryGetDateFromDatelike(year : Int, month: Int, day: Int): LocalDateTime?{
        return try {
            LocalDateTime.of(year,month,day,12,0)
        }
        catch (e:DateTimeException){
            null
        }
    }

    private fun getBoundingBoxArea(boundingBox: List<Double>): Double {
        return abs((boundingBox[0] * boundingBox[3] - boundingBox[1] * boundingBox[2])
                    + (boundingBox[2] * boundingBox[5] - boundingBox[3] * boundingBox[4])
                    + (boundingBox[4] * boundingBox[7] - boundingBox[5] * boundingBox[6])
        ) / 2
    }


    private fun extractTitleFromReadOperationResult(results: ReadOperationResult?): String {
        var maxvalue = 0.0
        var maxvalueText = ""
        for (result in results!!.analyzeResult().readResults()) {
            for (line in result.lines()) {
                val area = getBoundingBoxArea(line.boundingBox())
                if (area > maxvalue) {
                    maxvalue = area
                    maxvalueText = line.text()
                }
                // println(line.text())
                // println(line.boundingBox())
                // println(area)
            }
        }
        return maxvalueText
    }

    fun execute(rawText: String?, results: ReadOperationResult?): List<Event> {
        // This is just a stub so far.
        val dates = extractDates(rawText!!)
        val name = extractTitleFromReadOperationResult(results)

        val ret : MutableList<Event> = mutableListOf()
        for(date in dates){
            ret.add(Event(name,date.eventDateTime,date.duration))
        }

        return ret
    }
}


fun main() {
    // Main function that can be used to test functionality outside of android.
    val algorithm = Algorithm()

    val client = OCRAzureREST()

    val exampleString =
        "Organised by SPE International Aberdeen Section Well Decommissioning - The Future 20-22 April 2022, P&J Live SUBMIT YOUR ABSTRACT TODAY!"

    val url = "https://media.discordapp.net/attachments/903307652224913419/903331486462246922/20211028_181250.jpg"

    val testCases = listOf("10 jan", "03 feb 2020",
        "02-03-2020","3 feb 20","3 feb 2020 10pm",
        "3 feb 2020 10:30","10:00pm 3 feb 2020",
        exampleString,"april 20th, 5 jan",
        "29th february 2022 , 12th feb 2022",
        "20-22 feb 2020")

//    val testCases = client.getImageTextDataFromURL(url){}

    for (test in testCases){
        println("\nStart test \"$test\":")
        for(date in algorithm.extractDates(test)) println(date)
    }

    client.getImageTextDataFromURL(url){it->
        it?.let{
            println(it)
            for(date in algorithm.extractDates(it))
                println(date)
        }
    }


//    for (date in algorithm.extractDates(exampleString)) {
//        println(Instant.ofEpochSecond(date))
//    }

//    val client = OCRAzureREST()
//    client.getImageTextDataFromURL(url){
//        var it = client.results
//        println("\nThis is the final result:"+algorithm.extractTitleFromReadOperationResult(it))
//    }

//    val client = OCRAzureREST()
//    client.getImageTextDataFromURL(url) {
//        println(client.resultsText)
//        val result = algorithm.execute(client.resultsText, client.results);
//        println("\nThis is the final result:" + result.toString())
//    }

}