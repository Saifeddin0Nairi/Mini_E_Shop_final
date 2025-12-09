package com.example.mini_e_shop.domain.repository

import com.example.mini_e_shop.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the Product Repository.
 * Defines the methods used to interact with product data.
 */
interface ProductRepository {

    /**
     * Returns all products as a Flow, allowing real-time observation of changes.
     */
    fun getAllProducts(): Flow<List<Product>>

    /**
     * Finds a product by its ID.
     */
    suspend fun getProductById(id: String): Product?

    /**
     * Inserts or updates a product (upsert).
     */
    suspend fun upsertProduct(product: Product)

    /**
     * Deletes a product.
     */
    suspend fun deleteProduct(product: Product)

    /**
     * Updates the stock quantity of a product.
     */
    suspend fun updateProductStock(productId: String, newStock: Int)
}
