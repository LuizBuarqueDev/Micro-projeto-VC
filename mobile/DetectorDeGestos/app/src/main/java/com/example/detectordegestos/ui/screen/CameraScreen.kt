
package com.example.detectordegestos.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

import com.example.detectordegestos.ui.components.CameraPreview

@Composable
fun CameraScreen() {

    val context = LocalContext.current

    // Quantidade de pontos detectados
    var detectedPoints by remember {
        mutableIntStateOf(0)
    }

    // Gesto reconhecido pelo Random Forest
    var detectedGesture by remember {
        mutableStateOf<String?>(null)
    }

    // Controla a exibição dos keypoints
    var showKeypoints by remember {
        mutableStateOf(false)
    }

    // Permissão da câmera
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Detector de Gestos",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Mostre pedra, papel ou tesoura",
                style = MaterialTheme.typography.bodyMedium
            )

            if (permissionGranted) {

                // Visualização da câmera
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    CameraPreview(
                        showKeypoints = showKeypoints,
                        onHandDetected = { points, gesture ->
                            detectedPoints = points
                            detectedGesture = gesture
                        }
                    )
                }

                // Botão para mostrar ou ocultar os pontos
                Button(
                    onClick = {
                        showKeypoints = !showKeypoints
                    }
                ) {
                    Text(
                        text = if (showKeypoints) {
                            "Ocultar pontos"
                        } else {
                            "Mostrar pontos"
                        }
                    )
                }

                // Status da detecção
                Text(
                    text = if (detectedPoints == 21) {
                        "Mão detectada: 21 pontos identificados"
                    } else {
                        "Nenhuma mão detectada"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                // Traduz o resultado do modelo
                val gestureText = when (detectedGesture) {
                    "rock" -> "PEDRA"
                    "paper" -> "PAPEL"
                    "scissors" -> "TESOURA"
                    else -> "Aguardando gesto..."
                }

                // Exibe o gesto reconhecido
                Text(
                    text = gestureText,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

            } else {

                // Solicitação da permissão da câmera
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    ) {
                        Text("Permitir acesso à câmera")
                    }
                }
            }
        }
    }
}