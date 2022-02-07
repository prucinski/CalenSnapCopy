package com.example.ocrhotel

import android.util.Log
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.lang.Exception

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
                    assertEquals(true,it.contains("09.11"))
                }
        }
    }
    //Needs to have the file be present.
    @Test
    fun ocrWorksWithFile() {
        val file = File(System.getProperty("user.dir").dropLast(4)+"/testImages/test1_1.png")

        client.getImageTextData(file.readBytes(), { s ->
            s?.let { result ->
                assertEquals(true, result.contains("22.02.18"))
            }
        }) { e -> null }
    }
}