
package com.example.detectordegestos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.detectordegestos.ui.screen.CameraScreen
import com.example.detectordegestos.ui.theme.DetectorDeGestosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            DetectorDeGestosTheme {
                CameraScreen()
            }
        }
    }
}