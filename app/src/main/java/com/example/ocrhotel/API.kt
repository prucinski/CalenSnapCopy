package com.example.ocrhotel

import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.sql.Timestamp
import java.time.LocalDateTime
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

class APICoordinates {
    val N: Double = 0.0
    val W: Double = 0.0

    override fun toString(): String {
        return "${N}N, ${W}W"
    }
}

class APIUserEvent {
    val id: UUID = UUID(0, 0)
    val title: String = ""
    val event_time: String = ""
    val userid: UUID = UUID(0, 0)
}

class APIUserEvents {
    val events: Array<APIUserEvent> = arrayOf()
}

class APIEvent {
    val id: UUID = UUID(0, 0)
    val snap_time: String = ""
    val snap_location: APICoordinates = APICoordinates()
    override fun toString(): String {
        return "(ID: $id) $snap_time $snap_location"
    }
}

class APIEvents {
    val events: Array<APIEvent> = arrayOf()
}



// Read all past events for the user with profile.
fun readUserEvents(profileId: UUID, callback: Callback) {
    get(path("events", profileId.toString()), callback)
}

fun <T> decodeCallback(type: Class<T>, callback: (T) -> Unit): Callback {
    return object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            println(response)
            val gson = Gson()
            val data = response.body?.string()
            val result = gson.fromJson(data, type)
            callback(result)
        }
    }
}

/**
 * Testing
 * */


fun main(args: Array<String>) {
    readUserEvents(UUID.randomUUID(), decodeCallback(APIEvents::class.java) { result ->
        for (event in result.events) {
            println("${event.id}, ${event.snap_location}, ${event.snap_time}")
        }
    })
}