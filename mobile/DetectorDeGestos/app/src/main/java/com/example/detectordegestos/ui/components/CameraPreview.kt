
package com.example.detectordegestos.ui.components

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.detectordegestos.data.mediapipe.HandLandmarkerHelper
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onHandDetected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentCallback =
        androidx.compose.runtime.rememberUpdatedState(onHandDetected)

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        val executor = Executors.newSingleThreadExecutor()
        val landmarker = HandLandmarkerHelper(context)

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        var disposed = false

        cameraProviderFuture.addListener({

            val provider = cameraProviderFuture.get()

            if (!disposed) {
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider =
                            previewView.surfaceProvider
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
                                imageProxy.imageInfo.rotationDegrees.toFloat()
                            )
                        }

                        val rotatedBitmap =
                            Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                bitmap.height,
                                matrix,
                                true
                            )

                        val result = landmarker.detect(rotatedBitmap)

                        val pointCount =
                            result.landmarks()
                                .firstOrNull()
                                ?.size ?: 0

                        ContextCompat.getMainExecutor(context)
                            .execute {
                                if (!disposed) {
                                    currentCallback.value(pointCount)
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

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
