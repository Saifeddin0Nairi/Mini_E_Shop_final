package com.example.mini_e_shop.data.repository

import com.example.mini_e_shop.data.local.dao.CartDao
import com.example.mini_e_shop.data.local.dao.CartItemWithProduct
import com.example.mini_e_shop.data.local.entity.CartItemEntity
import com.example.mini_e_shop.data.local.entity.ProductEntity
import com.example.mini_e_shop.data.mapper.toCartItemDetails
import com.example.mini_e_shop.domain.model.CartItem
import com.example.mini_e_shop.domain.model.Product
import com.example.mini_e_shop.domain.repository.CartRepository
import com.example.mini_e_shop.presentation.cart.CartItemDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItems(userId: String): Flow<List<CartItemDetails>> {
        return cartDao.getCartItemsWithProducts(userId).map {
            it.map { cartItemWithProduct ->
                cartItemWithProduct.toCartItemDetails()
            }
        }
    }

    override suspend fun addProductToCart(product: Product, userId: String) {
        // Check if the product already exists in this user's cart
        val existingItem = cartDao.getCartItem(userId, product.id)

        if (existingItem != null) {
            // If it exists, just increase the quantity
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            cartDao.upsertCartItem(updatedItem)
        } else {
            // If it does not exist, create a new cart item
            val newItem = CartItemEntity(
                userId = userId,
                // We assign the ID directly since both are Strings (no need for toInt())
                productId = product.id,
                quantity = 1
            )
            cartDao.upsertCartItem(newItem)
        }

        // TODO: Upgrade to sync cart changes with Firestore
    }

    override suspend fun getCartItemsByIds(cartItemIds: List<Int>): List<CartItemDetails> {
        return cartDao.getCartItemsByIds(cartItemIds).map { cartItemWithProduct ->
            cartItemWithProduct.toCartItemDetails()
        }
    }

    override suspend fun updateQuantity(cartItemId: Int, newQuantity: Int) {
        cartDao.updateQuantity(cartItemId, newQuantity)
    }

    override suspend fun removeItem(cartItemId: Int) {
        cartDao.deleteCartItem(cartItemId)
    }

    override suspend fun clearCart(userId: String) {
        cartDao.clearCart(userId)
    }
}

// --- MAPPERS SECTION ---

private fun CartItemWithProduct.toCartItemDetails(): CartItemDetails {
    return CartItemDetails(
        cartItem = this.cartItem.toDomain(),
        product = this.product.toDomain()
    )
}

private fun CartItemEntity.toDomain(): CartItem {
    return CartItem(
        id = this.id,
        userId = this.userId,
        productId = this.productId,
        quantity = this.quantity
    )
}

private fun ProductEntity.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        brand = this.brand,
        category = this.category,
        origin = this.origin,
        price = this.price,
        stock = this.stock,
        imageUrl = this.imageUrl,
        description = this.description
    )
}
