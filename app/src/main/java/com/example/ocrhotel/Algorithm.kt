package com.example.ocrhotel

import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import java.time.*
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
    private val sep = " ./\\-"

    private val dateRegex = Regex("""((?:\d{1,2}-)?\d{1,2})(?:st|nd|rd|th)?[$sep]($monthsString|\d{1,2})(?:[$sep](\d{2,4}))?|($monthsString|\d{1,2})[$sep]((?:\d{1,2}-)?\d{1,2})(?:st|nd|rd|th)?(?:[$sep](\d{2,4}))?""".trimMargin())

    private val timeRegex = Regex("""(\d{1,2})[.:,](\d{1,2})\s?(am|pm)?|(\d{1,2})(am|pm)""")

    fun extractDates(string: String): List<LocalDateTime> {

        val text = string.lowercase()

        // Match the OCR text against regex to find all matches
        val matches = dateRegex.findAll(text).toMutableList()

        val matchesTimes = timeRegex.findAll(text).toMutableList()

        val currentDate = LocalDateTime.now()

        var additionalDates = mutableListOf<Triple<Int,Int,Int>>()

        // Convert matches into Triple objects containing the values for year, month, and day.
        // Why Triple and not just LocalDate? I reckon the former is faster for our intent which is an intermediate value.
        val dates: List<Triple<Int,Int,Int>> = matches.mapNotNull { match ->
            println(match.groups)

            // Flag helps differentiate between one type of date and the other, e.g. Feb 21st and 21st Feb,
            // those are two separate matching groups in the regex. Yes, the flag is boolean.
            val flag = match.groups[4]?.value == null || match.groups[5]?.value==null

            // Declare the variables at the start.
            var day = currentDate.dayOfMonth;
            var month: Int; var year: Int

            // group at index 1/5 is day; Elvis operator "?:" applies if it evaluates to Null, somehow
            // day = match.groups[if(flag) 1 else 5]?.value?.toIntOrNull() ?: currentDate.dayOfMonth


            // group at index 2/4 is month
            month = match.groups[if(flag) 2 else 4]?.value.let { m ->
                // Check and return if the month is represented as a number.
                // If it's a string, use the map to convert it. If this fails, return current month.
                return@let m?.toIntOrNull() ?: months[m] ?: currentDate.monthValue
            } // Assures the compiler this will not be null since it's somehow not convinced.


            val yearStr = match.groups[if(flag) 3 else 6]?.value ?: currentDate.year.toString()
            year = yearStr.toInt() // group at index 3/6 is year
            if(yearStr.length == 2)  { // Only the 2 last digits of the year? Then add century.
                year += (currentDate.year/100)*100 // This will work long after Java is deprecated :)
            }

            val handleMultipleDatesString = match.groups[if(flag) 1 else 5]?.value!!

            // We need to check whether this is an expression containing a range of dates
            if('-' in handleMultipleDatesString) {

                val splitDates = handleMultipleDatesString.split('-')
                day = splitDates?.elementAt(0).toInt()

                if ('.' in match.groups[0]!!.value ||
                    ' ' in match.groups[0]!!.value ||
                    '/' in match.groups[0]!!.value ||
                    '\\' in match.groups[0]!!.value ||
                    // We also need to perform a check whether the year string is actually counted
                    match.groups[if (flag) 3 else 6]?.value != null
                ) {

                    day = splitDates?.elementAt(0).toInt()
                    val day2 = splitDates?.elementAt(1).toInt()

                    for(i in day..day2){
                        additionalDates.add(Triple(year,month,i))
                    }
                    return@mapNotNull null
                }
                // If we've simply caught a date of the type DD-MM-YYYY, we need to take care of a
                // peculiar case/bug where it simply cuts off the Year part and thinks the month is the year.
                else {

                    year = currentDate.year

                    day = splitDates?.elementAt(if (flag) 0 else 1)?.toIntOrNull()
                        ?: currentDate.dayOfMonth

                    month = splitDates?.elementAt(if (flag) 1 else 0)?.toIntOrNull()
                        ?: currentDate.monthValue

                    return@mapNotNull Triple(year, month, day)
                }
            }
            else day = handleMultipleDatesString.toInt()


            // Handles US date format.
            // TODO: Handle US format for real.
            //  (Preferably through a setting, although it should be working rn. This check is honestly unnecessary.)
            // if (month > 12) {month=day.also{day=month}}

            // Some checks to remove invalid dates.
            // Those could be more sophisticated, e.g. checking for day based on the month (no 29th in Feb except for leap years etc.)
            // TODO: Probably discard the dates altogether if they are invalid.
            if (year > currentDate.year+2) year = currentDate.year
            if (month > 12 || month < 1) month = currentDate.monthValue
            if (day > 31 || day < 1) day = currentDate.dayOfMonth

            return@mapNotNull Triple(year,month,day)
        }
        additionalDates.addAll(dates);

        // Constructs the pairs of hour and minute.
        val times: List<Pair<Int,Int>> = matchesTimes.map { match ->

            // Same as above, takes the hour from the first matching group if it's of the sort 10:00am,
            // otherwise it assumes something like 10am. ?: defaults the value to 0.
            var hour = match.groups[1]?.value?.toInt() ?: match.groups[4]?.value?.toInt() ?: 12

            var minute = match.groups[2]?.value?.toInt() ?: 0

            val ampm = match.groups[3]?.value ?: match.groups[5]?.value

            if (ampm == "pm") hour += 12

            if(hour > 24 || hour < 0) hour = 12
            if(minute > 60 || minute < 0) minute = 0

            return@map Pair(hour,minute)
        }

        // Maps the dates with the times.
        // TODO: This doesn't seem right.
        val res = additionalDates.mapIndexed { i, (y, m, d) ->
                // This unpacks the pair. If there is no pair to unpack, it defaults to 12:00pm.
                val (hour, min) = times.elementAtOrElse(i) { Pair(12, 0) }
                try {
                    return@mapIndexed LocalDateTime.of(y, m, d, hour, min)
                }
                catch(e: DateTimeException) {return@mapIndexed currentDate}
        }
        return res
    }

    private fun getBoundingBoxArea(boundingBox: List<Double>): Double {
        return abs(
            (boundingBox[0] * boundingBox[3] - boundingBox[1] * boundingBox[2])
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

    fun execute(rawText: String?, results: ReadOperationResult?): Result {
        // This is just a stub so far.
        val date = extractDates(rawText!!)
        val name = extractTitleFromReadOperationResult(results)
        return Result(date, name)
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