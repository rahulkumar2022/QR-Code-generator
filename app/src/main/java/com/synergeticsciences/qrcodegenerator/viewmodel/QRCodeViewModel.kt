package com.synergeticsciences.qrcodegenerator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synergeticsciences.qrcodegenerator.data.database.QRCodeEntity
import com.synergeticsciences.qrcodegenerator.data.database.QRCodeType
import com.synergeticsciences.qrcodegenerator.data.repository.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class QRCodeViewModel(
    private val repository: QRCodeRepository
) : ViewModel() {

    private val _qrCodes = MutableStateFlow<List<QRCodeEntity>>(emptyList())
    val qrCodes: StateFlow<List<QRCodeEntity>> = _qrCodes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadQRCodes()
    }

    private fun loadQRCodes() {
        viewModelScope.launch {
            try {
                repository.getAllQRCodes().collect { codes ->
                    _qrCodes.value = codes ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load QR codes: ${e.message}"
                _qrCodes.value = emptyList()
            }
        }
    }

    fun saveGeneratedQRCode(content: String, type: QRCodeType = QRCodeType.TEXT) {
        viewModelScope.launch {
            try {
                val qrCode = QRCodeEntity(
                    content = content,
                    type = type,
                    timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    isGenerated = true
                )
                repository.insertQRCode(qrCode)
            } catch (e: Exception) {
                _error.value = "Failed to save QR code: ${e.message}"
            }
        }
    }

    fun saveScannedQRCode(content: String, type: QRCodeType = QRCodeType.TEXT) {
        viewModelScope.launch {
            try {
                val qrCode = QRCodeEntity(
                    content = content,
                    type = type,
                    timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    isGenerated = false
                )
                repository.insertQRCode(qrCode)
            } catch (e: Exception) {
                _error.value = "Failed to save scanned QR code: ${e.message}"
            }
        }
    }

    fun deleteQRCode(qrCode: QRCodeEntity) {
        viewModelScope.launch {
            try {
                repository.deleteQRCode(qrCode)
            } catch (e: Exception) {
                _error.value = "Failed to delete QR code: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
