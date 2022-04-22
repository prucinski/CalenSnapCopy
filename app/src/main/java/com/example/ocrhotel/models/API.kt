package com.example.ocrhotel

import android.app.Activity
import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Helper Functions
 * */

// API endpoint
const val ENDPOINT = "calensnap-api.herokuapp.com"
//const val ENDPOINT = "localhost"

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
private fun baseRequest(url: HttpUrl, jwt: String?): Request.Builder {
    var builder = Request.Builder()
        .addHeader("Content-Type", "application/json")

    if (jwt != null) {
        builder = builder.addHeader("Authorization", "Bearer $jwt")
    }

    return builder.url(url)
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
private fun get(url: HttpUrl, callback: Callback, jwt: String? = null): Call {
    val request = baseRequest(url, jwt)
        .build()
    return makeCall(request, callback)
}

// Post to the specified http url
private fun post(url: HttpUrl, data: String, callback: Callback, jwt: String? = null): Call {
    val request = baseRequest(url, jwt)
        .post(data.toRequestBody())
        .build()
    return makeCall(request, callback)
}

// Delete at the specified http url
private fun delete(url: HttpUrl, callback: Callback, jwt: String? = null): Call {
    val request = baseRequest(url, jwt)
        .delete()
        .build()
    return makeCall(request, callback)
}


open class APIValue {
    val success: Boolean = true
}

class APIProfile : APIValue() {
    val username: String = "Not Logged In"
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
    val title = ""
    val event_time = ""
    val username = ""
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

class APIJWTToken : APIValue() {
    val token = ""
}

/**
 * API functions
 * */

fun login(username: String, password: String, callback: (token: String?) -> Unit) {
    val gson = Gson()
    val payload = gson.toJson(object {
        val password = password
        val username = username
    })
    post(path("login"),
        payload,
        decodeCallback(APIJWTToken::class.java) { result ->
            callback(result?.token)
        })
}

fun createProfile(username: String, password: String, callback: (Boolean) -> Unit) {
    val gson = Gson()

    post(
        path("profile"),
        gson.toJson(object {
            val password = password
            val username = username
        }),
        decodeCallback(APIValue::class.java) { result ->
            if (result != null) {
                callback(result.success)

            } else {
                callback(false)
            }
        })
}

fun readProfile(jwt: String, callback: (APIProfile?) -> Unit) {
    get(
        path("profile"),
        decodeCallback(APIProfile::class.java) { result ->
            callback(result)
        }, jwt
    )
}

fun deleteProfile(jwt: String, callback: (Boolean) -> Unit) {
    delete(path("profile"), validateCallback(callback), jwt)
}


// Create a new event for the user with profileId. This updates both the userevent and the event table.
fun createEvent(
    jwt: String,
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

        val snap_location = object {
            val N = latitude
            val W = longitude
        }
        val snap_time = snapTime
    })

    println(data)

    post(path("events"), data, validateCallback(callback), jwt)
}


// Read all past events for the user with profile.
fun readUserEvents(jwt: String, callback: (events: APIUserEvents?) -> Unit) {
    get(
        path("events"),
        decodeCallback(APIUserEvents::class.java) { callback(it) },
        jwt
    )
}

fun deleteUserEvent(event_id: UUID, jwt: String, callback: (success: Boolean) -> Unit) {
    delete(
        path("events", event_id.toString()),
        decodeCallback(APIValue::class.java) {
            if (it != null) {
                callback(it.success)
            } else {
                callback(false)
            }
        },
        jwt
    )
}

fun readEvents(jwt: String, callback: (APIEvents?) -> Unit) {
    get(
        path("metadata", "events"),
        decodeCallback(APIEvents::class.java) { callback(it) },
        jwt
    )
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
            if (response.code != 200) {
                Log.w("UNSUCCESSFUL HTTP RESPONSE CODE: ", "${response.code}")
            }
            val result = gson.fromJson(data, APIValue::class.java)
            if (result.success) {
                val result = gson.fromJson(data, type)
                callback(result)
            } else {
                callback(null)
            }

        }
    }
}

fun getJwtFromPreferences(context: Context): String? {
    val sh = context.getSharedPreferences(
        context.getString(R.string.preferences_address),
        AppCompatActivity.MODE_PRIVATE
    )

    return sh.getString("JWT", null)
}

fun extractDate(string: String): LocalDateTime {
    return LocalDateTime.parse(string, DateTimeFormatter.RFC_1123_DATE_TIME)
}

/**
 * Testing
 * */


fun main(args: Array<String>) {
    val password = "password1234"

    login("User", password) { token ->
        if (token != null) {
            readProfile(token) { profile ->
                println(profile?.username)
            }

//            readEvents(token) { events ->
//                if (events != null) {
//                    for (event in events.events) {
//                        println(event)
//                    }
//
//                }
//            }

            for (i in 1..10) {

                createEvent(
                    token,
                    "User Event $i",
                    LocalDateTime.now().plusHours(10),
                    LocalDateTime.now().plusHours(10),
                    0.0,
                    0.0
                ) {
                    println("Event creation was successful: $it")
                }
            }
//
//            readUserEvents(token) {
//                if (it != null) {
//                    println("Userevents: ")
//                    for (uevent in it.events) {
//                        print("(${uevent.title}-${uevent.event_time}), ")
//                    }
//                    println()
//                    deleteUserEvent(it.events[0].id, token) {
//                        println("Event deleted successfully: $it")
//                    }
//
//                }
//            }


        }
    }
//
//    createProfile("Business", password) {
//        println(it)
//    }


//    createProfile("User", password) {
//        if (it != null) {
//            readProfile(it, password) { profile ->
//                println(profile?.username)
//            }
//            val time = LocalDateTime.now()
//            createEvent(it, "Test Event", time, time.plusDays(20), 0.0, 0.0) { success ->
//                readUserEvents(it) { events ->
//                    events?.events?.forEach { event ->
//                        println(event.title)
//                    }
//                }
//            }
//
//        }
//    }
//
//    readEvents {
//        it?.events?.forEach { event ->
//            println(event.snap_time)
//        }
//    }
}