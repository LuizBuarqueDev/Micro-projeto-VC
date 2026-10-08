
package com.example.detectordegestos.ui.components

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.detectordegestos.data.classifier.HandFeatureExtractor
import com.example.detectordegestos.data.classifier.RandomForestClassifier
import com.example.detectordegestos.data.mediapipe.HandLandmarkerHelper
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    showKeypoints: Boolean = false,
    onHandDetected: (Int, String?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentCallback = rememberUpdatedState(onHandDetected)

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var handPoints by remember {
        mutableStateOf<List<Offset>>(emptyList())
    }

    DisposableEffect(lifecycleOwner, previewView) {

        val executor = Executors.newSingleThreadExecutor()

        val landmarker = HandLandmarkerHelper(context)
        val classifier = RandomForestClassifier(context)

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        var disposed = false

        cameraProviderFuture.addListener({

            val provider = cameraProviderFuture.get()

            if (!disposed) {

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .setOutputImageFormat(
                        ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                    )
                    .build()

                analysis.setAnalyzer(executor) { imageProxy ->

                    try {
                        val bitmap = Bitmap.createBitmap(
                            imageProxy.width,
                            imageProxy.height,
                            Bitmap.Config.ARGB_8888
                        )

                        imageProxy.planes[0].buffer.rewind()

                        bitmap.copyPixelsFromBuffer(
                            imageProxy.planes[0].buffer
                        )

                        val matrix = Matrix().apply {
                            postRotate(
                                imageProxy.imageInfo
                                    .rotationDegrees.toFloat()
                            )
                        }

                        val rotatedBitmap = Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            bitmap.width,
                            bitmap.height,
                            matrix,
                            true
                        )

                        val result = landmarker.detect(rotatedBitmap)

                        val landmarks =
                            result.landmarks().firstOrNull()

                        val points = landmarks?.map { landmark ->
                            Offset(
                                landmark.x(),
                                landmark.y()
                            )
                        } ?: emptyList()

                        val pointCount = landmarks?.size ?: 0

                        val gesture =
                            if (landmarks != null && landmarks.size == 21) {

                                val features =
                                    HandFeatureExtractor.extract(landmarks)

                                classifier.predict(features)

                            } else {
                                null
                            }

                        ContextCompat.getMainExecutor(context).execute {

                            if (!disposed) {

                                handPoints = points

                                currentCallback.value(
                                    pointCount,
                                    gesture
                                )
                            }
                        }

                    } catch (exception: Exception) {
                        exception.printStackTrace()

                    } finally {
                        imageProxy.close()
                    }
                }

                try {
                    provider.unbindAll()

                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        analysis
                    )

                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }

        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true

            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }

            executor.execute {
                landmarker.close()
            }

            executor.shutdown()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        if (showKeypoints) {
            HandLandmarkOverlay(
                landmarks = handPoints,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}