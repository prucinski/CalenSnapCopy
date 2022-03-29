package com.example.ocrhotel

import android.util.Log
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OperationStatusCodes
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer


class OCRAzureREST {
    var results: ReadOperationResult? = null
    var resultsText: String? = null

        private set

    private fun postFile(data: ByteArray, callback: Callback): Call {
        val request = Request.Builder()
            .addHeader("Content-Type", "application/octet-stream")
            .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
            .url(POST_URLBase)
            .post(data.toRequestBody(MEDIA_TYPE_FILE))
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    /**
     * Reads text from raw file data using the Azure API.
     * @param data ByteArray to be received.
     * Supported image formats: JPEG, PNG, BMP, PDF and TIFF.
     * For the free tier, only the first 2 pages are processed. File size less than 50MB (4MB for the free tier).
     * After that, get the ReadOperationResults results variable in order to process it.
     */
    fun getImageTextData(data: ByteArray, callback: Consumer<String?>, failureCallback: Consumer<IOException>) {
        postFile(data, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w("OCRAzureREST", "Callback in onFailure() failed.")
                failureCallback.accept(e)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.w("OCRAzureREST", "Callback in onResponse() failed.")
                    val e = IOException("Unexpected code $response ,${response.message}")
                    failureCallback.accept(e)
                    throw e
                }
                val operationId = extractOperationIdFromOpLocation(response.headers["operation-location"])
                val vision = compVisClient.computerVision() as ComputerVisionImpl
                var pollForResult = true
                while (pollForResult) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    results = vision.getReadResult(UUID.fromString(operationId))
                    val status = results?.status()
                    if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                        pollForResult = false
                    }
                }
                extractText()
                Log.w("OCRAzureRest", "text extracted. Attempting to callback...")
                callback.accept(resultsText)
                Log.w("OCRAzureRest", "callback accepted.")
            }
        })
    }

    private fun post(json: String, callback: Callback): Call {
        val body = json.toRequestBody(MEDIA_TYPE_URL)
        val request: Request = Request.Builder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
            .url(POST_URLBase)
            .post(body)
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    /**
     * Processes raw file data from URL.
     *
     * @param url URL of the file to be received
     * After that, get the ReadOperationResults results variable in order to process it.
     * Note: It is only here for testing purposes, don't mind it.
     */
    fun getImageTextDataFromURL(url: String?, callback: Consumer<String?>) {
        post("{\"url\":\"$url\"}", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val operationId = extractOperationIdFromOpLocation(
                    response.headers["operation-location"]
                )
                val vision = compVisClient.computerVision() as ComputerVisionImpl
                var pollForResult = true
                while (pollForResult) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    results = vision.getReadResult(UUID.fromString(operationId))
                    val status = results?.status()
                    if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                        pollForResult = false
                    }
                }
                extractText()
                callback.accept(resultsText)
            }
        })
    }

    private fun extractText() {
        // Print read results, page per page
        val builder = StringBuilder()
        for (pageResult in results!!.analyzeResult().readResults()) {
            for (line in pageResult.lines()) {
                builder.append(line.text()+" ")
            }
        }
        resultsText = builder.toString()
    }

    companion object {
        // TODO: Encapsulate those somehow.
        private const val subscriptionKey = "db0abec60f8c4ea4a4d69cde1102939e"
        private const val endpoint = "https://ocr-app.cognitiveservices.azure.com"
        private const val POST_URLBase = "$endpoint/vision/v3.2/read/analyze"
        private const val GET_URLBase = "$endpoint/vision/v3.2/read/analyzeResults/"
        private val client = OkHttpClient()
        private val MEDIA_TYPE_FILE = "application/octet-stream".toMediaType()
        private val MEDIA_TYPE_URL = "application/json".toMediaType()
        private val compVisClient =
            ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint)

        private fun extractOperationIdFromOpLocation(operationLocation: String?): String {
            if (operationLocation != null && operationLocation.isNotEmpty()) {
                val splits = operationLocation.split("/").toTypedArray()
                if (splits.isNotEmpty()) {
                    return splits[splits.size - 1]
                }
            }
            throw IllegalStateException("Something went wrong: Couldn't extract the operation id from the operation location")
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val file = File("C:\\MEGA\\Images\\78994948_p0.png")
            val url = "https://th.bing.com/th/id/R.89c5a2342fea6af0fa9927eefd992526?rik=JfRVauzSZvXkQA&pid=ImgRaw&r=0"
            val ocrClient = OCRAzureREST()


//            ocrClient.getImageTextDataFromURL(url) { x: String? -> println(x)}

        }
    }
}