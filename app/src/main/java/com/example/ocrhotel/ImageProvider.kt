package com.example.ocrhotel

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.util.*

class CameraProcessor {


    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            // TODO: handle return value
        }

    public fun takeImage() {
        val file = File(context?.filesDir, "tempPictureFile")
        val a = activity
        a?.let {
            // Get uri for the file that was just created
            val uri = FileProvider.getUriForFile(
                Objects.requireNonNull(a.applicationContext),
                BuildConfig.APPLICATION_ID + ".provider", file
            );
            // Actually launch the camera
            cameraLauncher.launch(uri)
        }


        //just a skeleton, do whatever you want with it! Its here just for reference
        //for UI (see MainMenu). Feel free to update that section too for testing ~Piotr
    }
}