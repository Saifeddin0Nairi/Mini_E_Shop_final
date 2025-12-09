package com.example.mini_e_shop.data.local.entity
import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["userId", "productId"])
data class FavoriteEntity(
    val userId: String,
    val productId: String
)
