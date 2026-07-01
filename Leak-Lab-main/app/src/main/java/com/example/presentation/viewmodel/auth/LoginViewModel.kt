package com.example.presentation.viewmodel.auth

import android.text.Editable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.di.ServiceLocator
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val userRepository: UserRepository? = ServiceLocator.userRepository

    companion object {
        // Cache the last entered keyboard session inputs
        var lastEnteredText: Editable? = null
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, email: String) {
        if (username.isBlank() || email.isBlank()) {
            _loginState.value = LoginState.Error("Please fill all fields.")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                userRepository?.login(username, email)
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    object Success : LoginState
    data class Error(val message: String) : LoginState
}