package com.example.ocrhotel

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

private val baseUrl = HttpUrl.Builder().scheme("https").host("calensnap-api.herokuapp.com")

private fun path(path: String): HttpUrl {
    return baseUrl.addPathSegment(path).build()
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

private fun post(url: HttpUrl, data: ByteArray, callback: Callback): Call {
    val request = baseRequest(url)
        .post(data.toRequestBody())
        .build()
    return makeCall(request, callback)
}

private fun put(url: HttpUrl, data: ByteArray, callback: Callback): Call {
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

private val client = OkHttpClient()
private val MEDIA_TYPE_URL = "application/json".toMediaType()


fun main(args: Array<String>) {

    get(path("/"), object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            println(response);
        };
    })
}