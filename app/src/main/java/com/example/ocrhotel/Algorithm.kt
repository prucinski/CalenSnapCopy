package com.example.ocrhotel

import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import java.time.*
import java.util.*
import kotlin.math.abs

val testCases = listOf("3 feb 2020", "03 feb 2020", "02-03-2020","3 feb 20","3 feb 2020 10pm","3 feb 2020 10:30","10:00 3 feb 2020");

const val exampleString =
    "Organised by SPE International Aberdeen Section Well Decommissioning - The Future 20 April 2022, P&J Live SUBMIT YOUR ABSTRACT TODAY!"

const val url = "https://images-ext-2.discordapp.net/external/Q8m3jqPr-xJagawHD4guOzmvTojHs7v-qIdTnPtFo-Q/%3Fwidth%3D1270%26height%3D670/https/media.discordapp.net/attachments/903307652224913419/903308950768853042/Well-Decommissioning-Highlight_1024x540_0.png"

/**
 *  TODO
 *  - Handle American style dates, i.e. MM.DD.YYYY
 *  - Handle multiple dates for the same event, e.g. 21-22 April 2022
 *  - Handle cases where year is omitted
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

    class Result(val dateTime: LocalDateTime, val name: String) {
        override fun toString (): String {
            return "$name @ $dateTime"
        }
    }

    private val monthsString = months.keys.joinToString(separator = "|")

    // Default expression:
    // Handles everything from DD-MM-YYYY, DD-MMM-YYYY, MM-DD-YYYY, MMM-DD-YYYY
    // along with short date formats (DD MM) and ordinal suffixes (1st, 2nd, etc.)
    // Doesn't work with YYYY-MM-DD.


    // Solution/refactoring for future: Check the region of the device and determine the setting automatically
    // or include it as an option in the settings menu.
    private val regex_date_def = Regex("""($monthsString|\b\d{1,2}\b)[ ./-]?(\d{1,2})(?:st|nd|rd|th)?[ ./-]?(\b\d{2,4}\b)?|(\d{1,2})(?:st|nd|rd|th)?[ ./-]?($monthsString|\b\d{1,2}\b)[ ./-]?(\b\d{2,4}\b)?""")

    // Handles DD MMM, MMM DD with 1st,2nd, etc. (Deprecated for now since default handles everything)
    // private val regex_date_def_short = Regex("""($monthsString|\b\d{1,2}\b)[ ./-]?(\d{1,2})(?:st|nd|rd|th)?|(\d{1,2})(?:st|nd|rd|th)?[ ./-]?($monthsString|\b\d{1,2}\b)""")

    private val regex_time = Regex("""(\d?\d)[.:,](\d?\d)([am|pm])?""")

    fun extractDates(string: String, regex: Regex=regex_date_def): Sequence<LocalDateTime> {

        val text = string.lowercase()

        // Match the OCR text against regex to find all matches
        val matches = regex.findAll(text)

        val currentDate = LocalDateTime.now()

        // Convert matches into LocalDateTime objects
        val dates: Sequence<LocalDateTime> = matches.map { match ->

            val flag = match.groups[4]?.value == null

            // group at index 2/4 is day
            val day = match.groups[if(flag) 2 else 4]?.value?.toIntOrNull() ?: currentDate.dayOfMonth

            // group at index 1/5 is month
            val month = match.groups[if(flag) 1 else 5]?.value?.let { m ->

                // check if the month is represented as a number
                var res = m.toIntOrNull()
                if (res == null) {
                    // If the month is not a number, check the months HashMap for the corresponding month value
                    res = months[m+1]
                }
                // Handles a really weird mistake in the parsing of a date like '10:00 3 feb 2020'
                if(res==0) res+=1
                res
            }

            val yearStr = match.groups[if(flag) 3 else 6]?.value ?: currentDate.year.toString()
            println(match.groups)
            var year = yearStr?.toInt() // group at index 3/6 is year
            if(yearStr.length == 2)  { // its only the 2 last digits of the year
                year += 2000 // TODO: in 80 years this wont work anymore ;)
            }

            if (year == null || month == null || day == null) {
                return@map currentDate
            } else {
                // TODO: Handle times (hours and minutes)
                return@map LocalDateTime.of(year, month, day, 0, 0, 0)
            }
        }

        // Convert the localDateTimes to unix epoch timestamps
        return dates

    }

    private fun getBoundingBoxArea(boundingBox: List<Double>): Double {
        return abs(
            (boundingBox[0] * boundingBox[3] - boundingBox[1] * boundingBox[2])
                    + (boundingBox[2] * boundingBox[5] - boundingBox[3] * boundingBox[4])
                    + (boundingBox[4] * boundingBox[7] - boundingBox[5] * boundingBox[6])
        ) / 2
    }


    fun extractTitleFromReadOperationResult(results: ReadOperationResult?): String {
        var maxvalue = 0.0
        var maxvalueText = ""
        for (result in results!!.analyzeResult().readResults()) {
            for (line in result.lines()) {
                var area = getBoundingBoxArea(line.boundingBox())
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
        val date = extractDates(rawText!!).elementAt(0) // Just pick the first element for now.
        val name = extractTitleFromReadOperationResult(results)
        return Result(date, name)
    }

}

fun main() {
    // Main function that can be used to test functionality outside of android.
    val algorithm = Algorithm()

    // The intended date here is the third of February

    for (test in testCases){
        println("Start test \"$test\":")
        for (date in algorithm.extractDates(test)){
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