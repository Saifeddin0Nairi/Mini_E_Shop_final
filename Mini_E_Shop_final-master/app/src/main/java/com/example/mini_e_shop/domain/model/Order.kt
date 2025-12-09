package com.example.mini_e_shop.domain.model

data class Order(
    val id: String,
    val userId: String,
    val totalAmount: Double,
    val createdAt: String
)
