package com.example.mini_e_shop.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_e_shop.data.local.entity.UserEntity
import com.example.mini_e_shop.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState = _registerState.asStateFlow()
    private val _registerEvent = Channel<Unit>()
    val registerEvent = _registerEvent.receiveAsFlow()

    fun onNameChange(newValue: String) {
        _name.value = newValue
    }

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun registerUser() {
        val email = _email.value.trim()
        val password = _password.value.trim()
        val username = _name.value.trim()

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("Please fill in all required fields.")
            return
        }
        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password must be at least 6 characters long.")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                // 1. Use Firebase Auth to create a user account
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Registration successful, get user from Firebase
                            val firebaseUser = task.result?.user
                            if (firebaseUser != null) {
                                // Get unique Firebase UID
                                val userId = firebaseUser.uid

                                // 2. Create UserEntity with the required structure
                                val newUserEntity = UserEntity(
                                    id = userId,
                                    name = username,
                                    email = email,
                                    isAdmin = false // New users are not admins by default
                                )

                                // 3. Save user info to both Room and Firestore
                                viewModelScope.launch {
                                    // Save locally (offline support)
                                    userRepository.registerUser(newUserEntity)

                                    // Save to Firestore collection 'users'
                                    firestore.collection("users").document(userId).set(newUserEntity)
                                        .addOnSuccessListener {
                                            viewModelScope.launch {
                                                _registerState.value = RegisterState.Success
                                                _registerEvent.send(Unit) // Send event for navigation
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            _registerState.value =
                                                RegisterState.Error("Failed to save user information: ${e.message}")
                                        }
                                }
                            } else {
                                _registerState.value =
                                    RegisterState.Error("Unable to create user on Firebase.")
                            }
                        } else {
                            // Registration failed
                            _registerState.value = RegisterState.Error(
                                task.exception?.message
                                    ?: "Email is already in use or invalid."
                            )
                        }
                    }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed interface RegisterState {
    object Idle : RegisterState
    object Loading : RegisterState
    object Success : RegisterState
    data class Error(val message: String) : RegisterState
}
