package com.synergeticsciences.qrcodegenerator.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.core.content.FileProvider
import androidx.activity.ComponentActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.synergeticsciences.qrcodegenerator.R
import com.synergeticsciences.qrcodegenerator.ui.components.QRCodeScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onRequestCameraPermission: (() -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QR Code Generator",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("history") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_history),
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Generate") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_qr_generate),
                            contentDescription = "Generate QR"
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Scan") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_qr_scan),
                            contentDescription = "Scan QR"
                        )
                    }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedTabIndex) {
                    0 -> GenerateTab()
                    1 -> ScanTab(onRequestCameraPermission = onRequestCameraPermission)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateTab() {
    var textInput by remember { mutableStateOf("") }
    var generatedQrBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generate QR Code",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enter text or URL to generate a QR code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Enter text or URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            generateQRCode(textInput) { bitmap ->
                                generatedQrBitmap = bitmap
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = textInput.isNotBlank()
                ) {
                    Text("Generate QR Code")
                }

                Spacer(modifier = Modifier.height(24.dp))

                generatedQrBitmap?.let { bitmap ->
                    Card(
                        modifier = Modifier.size(200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Generated QR Code",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                saveQRCodeToFile(context, bitmap) { success ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (success) "QR Code saved successfully" else "Failed to save QR Code"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }

                        IconButton(
                            onClick = {
                                shareQRCode(context, bitmap)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanTab(
    onRequestCameraPermission: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showResult) {
            QRCodeScanner(
                onQRCodeDetected = { result ->
                    scannedResult = result
                    showResult = true
                },
                onError = { error ->
                    errorMessage = error
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(error)
                    }
                },
                onRequestPermission = {
                    onRequestCameraPermission?.invoke()
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Show scanned result
            ScannedResultScreen(
                result = scannedResult ?: "",
                onBackToScan = {
                    showResult = false
                    scannedResult = null
                    errorMessage = null
                }
            )
        }
    }

    // Snackbar for errors
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ScannedResultScreen(
    result: String,
    onBackToScan: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "QR Code Scanned Successfully!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Content:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onBackToScan,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Scan Another")
                    }

                    Button(
                        onClick = {
                            // Copy to clipboard
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("QR Code", result)
                            clipboard.setPrimaryClip(clip)
                            
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Copied to clipboard!")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy")
                    }
                }

                // If it's a URL, add open button
                if (result.startsWith("http://") || result.startsWith("https://")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(result))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Could not open URL")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open URL")
                    }
                }
            }
        }
    }

    // Snackbar for copy/open actions
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// QR Code Generation Functions
fun generateQRCode(text: String, onBitmapReady: (ImageBitmap) -> Unit) {
    try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }

        onBitmapReady(bitmap.asImageBitmap())
    } catch (e: Exception) {
        // Handle error
        e.printStackTrace()
    }
}

fun saveQRCodeToFile(context: android.content.Context, bitmap: ImageBitmap, onComplete: (Boolean) -> Unit) {
    try {
        val file = File(context.getExternalFilesDir(null), "qr_code_${System.currentTimeMillis()}.png")
        val outputStream = FileOutputStream(file)
        val androidBitmap = bitmap.asAndroidBitmap()
        androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        onComplete(true)
    } catch (e: Exception) {
        e.printStackTrace()
        onComplete(false)
    }
}

fun shareQRCode(context: android.content.Context, bitmap: ImageBitmap) {
    try {
        val file = File(context.cacheDir, "shared_qr_code.png")
        val outputStream = FileOutputStream(file)
        val androidBitmap = bitmap.asAndroidBitmap()
        androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "image/png"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            android.content.Intent.createChooser(
                intent,
                "Share QR Code"
            )
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
