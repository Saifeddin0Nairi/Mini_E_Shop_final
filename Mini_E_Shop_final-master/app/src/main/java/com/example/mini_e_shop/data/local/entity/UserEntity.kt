package com.example.mini_e_shop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

object UserRole {
//    const val ADMIN = "ADMIN"
//    const val USER = "USER"
}
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
//    val passwordHash: String,
    val name: String,
    val isAdmin : Boolean
)
