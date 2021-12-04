package com.example.ocrhotel

import android.util.Log
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class OCRUnitTest{
    private val client = OCRAzureREST()
    @Test
    fun ocrWorksWithURL() {
        val url = "https://i.imgur.com/FEiKUeh.jpg"
        client.getImageTextDataFromURL(url){
                it -> it?.let {
                    assertEquals("09.11",it.contains("09.11"))
                }
        }
    }

    // Needs to have the file be present.
    @Test
    fun ocrWorksWithFile() {
        val file = File("C:\\Users\\matey\\Downloads\\event.jpg")
        client.getImageTextData(file){
                it -> it?.let {
                    assertEquals("09.11",it.contains("09.11"))
                }
        }
    }
}