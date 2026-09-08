package com.libraryofmiao.membership.ui.verify

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.libraryofmiao.membership.data.model.VerifyResult
import com.libraryofmiao.membership.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class VerifyUiState(
    val code: String = "",
    val loading: Boolean = false,
    val result: VerifyResult? = null,
    val error: String? = null
)

class VerifyViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState

    fun onCodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(code = value, error = null)
    }

    fun verify(code: String = _uiState.value.code) {
        if (code.isBlank()) return
        _uiState.value = _uiState.value.copy(loading = true, error = null, code = code)
        viewModelScope.launch {
            try {
                val response = ApiClient.membershipApi.verify(code)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _uiState.value = _uiState.value.copy(loading = false, result = body)
                } else {
                    _uiState.value = _uiState.value.copy(loading = false, error = body?.error ?: "Member not found")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = "Server connection error")
            }
        }
    }
}
