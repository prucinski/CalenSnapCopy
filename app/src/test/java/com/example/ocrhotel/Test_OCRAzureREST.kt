package com.example.ocrhotel

import android.util.Log
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.lang.Exception
import java.util.*



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class OCRUnitTest{

    @Test
    fun ocrWorksWithURL() {
        val client = OCRAzureREST()
        val url = "https://i.imgur.com/FEiKUeh.jpg"
        var stringToCheck = ""
        var reached = false

        client.getImageTextDataFromURL(url){
                it -> it?.let {
            stringToCheck = it
            reached = true
            }
        }
        //Test passes if result is returned within 5 seconds.
        Thread.sleep(5000);
        assertEquals(true, stringToCheck.contains("09.23"))

    }
    //Needs to have the file be present. Runs in batches of 5. This is the easiest difficulty level.
    @Test
    fun ocrWorksWithFile_level1() {
        val client = OCRAzureREST()
        var stringToCheck = ""
        //decided that ctrl c + ctrl v is easier.
        var file = File(System.getProperty("user.dir").dropLast(4)+"/testImages/test1_1.jpg")
        client.getImageTextData(file.readBytes(), { s -> s?.let { stringToCheck = s }}) { e -> null }
        //contains("22.02.18")
        Log.i("", "Testing poster 1_1...")
        Thread.sleep(5000)
        assertEquals(true, stringToCheck.contains("22.02.18"))
        assertEquals( false, stringToCheck.contains("2125.12.1221"))

        file = File(System.getProperty("user.dir").dropLast(4)+"/testImages/test1_2.jpg")
        client.getImageTextData(file.readBytes(), { s -> s?.let { stringToCheck = s }}) { e -> null }
        //contains 23.02.2022 and 13:00
        Log.i("", "Testing poster 1_2...")
        Thread.sleep(5000)
        assertEquals(true, stringToCheck.contains("23.02.2022"))
        assertEquals( true, stringToCheck.contains("13:00"))

        file = File(System.getProperty("user.dir").dropLast(4)+"/testImages/test1_3.jpg")
        client.getImageTextData(file.readBytes(), { s -> s?.let { stringToCheck = s }}) { e -> null }
        Log.i("", "Testing poster 1_3...")
        //contains 18/3/20 and 7PM
        Thread.sleep(5000)
        assertEquals(true, stringToCheck.contains("18/3/20"))
        assertEquals( true, stringToCheck.contains("7PM"))

        file = File(System.getProperty("user.dir").dropLast(4)+"/testImages/test1_4.jpg")
        client.getImageTextData(file.readBytes(), { s -> s?.let { stringToCheck = s }}) { e -> null }
        Log.i("", "Testing poster 1_4...")
        //13 SEPTEMBER 2014
        Thread.sleep(5000)
        Log.i("", stringToCheck)
        assertEquals(true, stringToCheck.contains("13 SEPTEMBER 2014"))
        assertEquals(false, stringToCheck.contains("unacceptable nonsense"))

        file = File(System.getProperty("user.dir").dropLast(4)+"/testImages/test1_5.jpg")
        client.getImageTextData(file.readBytes(), { s -> s?.let { stringToCheck = s }}) { e -> null }
        Log.i("", "Testing poster 1_5...")
        //09.23
        Thread.sleep(5000)
        assertEquals(true, stringToCheck.contains("09.23"))
        assertEquals(false, stringToCheck.contains("unacceptable nonsense"))

    }
    @Test
    fun testFunction(){
        assertEquals(2, 2)
    }
}