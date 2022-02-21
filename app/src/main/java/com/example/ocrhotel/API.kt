package com.example.ocrhotel

import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OperationStatusCodes
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.*



    private fun callAPI(data: ByteArray, callback: Callback): Call {
        val request = Request.Builder()
            .addHeader("Content-Type", "application/json")
            .url("https://calensnap-api.herokuapp.com")
            //.post(data.toRequestBody())
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    private val client = OkHttpClient()
    private val MEDIA_TYPE_URL = "application/json".toMediaType()



fun main(args: Array<String>) {

    callAPI(ByteArray(0), object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            println(response);
        };
    })
}