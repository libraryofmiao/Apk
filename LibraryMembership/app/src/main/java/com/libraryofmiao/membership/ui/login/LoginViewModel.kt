package com.libraryofmiao.membership.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.libraryofmiao.membership.data.network.ApiClient
import com.libraryofmiao.membership.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val pin: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val session = SessionManager(app)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onPinChanged(value: String) {
        _uiState.value = _uiState.value.copy(pin = value.filter { it.isDigit() }, error = null)
    }

    fun submit() {
        val pin = _uiState.value.pin
        if (pin.length != 6) return
        _uiState.value = _uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                val response = ApiClient.authApi.login(mapOf("pin" to pin))
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    session.setLoggedIn(true)
                    _uiState.value = _uiState.value.copy(loading = false, success = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = body?.error ?: "Invalid administrative passcode."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Server connection error. Please try again."
                )
            }
        }
    }
}
