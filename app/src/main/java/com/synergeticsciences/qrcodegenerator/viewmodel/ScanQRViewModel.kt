package com.synergeticsciences.qrcodegenerator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synergeticsciences.qrcodegenerator.data.database.QRCodeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanQRViewModel : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private val _scannedResult = MutableStateFlow<String?>(null)
    val scannedResult: StateFlow<String?> = _scannedResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun startScanning() {
        _isScanning.value = true
        _error.value = null
    }

    fun stopScanning() {
        _isScanning.value = false
    }

    fun toggleTorch() {
        _torchEnabled.value = !_torchEnabled.value
    }

    fun onQRCodeScanned(result: String) {
        _scannedResult.value = result
        _isScanning.value = false

        // Determine QR code type from content
        val type = determineQRCodeType(result)
        // Save to history would be handled by the parent ViewModel
    }

    private fun determineQRCodeType(content: String): QRCodeType {
        return when {
            content.startsWith("http://") || content.startsWith("https://") -> QRCodeType.URL
            content.contains("@") -> QRCodeType.EMAIL
            content.startsWith("tel:") -> QRCodeType.PHONE
            content.startsWith("sms:") -> QRCodeType.SMS
            content.startsWith("geo:") -> QRCodeType.GEO
            content.startsWith("WIFI:") -> QRCodeType.WIFI
            else -> QRCodeType.TEXT
        }
    }

    fun clearResult() {
        _scannedResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
