package com.synergeticsciences.qrcodegenerator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.synergeticsciences.qrcodegenerator.data.database.QRCodeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenerateQRViewModel : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _generatedQRCode = MutableStateFlow<androidx.compose.ui.graphics.ImageBitmap?>(null)
    val generatedQRCode: StateFlow<androidx.compose.ui.graphics.ImageBitmap?> = _generatedQRCode.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun generateQRCode() {
        if (_inputText.value.isBlank()) {
            _error.value = "Please enter text or URL to generate QR code"
            return
        }

        _isGenerating.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val qrCodeWriter = QRCodeWriter()
                val bitMatrix = qrCodeWriter.encode(_inputText.value, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height

                // Create ImageBitmap from bitMatrix (simplified for now)
                // In a real implementation, you'd convert the bitMatrix to ImageBitmap
                _generatedQRCode.value = null // Placeholder - would need actual bitmap creation

            } catch (e: Exception) {
                _error.value = "Failed to generate QR code: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearQRCode() {
        _generatedQRCode.value = null
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun getQRCodeType(): QRCodeType {
        return when {
            _inputText.value.startsWith("http://") || _inputText.value.startsWith("https://") -> QRCodeType.URL
            _inputText.value.contains("@") -> QRCodeType.EMAIL
            _inputText.value.startsWith("tel:") -> QRCodeType.PHONE
            _inputText.value.startsWith("sms:") -> QRCodeType.SMS
            _inputText.value.startsWith("geo:") -> QRCodeType.GEO
            else -> QRCodeType.TEXT
        }
    }
}
