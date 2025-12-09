package com.example.mini_e_shop.data.repository

import com.example.mini_e_shop.data.local.dao.FavoriteDao
import com.example.mini_e_shop.data.local.entity.FavoriteEntity
import com.example.mini_e_shop.data.local.entity.ProductEntity
import com.example.mini_e_shop.domain.model.Product
import com.example.mini_e_shop.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {
    override fun getFavoriteProductIds(userId: String): Flow<Set<String>> {
        return favoriteDao.getFavoriteProductIds(userId).map { it.toSet() }
    }

    override suspend fun toggleFavorite(productId: String, userId: String) {
        val isFavorite = favoriteDao.isFavorite(userId, productId)
        if (isFavorite) {
            favoriteDao.removeFavorite(userId, productId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(userId = userId, productId = productId))
        }
    }

    // EDIT: Add the new function implementation here
    override fun getFavoriteProducts(userId: String): Flow<List<Product>> {
        return favoriteDao.getFavoriteProducts(userId).map { favoriteProductEntities ->
            favoriteProductEntities.map { it.toDomain() }
        }
    }
    override fun isFavorite(productId: String, userId: String): Flow<Boolean> {
        return favoriteDao.getFavoriteProductIds(userId).map { favoriteIds ->
            productId in favoriteIds
        }
    }
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