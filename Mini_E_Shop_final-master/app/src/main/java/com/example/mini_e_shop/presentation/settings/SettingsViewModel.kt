package com.example.mini_e_shop.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_e_shop.data.preferences.ThemeOption
import com.example.mini_e_shop.data.preferences.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "Tiếng Việt", // This will now be driven by the languageCode
    val languageCode: String = "vi", // Add languageCode to the state
    val name: String = "manh",
    val email: String = "manh@gmail.com"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _recreateActivityEvent = MutableSharedFlow<Unit>()
    val recreateActivityEvent = _recreateActivityEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            // Sync language first
            val initialLanguageCode = userPreferencesManager.languageFlow.first()
            _state.update { it.copy(
                languageCode = initialLanguageCode,
                language = if (initialLanguageCode == "vi") "Tiếng Việt" else "English"
            ) }

            userPreferencesManager.languageFlow.collect { langCode ->
                if (langCode != _state.value.languageCode) {
                    _state.update { it.copy(
                        languageCode = langCode,
                        language = if (langCode == "vi") "Tiếng Việt" else "English"
                    ) }
                }
            }
        }

        viewModelScope.launch {
            val initialTheme = userPreferencesManager.themeOptionFlow.first()
            _state.update { it.copy(darkModeEnabled = initialTheme == ThemeOption.DARK) }

            userPreferencesManager.themeOptionFlow.collect { option ->
                _state.update { it.copy(darkModeEnabled = option == ThemeOption.DARK) }
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _state.update { it.copy(notificationsEnabled = enabled) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val themeOption = if (enabled) ThemeOption.DARK else ThemeOption.LIGHT
            userPreferencesManager.saveThemeOption(themeOption)
        }
        _state.update { it.copy(darkModeEnabled = enabled) }
    }

    fun changeLanguage(newLanguageCode: String) {
        if (newLanguageCode != _state.value.languageCode) {
            viewModelScope.launch {
                userPreferencesManager.saveLanguage(newLanguageCode)
                _recreateActivityEvent.emit(Unit)
            }
        }
    }

    fun updateUserInfo(name: String, email: String) {
        _state.update { it.copy(name = name, email = email) }
    }
}
