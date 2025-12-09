package com.example.mini_e_shop.data.mapper

import com.example.mini_e_shop.data.local.dao.CartItemWithProduct
import com.example.mini_e_shop.data.local.entity.CartItemEntity
import com.example.mini_e_shop.domain.model.CartItem
import com.example.mini_e_shop.presentation.cart.CartItemDetails

fun CartItemWithProduct.toCartItemDetails(): CartItemDetails {
    return CartItemDetails(
        cartItem = this.cartItem.toCartItem(),
        product = this.product.toProduct()
    )
}

fun CartItemEntity.toCartItem(): CartItem {
    return CartItem(
        id = id,
        userId = userId,
        productId = productId,
        quantity = quantity
    )
}
