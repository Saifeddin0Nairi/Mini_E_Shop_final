package com.example.mini_e_shop.domain.repository

import com.example.mini_e_shop.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserByEmail(email: String):  UserEntity?
    suspend fun getUserByName(name: String): UserEntity?
    suspend fun getUserById(userId: String): UserEntity?
    fun observeUserById(userId: String): Flow<UserEntity?>
    suspend fun registerUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity)
    fun getCurrentUser(): Flow<UserEntity?>
}
