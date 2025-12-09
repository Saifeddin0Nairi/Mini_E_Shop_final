package com.example.mini_e_shop.domain.model

data class User(
    val id: String,
    val email: String,
//    val passwordHash: String,
    val name: String,
    val isAdmin : Boolean
)
