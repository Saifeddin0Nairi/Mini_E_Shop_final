package com.example.mini_e_shop.data.repository

import com.example.mini_e_shop.data.local.dao.UserDao
import com.example.mini_e_shop.data.local.entity.UserEntity
import com.example.mini_e_shop.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import com.example.mini_e_shop.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val preferencesManager: UserPreferencesManager,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val repositoryScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob()
    )

    private var currentListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        // Start listening for changes in the current user from Firestore
        // and sync updates into Room database
        subscribeToRemoteUserChanges()
    }

    // This function runs in the background and listens to Firestore updates,
    // syncing any changes into Room
    private fun subscribeToRemoteUserChanges() {
        repositoryScope.launch {
            // Listen for authentication state changes
            preferencesManager.authPreferencesFlow.collect { prefs ->

                // Remove previous listener if it exists
                currentListener?.remove()
                currentListener = null

                if (prefs.isLoggedIn && prefs.loggedInUserId.isNotEmpty()) {
                    val userId = prefs.loggedInUserId

                    // Listen for changes to this user's Firestore document
                    currentListener = firestore.collection("users").document(userId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                println("Firestore user listener error: ${error.message}")
                                return@addSnapshotListener
                            }

                            if (snapshot != null && snapshot.exists()) {
                                try {
                                    val data = snapshot.data
                                    if (data != null) {

                                        // Convert Firestore "isAdmin" (Boolean/Int/Any) safely
                                        val isAdminValue = data["isAdmin"]
                                        val isAdmin = when (isAdminValue) {
                                            is Boolean -> isAdminValue
                                            is Number -> isAdminValue.toInt() != 0
                                            else -> false
                                        }

                                        android.util.Log.d(
                                            "UserRepositoryImpl",
                                            "Firestore data - isAdmin raw: $isAdminValue, converted: $isAdmin"
                                        )

                                        // Convert Firestore data into a UserEntity
                                        val userEntity = UserEntity(
                                            id = snapshot.id,
                                            email = data["email"] as? String ?: "",
                                            name = data["name"] as? String ?: "",
                                            isAdmin = isAdmin
                                        )

                                        // Update Room database
                                        repositoryScope.launch {
                                            userDao.insertUser(userEntity)
                                            android.util.Log.d(
                                                "UserRepositoryImpl",
                                                "User ${userEntity.id} synced from Firestore → Room. Email: ${userEntity.email}, isAdmin: ${userEntity.isAdmin}"
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("UserRepositoryImpl", "Error converting Firestore user data: $e")
                                    e.printStackTrace()
                                }
                            }
                        }
                }
            }
        }
    }

    override suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    override suspend fun getUserByName(name: String): UserEntity? {
        return userDao.getUserByName(name)
    }

    override suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    override fun observeUserById(userId: String): Flow<UserEntity?> {

        // Force refresh from Firestore before flowing Room values
        repositoryScope.launch {
            try {
                android.util.Log.d("UserRepositoryImpl", "observeUserById - Force refreshing Firestore for user $userId")

                val roomUser = userDao.getUserById(userId)
                android.util.Log.d("UserRepositoryImpl", "Room user: ${roomUser?.email}, isAdmin: ${roomUser?.isAdmin}")

                // Get Firestore version
                val snapshot = firestore.collection("users").document(userId).get().await()

                if (snapshot.exists()) {
                    val data = snapshot.data

                    if (data != null) {
                        val isAdminValue = data["isAdmin"]
                        val isAdmin = when (isAdminValue) {
                            is Boolean -> isAdminValue
                            is Number -> isAdminValue.toInt() != 0
                            else -> false
                        }

                        val userEntity = UserEntity(
                            id = snapshot.id,
                            email = data["email"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            isAdmin = isAdmin
                        )

                        // Sync to Room only if needed
                        if (roomUser == null || roomUser.isAdmin != userEntity.isAdmin) {
                            userDao.insertUser(userEntity)
                        }
                    }
                } else {
                    // If Firestore does not contain this user but Room does → sync Room → Firestore
                    if (roomUser != null) {
                        val userMap = hashMapOf(
                            "email" to roomUser.email,
                            "name" to roomUser.name,
                            "isAdmin" to roomUser.isAdmin
                        )
                        firestore.collection("users").document(userId).set(userMap)
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("UserRepositoryImpl", "observeUserById - Firestore refresh error: $e")
            }
        }

        return userDao.observeUserById(userId)
    }

    override suspend fun registerUser(user: UserEntity) {
        android.util.Log.d("UserRepositoryImpl", "registerUser - ${user.email}, isAdmin=${user.isAdmin}")

        // Save to Room
        userDao.insertUser(user)

        // Sync to Firestore
        val userMap = hashMapOf(
            "email" to user.email,
            "name" to user.name,
            "isAdmin" to user.isAdmin
        )

        firestore.collection("users").document(user.id)
            .set(userMap)
            .addOnSuccessListener {
                android.util.Log.d("UserRepositoryImpl", "User ${user.id} synced to Firestore.")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("UserRepositoryImpl", "Error syncing user to Firestore: $e")
            }
    }

    override suspend fun updateUser(user: UserEntity) {
        android.util.Log.d("UserRepositoryImpl", "updateUser - ${user.email}, isAdmin=${user.isAdmin}")

        // Update Room database
        userDao.insertUser(user)

        // Sync update to Firestore
        val userMap = hashMapOf(
            "email" to user.email,
            "name" to user.name,
            "isAdmin" to user.isAdmin
        )

        firestore.collection("users").document(user.id)
            .set(userMap)
            .addOnSuccessListener {
                android.util.Log.d("UserRepositoryImpl", "User ${user.id} updated in Firestore.")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("UserRepositoryImpl", "Error updating user in Firestore: $e")
            }
    }

    override fun getCurrentUser(): Flow<UserEntity?> {
        return preferencesManager.authPreferencesFlow.flatMapLatest { prefs ->
            if (prefs.isLoggedIn && prefs.loggedInUserId.isNotEmpty()) {

                val userId = prefs.loggedInUserId

                // Force syncing Room → Firestore if Room indicates admin
                repositoryScope.launch {
                    val roomUser = userDao.getUserById(userId)
                    if (roomUser != null && roomUser.isAdmin) {
                        val userMap = hashMapOf(
                            "email" to roomUser.email,
                            "name" to roomUser.name,
                            "isAdmin" to roomUser.isAdmin
                        )
                        firestore.collection("users").document(userId).set(userMap)
                    }
                }

                userDao.observeUserById(userId)

            } else {
                flowOf(null)
            }
        }
    }
}

