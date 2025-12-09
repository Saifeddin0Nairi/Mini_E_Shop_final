package com.example.mini_e_shop.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mini_e_shop.data.preferences.UserPreferencesManager.PreferenceKeys.IS_LOGGED_IN
import com.example.mini_e_shop.data.preferences.UserPreferencesManager.PreferenceKeys.LOGGED_IN_USER_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class AuthPreferences(
    val isLoggedIn: Boolean,
    val loggedInUserId: String,
    val rememberMeEmail: String // For pre-filling the login form
)

// Enum for theme options
enum class ThemeOption {
    LIGHT, DARK, SYSTEM
}

@Singleton
class UserPreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferenceKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val LOGGED_IN_USER_ID = stringPreferencesKey("logged_in_user_id")
        val REMEMBER_ME_EMAIL = stringPreferencesKey("remember_me_email")
        val UI_THEME_OPTION = stringPreferencesKey("ui_theme_option")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val authPreferencesFlow: Flow<AuthPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val isLoggedIn = preferences[PreferenceKeys.IS_LOGGED_IN] ?: false
            val userId = preferences[PreferenceKeys.LOGGED_IN_USER_ID] ?: ""
            val email = preferences[PreferenceKeys.REMEMBER_ME_EMAIL] ?: ""
            AuthPreferences(isLoggedIn, userId, email)
        }

    val themeOptionFlow: Flow<ThemeOption> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            when (preferences[PreferenceKeys.UI_THEME_OPTION]) {
                "LIGHT" -> ThemeOption.LIGHT
                "DARK" -> ThemeOption.DARK
                else -> ThemeOption.SYSTEM
            }
        }

    val languageFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferenceKeys.APP_LANGUAGE] ?: "vi" // Default to Vietnamese
        }

    suspend fun saveLoginState(userId: String, email: String, rememberMe: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] = true
            preferences[PreferenceKeys.LOGGED_IN_USER_ID] = userId
            if (rememberMe) {
                preferences[PreferenceKeys.REMEMBER_ME_EMAIL] = email
            } else {
                // Nếu người dùng không chọn, hãy xóa email đã lưu (nếu có)
                preferences.remove(PreferenceKeys.REMEMBER_ME_EMAIL)
            }
        }
    }

    suspend fun clearLoginState() {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] = false
            preferences.remove(PreferenceKeys.LOGGED_IN_USER_ID)
            // We might want to keep the email for user convenience, so we don't clear REMEMBER_ME_EMAIL
        }
    }

    suspend fun saveRememberMeEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.REMEMBER_ME_EMAIL] = email
        }
    }

    suspend fun saveThemeOption(themeOption: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.UI_THEME_OPTION] = themeOption.name
        }
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.APP_LANGUAGE] = languageCode
        }
    }
    suspend fun saveAuthPreferences(isLoggedIn: Boolean, userId: String) { // Nhận vào String
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferenceKeys.LOGGED_IN_USER_ID] = userId // Lưu String
        }
    }
}

