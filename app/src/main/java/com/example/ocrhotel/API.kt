package com.example.ocrhotel

import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

/**
 * Helper Functions
 * */

private val client = OkHttpClient()

private fun path(vararg paths: String): HttpUrl {
    val baseUrl = HttpUrl.Builder().scheme("https").host("calensnap-api.herokuapp.com")
    for (path in paths) {
        baseUrl.addPathSegment(path)
    }
    return baseUrl.build()
}

private fun baseRequest(url: HttpUrl): Request.Builder {
    return Request.Builder()
        .addHeader("Content-Type", "application/json")
        .url(url)
}


private fun makeCall(req: Request, callback: Callback): Call {
    val call = client.newCall(req)
    call.enqueue(callback)
    return call
}


private fun get(url: HttpUrl, callback: Callback): Call {
    val request = baseRequest(url)
        .build()
    return makeCall(request, callback)
}

private fun post(url: HttpUrl, data: String, callback: Callback): Call {
    val request = baseRequest(url)
        .post(data.toRequestBody())
        .build()
    return makeCall(request, callback)
}

private fun put(url: HttpUrl, data: String, callback: Callback): Call {
    val request = baseRequest(url)
        .put(data.toRequestBody())
        .build()
    return makeCall(request, callback)
}

private fun delete(url: HttpUrl, callback: Callback): Call {
    val request = baseRequest(url)
        .delete()
        .build()
    return makeCall(request, callback)
}


/**
 * API functions
 * */

// Create a new event for the user with profileId.
fun createEvent(
    profileId: UUID,
    eventTime: LocalDateTime,
    latitude: Double,
    longitude: Double,
    callback: Callback
) {
    val time = Timestamp.from(eventTime.toInstant(ZoneOffset.UTC)).toString()
    val data: String = """
    {
        "event_location": {
            "N": ${latitude},
            "W": ${longitude}
        },
        "event_time": "$time"
    }
    """

    post(path("events", profileId.toString()), data, callback)
}

// Read all past events for the user with profile.
fun readEvents(profileId: UUID) {
    get(path(""))
}


/**
 * Testing
 * */

fun main(args: Array<String>) {

    val printCallback = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            println(response);
        }
    }
    createEvent(UUID.randomUUID(), LocalDateTime.now(), 0.0, 0.0, printCallback)

    get(path(""), printCallback)
    post(path(""), "", printCallback)
}