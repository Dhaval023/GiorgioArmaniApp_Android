package com.example.giorgioarmaniapp.ui.login_page.popup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.service.RestService
import kotlinx.coroutines.launch

class PasscodeViewModel : ViewModel() {

    private val restService = RestService()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _alertEvent = MutableLiveData<String?>()
    val alertEvent: LiveData<String?> = _alertEvent

    private val _navigateToSettings = MutableLiveData<Boolean>()
    val navigateToSettings: LiveData<Boolean> = _navigateToSettings

    fun submitPasscode(passcode: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = restService.passcodePost(passcode)

                if (response != null &&
                    response.msg != null &&
                    response.success == 1
                ) {
                    _navigateToSettings.value = true
                } else {
                    _alertEvent.value = response?.msg ?: "Invalid Passcode."
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                _alertEvent.value = "Something went wrong. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onAlertHandled()    { _alertEvent.value = null }
    fun onNavigateHandled() { _navigateToSettings.value = false }
}