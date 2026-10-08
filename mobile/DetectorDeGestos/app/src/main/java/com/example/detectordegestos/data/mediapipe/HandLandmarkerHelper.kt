package com.example.detectordegestos.data.mediapipe

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.core.RunningMode

class HandLandmarkerHelper(
    context: Context
) : AutoCloseable {

    private val handLandmarker: HandLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("models/hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumHands(1)
            .setMinHandDetectionConfidence(0.5f)
            .build()

        handLandmarker = HandLandmarker.createFromOptions(
            context.applicationContext,
            options
        )
    }

    fun detect(bitmap: Bitmap): HandLandmarkerResult {
        val mpImage = BitmapImageBuilder(bitmap).build()
        return handLandmarker.detect(mpImage)
    }

    override fun close() {
        handLandmarker.close()


    }
}
