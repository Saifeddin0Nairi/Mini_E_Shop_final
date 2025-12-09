package com.example.mini_e_shop.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cart_items",

    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE 
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["productId"])]
)
data class CartEntity(
    val userId: String,
    val productId: String,
    val quantity: Int
)
