package com.example.ocrhotel

import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import java.time.*
import java.util.*
import kotlin.math.abs

const val exampleString = "Organised by SPE International Aberdeen Section Well Decommissioning - The Future 20 April 2022, P&J Live SUBMIT YOUR ABSTRACT TODAY!"

const val url = "https://images-ext-2.discordapp.net/external/Q8m3jqPr-xJagawHD4guOzmvTojHs7v-qIdTnPtFo-Q/%3Fwidth%3D1270%26height%3D670/https/media.discordapp.net/attachments/903307652224913419/903308950768853042/Well-Decommissioning-Highlight_1024x540_0.png"

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

class Algorithm {

    class EventPlaceholder(val dateMilliseconds: Long, val name: String)

    private val monthsString = months.keys.joinToString(separator = "|")
    private val regex = Regex("""(\d\d)[/ .-]($monthsString|\d?\d)[/ .-]((?:\d\d)?\d\d|)""")

    fun extractDates(text: String): Sequence<Long> {

        // Match the OCR text against regex to find all matches
        val matches = regex.findAll(text.lowercase())

        // Convert matches into LocalDateTime objects
        val dates: Sequence<LocalDateTime> = matches.map { match ->
            // group at index 1 is day
            val day = match.groups[1]?.value?.toInt()

            // group at index 2 is month
            val month = match.groups[2]?.value?.let { m ->
                
                // check if the month is represented as a number
                var res = m.toIntOrNull()
                if (res == null) {
                    // If the month is not a number, check the months HashMap for the co-responding month value
                    res = months[m]
                }
                res
            }
            val year = match.groups[3]?.value?.toInt() // group at index 3 is year
            if (year == null || month == null || day == null) {
            } else {
                // TODO: Handle times
                return@map LocalDateTime.of(year, month, day, 0, 0, 0)
            }
            return@map LocalDateTime.now()
        }
        // Convert the localDateTimes to unix epoch timestamps
        return dates.map { it ->
            it.atZone(ZoneId.systemDefault()).toEpochSecond()
        }

    }

    private fun getBoundingBoxArea(boundingBox: List<Double>): Double{
        return abs((boundingBox[0]*boundingBox[3]-boundingBox[1]*boundingBox[2])
                + (boundingBox[2]*boundingBox[5]-boundingBox[3]*boundingBox[4])
                + (boundingBox[4]*boundingBox[7]-boundingBox[5]*boundingBox[6])
        )/2
    }


    fun extractTitleFromReadOperationResult(results: ReadOperationResult?): String{
        var maxvalue = 0.0
        var maxvalueText = ""
        for(result in results!!.analyzeResult().readResults()){
            for(line in result.lines()){
                var area = getBoundingBoxArea(line.boundingBox())
                if(area > maxvalue) {
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

    fun createEvent(rawText: String): EventPlaceholder {
        // This is just a stub so far.
        val date = Date().time
        val name = "Placeholder"

        val event = EventPlaceholder(date, name)

        return event
    }

}

fun main() {
    // Main function that can be used to test functionality outside of android.
    val creator = Algorithm()

//    for (date in creator.extractDates(exampleString)) {
//        println(Instant.ofEpochSecond(date))
//    }

    val client = OCRAzureREST()
    client.getImageTextDataFromURL(url){
        var it = client.results
        println("\nThis is the final result:"+creator.extractTitleFromReadOperationResult(it))
    }
}