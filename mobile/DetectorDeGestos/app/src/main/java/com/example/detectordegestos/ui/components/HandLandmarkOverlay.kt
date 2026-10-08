package com.example.detectordegestos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

private val connections = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4,
    0 to 5, 5 to 6, 6 to 7, 7 to 8,
    5 to 9, 9 to 10, 10 to 11, 11 to 12,
    9 to 13, 13 to 14, 14 to 15, 15 to 16,
    13 to 17, 17 to 18, 18 to 19, 19 to 20,
    0 to 17
)

@Composable
fun HandLandmarkOverlay(
    landmarks: List<Offset>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {

        val points = landmarks.map {
            Offset(
                x = (1f - it.x) * size.width,
                y = it.y * size.height
            )
        }

        if (points.size != 21) return@Canvas

        // Linhas conectando os pontos
        connections.forEach { (start, end) ->
            drawLine(
                color = Color.White,
                start = points[start],
                end = points[end],
                strokeWidth = 4f
            )
        }

        // Os 21 pontos da mão
        points.forEach { point ->
            drawCircle(
                color = Color.Red,
                radius = 7f,
                center = point
            )
        }
    }
}