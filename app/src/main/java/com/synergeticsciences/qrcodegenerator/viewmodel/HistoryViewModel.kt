package com.synergeticsciences.qrcodegenerator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synergeticsciences.qrcodegenerator.data.database.QRCodeEntity
import com.synergeticsciences.qrcodegenerator.data.repository.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: QRCodeRepository
) : ViewModel() {

    private val _qrCodes = MutableStateFlow<List<QRCodeEntity>>(emptyList())
    val qrCodes: StateFlow<List<QRCodeEntity>> = _qrCodes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllQRCodes().collect { codes ->
                    _qrCodes.value = codes ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load history: ${e.message}"
                _qrCodes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        performSearch(query)
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            loadHistory()
            return
        }

        viewModelScope.launch {
            try {
                repository.searchQRCodes(query).collect { results ->
                    _qrCodes.value = results ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                _qrCodes.value = emptyList()
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

    fun deleteAllHistory() {
        viewModelScope.launch {
            try {
                repository.deleteAllQRCodes()
            } catch (e: Exception) {
                _error.value = "Failed to clear history: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
