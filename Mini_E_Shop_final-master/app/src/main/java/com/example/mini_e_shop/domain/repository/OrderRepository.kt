package com.example.mini_e_shop.domain.repository

import com.example.mini_e_shop.data.local.entity.OrderEntity
import com.example.mini_e_shop.data.local.entity.OrderItemEntity
import com.example.mini_e_shop.domain.model.Order
import com.example.mini_e_shop.presentation.cart.CartItemDetails
import kotlinx.coroutines.flow.Flow


interface OrderRepository {
    fun getOrders(userId: String): Flow<List<OrderEntity>>
    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>>
    suspend fun createOrderFromCart(userId: String, cartItems: List<CartItemDetails>)
    fun getOrdersForUser(userId: String): Flow<List<Order>>
}

