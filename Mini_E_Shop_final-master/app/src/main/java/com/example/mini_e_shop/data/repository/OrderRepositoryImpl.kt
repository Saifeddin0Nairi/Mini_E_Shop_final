package com.example.mini_e_shop.data.repository

import com.example.mini_e_shop.data.local.dao.OrderDao
import com.example.mini_e_shop.data.local.entity.OrderEntity
import com.example.mini_e_shop.data.local.entity.OrderItemEntity
import com.example.mini_e_shop.domain.model.Order
import com.example.mini_e_shop.domain.repository.OrderRepository
import com.example.mini_e_shop.presentation.cart.CartItemDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getOrders(userId: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByUser(userId)
    }

    override fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>> {
        return orderDao.getItemsForOrder(orderId)
    }

    override fun getOrdersForUser(userId: String): Flow<List<Order>> {
        return orderDao.getOrdersByUser(userId).map { entities ->
            entities
                .filter { it.isPaid } // Chỉ lấy các đơn hàng đã thanh toán
                .map { entity ->
                    Order(
                        id = entity.id,
                        userId = entity.userId,
                        totalAmount = entity.totalAmount,
                        createdAt = entity.createdAt.toString()
                    )
                }
        }
    }

    override suspend fun createOrderFromCart(userId: String, cartItems: List<CartItemDetails>) {
        if (cartItems.isEmpty()) return

        val newOrderId = UUID.randomUUID().toString()

        val totalAmount = cartItems.sumOf { it.product.price * it.cartItem.quantity }

        val orderEntity = OrderEntity(
            id = newOrderId,
            userId = userId,
            totalAmount = totalAmount,
            createdAt = Date(),
            isPaid = true
        )
        val orderItemEntities = cartItems.map {
            OrderItemEntity(
                orderId = newOrderId,
                productId = it.product.id,
                quantity = it.cartItem.quantity,
                price = it.product.price
            )
        }
        orderDao.insertOrder(orderEntity)
        orderDao.insertOrderItems(orderItemEntities)
    }
}
