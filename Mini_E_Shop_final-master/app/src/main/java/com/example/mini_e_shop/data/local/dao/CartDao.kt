package com.example.mini_e_shop.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.example.mini_e_shop.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.Embedded
import androidx.room.Relation
import com.example.mini_e_shop.data.local.entity.ProductEntity


@Dao
interface CartDao {

    @Transaction
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItemsWithProducts(userId: String): Flow<List<CartItemWithProduct>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItemsByUser(userId: String): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :newQuantity WHERE id = :cartItemId")
    suspend fun updateQuantity(cartItemId: Int, newQuantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteCartItem(cartItemId: Int)

    @Delete
    suspend fun removeFromCart(cartItem: CartItemEntity)
    @Upsert
    suspend fun upsertCartItem(cartItem: CartItemEntity)
    @Query("SELECT * FROM cart_items WHERE id IN (:cartItemIds)")
    suspend fun getCartItemsByIds(cartItemIds: List<Int>): List<CartItemWithProduct>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun getCartItem(userId: String, productId: String): CartItemEntity?

}
