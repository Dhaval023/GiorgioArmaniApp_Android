package com.example.giorgioarmaniapp.ui.login_page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.service.RestService
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object NoInternet : LoginUiState()
}

class LoginPageViewModel : ViewModel() {

    private val restService = RestService()

    var usernameText: String = ""
    var passwordText: String = ""

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    init {
        // Mirror C# default power settings init
        if (Settings.inboundRFIDPower == 0) Settings.inboundRFIDPower = 270
        if (Settings.outboundRFIDPower == 0) Settings.outboundRFIDPower = 270
        if (Settings.stockTakeRFIDPower == 0) Settings.stockTakeRFIDPower = 270
    }

    fun login(hasInternet: Boolean) {
        if (!hasInternet) {
            _uiState.value = LoginUiState.NoInternet
            return
        }
        if (usernameText.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter your username.")
            return
        }
        if (passwordText.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter your password.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = restService.login(usernameText, passwordText, "mobile")
                if (response != null && response.success == 1) {
                    val loginDetails = response.data?.firstOrNull()
                    if (loginDetails != null) {
                        Settings.userId = loginDetails.user_id
                        Settings.userType = loginDetails.user_type ?: ""
                        Settings.userName = loginDetails.empName ?: ""
                        Settings.password = passwordText
                        Settings.storeId = loginDetails.store_id ?: ""
                        Settings.storeName = loginDetails.store_name ?: ""
                        _uiState.value = LoginUiState.Success
                    } else {
                        _uiState.value = LoginUiState.Error("Login failed. No user data returned.")
                    }
                } else {
                    _uiState.value = LoginUiState.Error(response?.msg ?: "Login failed.")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("An unexpected error occurred.")
            }
        }
    }
}