package com.example.mini_e_shop.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_e_shop.data.local.entity.UserEntity
import com.example.mini_e_shop.data.preferences.UserPreferencesManager
import com.example.mini_e_shop.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _usernameOrEmail = MutableStateFlow("")
    val usernameOrEmail = _usernameOrEmail.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe = _rememberMe.asStateFlow()

    private val _loginEvent = Channel<LoginEvent>()
    val loginEvent = _loginEvent.receiveAsFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    init {
        loadRememberMeCredentials()
    }

    /**
     * Load saved email if the user previously selected "Remember Me".
     */
    private fun loadRememberMeCredentials() {
        viewModelScope.launch {
            val prefs = userPreferencesManager.authPreferencesFlow.first()
            _usernameOrEmail.value = prefs.rememberMeEmail
            if (prefs.rememberMeEmail.isNotEmpty()) {
                _rememberMe.value = true
            }
        }
    }

    fun onUsernameOrEmailChange(newValue: String) {
        _usernameOrEmail.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun onRememberMeChange(newValue: Boolean) {
        _rememberMe.value = newValue
    }

    /**
     * Handles the login process:
     * 1. Login using Firebase Auth
     * 2. Save login state to DataStore
     * 3. Sync user data from Firestore → Room
     * 4. Navigate to Home screen
     */
    fun loginUser() {
        val email = _usernameOrEmail.value.trim()
        val password = _password.value.trim()

        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Please enter email and password.")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                // 1. Login using Firebase Auth
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            // 2. Get the Firebase user
                            val firebaseUser = task.result?.user
                            if (firebaseUser != null) {

                                val userId = firebaseUser.uid

                                viewModelScope.launch {

                                    // 3. Save login state in DataStore
                                    userPreferencesManager.saveLoginState(
                                        userId = userId,
                                        email = email,
                                        rememberMe = _rememberMe.value
                                    )

                                    // 4. Always fetch Firestore first to ensure isAdmin is correct
                                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    var userEntity: UserEntity? = null

                                    try {
                                        val snapshot = firestore.collection("users").document(userId).get().await()
                                        if (snapshot.exists()) {
                                            val data = snapshot.data
                                            if (data != null) {

                                                // Convert Firestore isAdmin field safely
                                                val rawAdminValue = data["isAdmin"]
                                                val isAdmin = when (rawAdminValue) {
                                                    is Boolean -> rawAdminValue
                                                    is Number -> rawAdminValue.toInt() != 0
                                                    else -> false
                                                }

                                                android.util.Log.d(
                                                    "LoginViewModel",
                                                    "Firestore isAdmin raw: $rawAdminValue, converted: $isAdmin"
                                                )

                                                userEntity = UserEntity(
                                                    id = snapshot.id,
                                                    email = data["email"] as? String ?: email,
                                                    name = data["name"] as? String ?: "",
                                                    isAdmin = isAdmin
                                                )

                                                // Save to Room
                                                userRepository.registerUser(userEntity)
                                                android.util.Log.d(
                                                    "LoginViewModel",
                                                    "User synced Firestore → Room. Admin: ${userEntity!!.isAdmin}"
                                                )
                                            }
                                        } else {
                                            // User not found in Firestore → fallback to Room or auto-create
                                            userEntity = userRepository.getUserById(userId)
                                            if (userEntity == null) {
                                                userEntity = UserEntity(
                                                    id = userId,
                                                    email = email,
                                                    name = email.substringBefore("@"),
                                                    isAdmin = false
                                                )
                                                userRepository.registerUser(userEntity)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        println("LoginViewModel: Error fetching Firestore user: $e")

                                        // Fallback to Room
                                        userEntity = userRepository.getUserById(userId)
                                        if (userEntity == null) {
                                            userEntity = UserEntity(
                                                id = userId,
                                                email = email,
                                                name = email.substringBefore("@"),
                                                isAdmin = false
                                            )
                                            userRepository.registerUser(userEntity)
                                        }
                                    }

                                    _loginState.value = LoginState.Success(userEntity)

                                    // Emit navigation event
                                    _loginEvent.send(LoginEvent.NavigateToHome)
                                }

                            } else {
                                _loginState.value = LoginState.Error("Failed to retrieve user information.")
                            }
                        } else {
                            // Login failed
                            _loginState.value = LoginState.Error(
                                task.exception?.message ?: "Incorrect email or password."
                            )
                        }
                    }

            } catch (e: Exception) {
                _loginState.value = LoginState.Error("An error occurred: ${e.message}")
            }
        }
    }

    // UI states for Login Screen
    sealed interface LoginState {
        object Idle : LoginState
        object Loading : LoginState
        data class Success(val user: UserEntity?) : LoginState
        data class Error(val message: String) : LoginState
    }

    // Events sent to the UI layer
    sealed interface LoginEvent {
        object NavigateToHome : LoginEvent
    }
}
