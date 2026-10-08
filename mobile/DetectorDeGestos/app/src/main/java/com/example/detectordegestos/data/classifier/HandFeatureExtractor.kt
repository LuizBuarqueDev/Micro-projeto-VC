package com.example.detectordegestos.data.classifier

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.sqrt

object HandFeatureExtractor {

    fun extract(
        landmarks: List<NormalizedLandmark>
    ): FloatArray {

        require(landmarks.size == 21) {
            "São necessários 21 pontos da mão."
        }

        val wrist = landmarks[0]

        val maxDistance = landmarks.maxOf { point ->
            val dx = (point.x() - wrist.x()).toDouble()
            val dy = (point.y() - wrist.y()).toDouble()

            sqrt(dx * dx + dy * dy)
        }

        val scale = if (maxDistance > 0.0) {
            maxDistance.toFloat()
        } else {
            1f
        }

        val features = FloatArray(63)

        landmarks.forEachIndexed { index, point ->
            val offset = index * 3

            features[offset] =
                (point.x() - wrist.x()) / scale

            features[offset + 1] =
                (point.y() - wrist.y()) / scale

            features[offset + 2] =
                (point.z() - wrist.z()) / scale
        }

        return features
    }
}
