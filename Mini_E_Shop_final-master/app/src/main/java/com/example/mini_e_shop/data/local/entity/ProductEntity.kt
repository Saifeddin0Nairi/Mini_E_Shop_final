package com.example.mini_e_shop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
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
