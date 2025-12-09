package com.example.mini_e_shop.domain.model

data class Products (
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val origin: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String,
    val description: String
)