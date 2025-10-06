package com.synergeticsciences.qrcodegenerator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.synergeticsciences.qrcodegenerator.data.database.QRCodeDatabase
import com.synergeticsciences.qrcodegenerator.data.repository.QRCodeRepository
import com.synergeticsciences.qrcodegenerator.data.repository.QRCodeRepositoryImpl
import com.synergeticsciences.qrcodegenerator.navigation.AppNavigation
import com.synergeticsciences.qrcodegenerator.ui.theme.QRCodeGeneratorTheme
import com.synergeticsciences.qrcodegenerator.viewmodel.*

class MainActivity : ComponentActivity() {

    lateinit var qrCodeRepository: QRCodeRepository
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result is handled in the QRCodeScanner composable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install splash screen
        installSplashScreen()

        try {
            // Initialize database and repository
            val database = QRCodeDatabase.getDatabase(applicationContext)
            qrCodeRepository = QRCodeRepositoryImpl(database.qrCodeDao())

            setContent {
                QRCodeGeneratorTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            onRequestCameraPermission = { requestCameraPermission() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Handle initialization error
            android.util.Log.e("MainActivity", "Failed to initialize app", e)
            finish()
        }
    }

    fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Factory for creating ViewModels with repository injection
    inline fun <reified T : androidx.lifecycle.ViewModel> getViewModel(
        crossinline factory: (QRCodeRepository) -> T
    ): T {
        return ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return factory(qrCodeRepository) as T
                }
            }
        )[T::class.java]
    }
}