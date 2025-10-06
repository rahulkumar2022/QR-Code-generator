package com.synergeticsciences.qrcodegenerator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synergeticsciences.qrcodegenerator.data.repository.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: QRCodeRepository
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(true) // Default to dark theme
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _autoCopyEnabled = MutableStateFlow(true)
    val autoCopyEnabled: StateFlow<Boolean> = _autoCopyEnabled.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _historyCount = MutableStateFlow(0)
    val historyCount: StateFlow<Int> = _historyCount.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _historyCount.value = repository.getQRCodeCount()
            } catch (e: Exception) {
                _error.value = "Failed to load settings: ${e.message}"
            }
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleAutoCopy() {
        _autoCopyEnabled.value = !_autoCopyEnabled.value
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteAllQRCodes()
                _historyCount.value = 0
            } catch (e: Exception) {
                _error.value = "Failed to clear history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshHistoryCount() {
        viewModelScope.launch {
            try {
                _historyCount.value = repository.getQRCodeCount()
            } catch (e: Exception) {
                _error.value = "Failed to refresh history count: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
