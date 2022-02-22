package com.example.ocrhotel

import android.util.Log
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.lang.Exception
import java.time.LocalDateTime
import java.util.*



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class OCRUnitTest{
    val algorithm = Algorithm()
    var currenttime = ""
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

    @Test
    fun test1(){
        assertEquals("2020-02-03T12:00",algorithm.extractDates("03 02 2020").elementAt(0).toString())
    }
    @Test
    fun test2(){
        assertEquals("2020-02-03T12:00",algorithm.extractDates("03 feb 2020").elementAt(0).toString())
    }
    @Test
    fun test3(){
        assertEquals("2020-02-29T12:02",algorithm.extractDates("29.02.2020").elementAt(0).toString())
        // time 12:02?
    }
    @Test
    fun test4(){
        currenttime = LocalDateTime.now().toString()
        assertEquals(currenttime.dropLast(13),algorithm.extractDates("29.02.2021").elementAt(0).toString().dropLast(13)) // returns current date and time
    }
    @Test
    fun test5(){
        assertEquals("2000-02-29T12:02",algorithm.extractDates("29.02.2000").elementAt(0).toString())
    }

    @Test
    fun test6(){
        currenttime = LocalDateTime.now().toString()
        assertEquals(currenttime.dropLast(13),algorithm.extractDates("30.02.2021").elementAt(0).toString().dropLast(13)) // returns current date and time
    }

    @Test
    fun test7() {
        currenttime = LocalDateTime.now().toString()
        assertEquals(currenttime.dropLast(13),algorithm.extractDates("30.02.2020").elementAt(0).toString().dropLast(13) // returns current date and time
        )
    }
    @Test
    fun test8(){
        currenttime = LocalDateTime.now().toString()
        assertEquals(currenttime.dropLast(13),algorithm.extractDates("30.02.2000").elementAt(0).toString().dropLast(13)) // returns current date and time
    }
    @Test
    fun test9(){
        currenttime = LocalDateTime.now().toString()
        assertEquals(currenttime.dropLast(13),algorithm.extractDates("31.06.2020").elementAt(0).toString().dropLast(13)) // returns current date and time
    }
    @Test
    fun test10(){
        currenttime = LocalDateTime.now().toString()
        assertEquals(currenttime.dropLast(13),algorithm.extractDates("31.06.2020").elementAt(0).toString().dropLast(13)) // returns current date and time
    }
    @Test
    fun test11(){
        assertEquals("2020-01-22T00:01",algorithm.extractDates("00.01.2020").elementAt(0).toString())
    }
    @Test
    fun test12(){
        assertEquals("2004-02-22T22:04",algorithm.extractDates("20-22.04.2020").elementAt(0).toString())
    }
    @Test
    fun test13(){
        assertEquals("2020-04-22T22:04",algorithm.extractDates("22.04.2020 at 20:20").elementAt(0).toString())
    }
    @Test
    fun test14(){
        assertEquals("2020-04-22T22:04",algorithm.extractDates("22.04.2020 at 20:30").elementAt(0).toString())
    }
    @Test
    fun test15(){
        assertEquals("2020-04-22T22:04",algorithm.extractDates("22.04.2020 at 2030").elementAt(0).toString())
    }
    @Test
    fun test16(){
        assertEquals("2020-04-22T22:04",algorithm.extractDates("22.04.2020 at 2020").elementAt(0).toString())
    }
    @Test
    fun test17(){
        assertEquals("2020-04-20T20:04",algorithm.extractDates("20.04.2020 at 18pm").elementAt(0).toString())
    }
}