package com.example.presentation.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.UserEntity
import com.example.data.di.ServiceLocator
import com.example.data.repository.AuthListener
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel(), AuthListener {
    private val userRepository = ServiceLocator.userRepository

    private val _userState = MutableStateFlow<UserEntity?>(null)
    val userState: StateFlow<UserEntity?> = _userState.asStateFlow()

    init {
        // Register to the repository event notifier stream
        UserRepository.addAuthListener(this)
    }

    override fun onUserChanged(user: UserEntity?) {
        _userState.value = user
    }

    fun logout() {
        viewModelScope.launch {
            userRepository?.logout()
        }
    }
}