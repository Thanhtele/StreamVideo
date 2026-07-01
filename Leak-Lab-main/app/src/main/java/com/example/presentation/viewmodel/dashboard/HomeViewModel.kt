package com.example.presentation.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import com.example.data.di.ServiceLocator

class HomeViewModel : ViewModel() {
    private val userRepository = ServiceLocator.userRepository
    val activeUser = userRepository?.activeUser
}