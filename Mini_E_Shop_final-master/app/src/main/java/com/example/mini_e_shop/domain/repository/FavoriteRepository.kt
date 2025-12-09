package com.example.mini_e_shop.domain.repository

import com.example.mini_e_shop.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavoriteProductIds(userId: String): Flow<Set<String>>
    suspend fun toggleFavorite(productId: String, userId: String)
    fun getFavoriteProducts(userId: String): Flow<List<Product>>

    fun isFavorite(productId: String, userId: String): Flow<Boolean>
}