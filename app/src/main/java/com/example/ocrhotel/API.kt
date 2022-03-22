package com.example.ocrhotel

import com.google.gson.Gson
import com.google.gson.JsonObject
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

// API endpoint
const val ENDPOINT = "calensnap-api.herokuapp.com"
private val client = OkHttpClient()


// Assembles a url path out of an arbitrary number of string fragments, joined together with '/'. The Endpoint is set as specified above.
private fun path(vararg paths: String): HttpUrl {
    val baseUrl = HttpUrl.Builder().scheme("https").host(ENDPOINT)
    for (path in paths) {
        baseUrl.addPathSegment(path)
    }
    return baseUrl.build()
}

// Constructs a basic HTTP request
private fun baseRequest(url: HttpUrl): Request.Builder {
    return Request.Builder()
        .addHeader("Content-Type", "application/json")
        .url(url)
}

private fun toPasswordPayload(password: String): String {
    val gson = Gson()
    return gson.toJson(object {
        val password = password
    })
}


// Makes the HTTP call described by req
private fun makeCall(req: Request, callback: Callback): Call {
    val call = client.newCall(req)
    call.enqueue(callback)
    return call
}

// Get from the specified http url
private fun get(url: HttpUrl, callback: Callback): Call {
    val request = baseRequest(url)
        .build()
    return makeCall(request, callback)
}

// Post to the specified http url
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


// Delete at the specified http url
private fun delete(url: HttpUrl, callback: Callback): Call {
    val request = baseRequest(url)
        .delete()
        .build()
    return makeCall(request, callback)
}


open class APIValue {
    val success: Boolean = true
}

class APIProfile : APIValue() {
    val id: UUID = UUID(0, 0)
    val username: String = ""
    val remaining_free_uses: Int = 0
    val premium_user: Boolean = false
    val business_user: Boolean = false
    val duration_in_mins: Int = 0
    val mm_dd: Boolean = false
    val darkmode: Boolean = false
}

class APICoordinates : APIValue() {
    val N: Double = 0.0
    val W: Double = 0.0

    override fun toString(): String {
        return "${N}N, ${W}W"
    }
}

class APIUserEvent : APIValue() {
    val id: UUID = UUID(0, 0)
    val title: String = ""
    val event_time: String = ""
    val userid: UUID = UUID(0, 0)
}

class APIUserEvents : APIValue() {
    val events: Array<APIUserEvent> = arrayOf()
}

class APIEvent : APIValue() {
    val id: UUID = UUID(0, 0)
    val snap_time: String = ""
    val snap_location: APICoordinates = APICoordinates()
    override fun toString(): String {
        return "(ID: $id) $snap_time $snap_location"
    }
}

class APIEvents : APIValue() {
    val events: Array<APIEvent> = arrayOf()
}

class APIUserID : APIValue() {
    val profile_id: UUID = UUID(0, 0)
}

/**
 * API functions
 * */


fun createProfile(username: String, password: String, callback: (profileId: UUID?) -> Unit) {
    val gson = Gson()

    post(
        path("profile"),
        gson.toJson(object {
            val password = password
            val username = username
        }),
        decodeCallback(APIUserID::class.java) { result ->
            callback(result?.profile_id)
        })
}

fun readProfile(profileId: UUID, password: String, callback: (APIProfile?) -> Unit) {
    get(
        path("profile", profileId.toString()),
        decodeCallback(APIProfile::class.java) { result ->
            callback(result)
        })
}

fun deleteProfile(profileId: UUID, callback: (Boolean) -> Unit) {
    delete(path("profile", profileId.toString()), validateCallback(callback))
}


// Create a new event for the user with profileId. This updates both the userevent and the event table.
fun createEvent(
    profileId: UUID,
    title: String,
    eventTime: LocalDateTime,

    snapTime: LocalDateTime,
    latitude: Double,
    longitude: Double,
    callback: (Boolean) -> Unit
) {
    val time = Timestamp.from(eventTime.toInstant(ZoneOffset.UTC)).toString()
    val gson = Gson()
    val data = gson.toJson(object {
        val title = title
        val event_time = eventTime.toString()
        val userid = profileId.toString()

        val snap_location = object {
            val N = latitude
            val W = longitude
        }
        val snap_time = snapTime
    })

    println(data)

    post(path("events", profileId.toString()), data, validateCallback(callback))
}


// Read all past events for the user with profile.
fun readUserEvents(profileId: UUID, callback: (events: APIUserEvents?) -> Unit) {
    get(
        path("events", profileId.toString()),
        decodeCallback(APIUserEvents::class.java) { callback(it) })
}


fun readEvents(callback: (APIEvents?) -> Unit) {
    get(
        path("metadata", "events"),
        decodeCallback(APIEvents::class.java) { callback(it) })
}

private val ignoreCallback = object : Callback {
    override fun onFailure(call: Call, e: IOException) {
        throw e;
    }

    override fun onResponse(call: Call, response: Response) {

    }
}

private fun validateCallback(callback: (Boolean) -> Unit): Callback {
    return object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(false)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) callback(true) else callback(false)
        }
    }
}

// Decodes the json body of the response
private fun <T> decodeCallback(
    type: Class<T>,
    callback: (T?) -> Unit
): Callback where T : APIValue {
    return object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            println("Failure: $call")
            callback(null)
        }

        override fun onResponse(call: Call, response: Response) {
            println(response)
            val gson = Gson()
            val data = response.body?.string()
            val result = gson.fromJson(data, type)
            println("success: ${result.success}")
            println("Response: $result")
            if (result.success) {
                callback(result)
            } else {
                callback(null)
            }

        }
    }
}

/**
 * Testing
 * */


fun main(args: Array<String>) {
    val password = "password1234"
    createProfile("Erik2", password) {
        if (it != null) {
            readProfile(it, password) { profile ->
                println(profile?.username)
            }
            val time = LocalDateTime.now()
            createEvent(it, "Test Event", time, time.plusDays(20), 0.0, 0.0) { success ->
                println(success)
                readUserEvents(it) { events ->
                    events?.events?.forEach { event ->
                        println(event.title)
                    }
                }
            }
        }
    }

    readEvents {
        it?.events?.forEach { event ->
            println(event.snap_time)
        }
    }
}