package com.example.mini_e_shop.domain.model

data class CartItem(
    val id: Int,
    val userId: String,
    val productId: String,
    val quantity: Int
)
