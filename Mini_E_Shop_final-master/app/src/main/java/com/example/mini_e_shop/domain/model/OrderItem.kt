package com.example.mini_e_shop.domain.model

data class OrderItem(
    val id: Int,
    val orderId: String,
    val shoeId: Int,
    val quantity: Int,
    val unitPrice: Double
)
