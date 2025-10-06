package com.synergeticsciences.qrcodegenerator.ui.components

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun QRCodeScanner(
    onQRCodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    onRequestPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var permissionCheckTrigger by remember { mutableStateOf(0) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Check camera permission
    LaunchedEffect(permissionCheckTrigger) {
        hasCameraPermission = checkCameraPermission(context)
    }

    // Listen for lifecycle changes to refresh permission when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionCheckTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Cleanup camera executor
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // Camera Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            
                            val preview = Preview.Builder().build()
                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor, QRCodeAnalyzer { qrCode ->
                                        onQRCodeDetected(qrCode)
                                    })
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalyzer
                                )
                                
                                preview.setSurfaceProvider(previewView.surfaceProvider)
                            } catch (exc: Exception) {
                                Log.e("QRCodeScanner", "Use case binding failed", exc)
                                onError("Failed to start camera: ${exc.message}")
                            }
                        } catch (exc: Exception) {
                            Log.e("QRCodeScanner", "Camera provider initialization failed", exc)
                            onError("Failed to initialize camera: ${exc.message}")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                // Scanning frame
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.Center)
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                )

                // Instructions
                Text(
                    text = "Position QR code within the frame",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                )
            }

            // Torch toggle button
            FloatingActionButton(
                onClick = {
                    torchEnabled = !torchEnabled
                    camera?.cameraControl?.enableTorch(torchEnabled)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Icon(
                    imageVector = if (torchEnabled) Icons.Default.FlashOff else Icons.Default.FlashOn,
                    contentDescription = if (torchEnabled) "Turn off flash" else "Turn on flash",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Permission denied state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera permission required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Please grant camera permission to scan QR codes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        onRequestPermission()
                        // Trigger permission check after requesting
                        permissionCheckTrigger++
                    }
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

private fun checkCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

private class QRCodeAnalyzer(
    private val onQRCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            onQRCodeDetected(value)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("QRCodeAnalyzer", "Barcode scanning failed", exception)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
