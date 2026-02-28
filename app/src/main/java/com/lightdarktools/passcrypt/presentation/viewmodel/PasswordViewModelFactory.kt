package com.lightdarktools.passcrypt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lightdarktools.passcrypt.data.local.SettingsManager
import com.lightdarktools.passcrypt.domain.usecase.PasswordUseCases

class PasswordViewModelFactory(
    private val useCases: PasswordUseCases,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasswordViewModel(useCases, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
