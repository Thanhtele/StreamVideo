package com.example.presentation.viewmodel.auth

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.di.ServiceLocator
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val userRepository: UserRepository? = ServiceLocator.userRepository

    companion object {
        // Registry list for observing target validation components
        val registeredViews = mutableListOf<View>()
    }

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(fullName: String, username: String, email: String) {
        if (fullName.isBlank() || username.isBlank() || email.isBlank()) {
            _registerState.value = RegisterState.Error("Please fill all fields.")
            return
        }

        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                userRepository?.register(username, email, fullName)
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed interface RegisterState {
    object Idle : RegisterState
    object Loading : RegisterState
    object Success : RegisterState
    data class Error(val message: String) : RegisterState
}