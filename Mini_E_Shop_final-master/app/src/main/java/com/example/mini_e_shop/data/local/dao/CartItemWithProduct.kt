package com.example.mini_e_shop.data.local.dao


import androidx.room.Embedded
import androidx.room.Relation
import com.example.mini_e_shop.data.local.entity.CartItemEntity
import com.example.mini_e_shop.data.local.entity.ProductEntity


data class CartItemWithProduct(

    @Embedded
    val cartItem: CartItemEntity,

    @Relation(
        parentColumn = "productId",
        entityColumn = "id"         
    )
    val product: ProductEntity
)
