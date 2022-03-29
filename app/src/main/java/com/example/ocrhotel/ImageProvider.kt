package com.example.ocrhotel

import android.app.Activity
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.util.*

class ImageProvider(
    resultCaller: ActivityResultCaller,
    private val activity: Activity?,
    private val onImageProvided: (Uri) -> Unit,
) {
    private var tempFileUri: Uri? = null

    private val cameraLauncher =
        resultCaller.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                tempFileUri?.let { onImageProvided(it) }
            }
        }

    private val filePickerLauncher =
        resultCaller.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                onImageProvided(uri)
            }
        }


    fun useCamera() {
        val file = File(activity?.filesDir, "tempPictureFile")
        val a = activity
        a?.let {
            // Get uri for the file that was just created
            val uri = FileProvider.getUriForFile(
                Objects.requireNonNull(a.applicationContext),
                BuildConfig.APPLICATION_ID + ".provider", file
            )
            // Actually launch the camera
            tempFileUri = uri
            cameraLauncher.launch(uri)
        }

    }

    fun useGallery() {

        filePickerLauncher.launch("image/*")
    }
}