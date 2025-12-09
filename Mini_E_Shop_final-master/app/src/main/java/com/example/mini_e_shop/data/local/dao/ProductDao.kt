package com.example.mini_e_shop.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.mini_e_shop.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cho Product.
 */
@Dao
interface ProductDao {


    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>


    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?


    @Query("SELECT * FROM products WHERE category = :category ORDER BY price ASC")
    suspend fun getProductsByCategorySortedByPrice(category: String): List<ProductEntity>


    @Upsert
    suspend fun upsertProduct(product: ProductEntity)


    @Upsert
    suspend fun upsertProducts(products: List<ProductEntity>)


    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Int)


    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    suspend fun decreaseStock(productId: String, quantity: Int)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)


    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
