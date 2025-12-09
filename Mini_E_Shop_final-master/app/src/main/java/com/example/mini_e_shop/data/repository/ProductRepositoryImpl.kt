package com.example.mini_e_shop.data.repository

import com.example.mini_e_shop.data.local.SampleData
import com.example.mini_e_shop.data.local.dao.ProductDao
import com.example.mini_e_shop.data.local.entity.ProductEntity
import com.example.mini_e_shop.domain.model.Product
import com.example.mini_e_shop.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val firestore: FirebaseFirestore
) : ProductRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Automatically start listening to Firestore when the Repository is created
        subscribeToRemoteProductChanges()
    }

    // This function listens to Firestore changes and updates Room database automatically
    private fun subscribeToRemoteProductChanges() {
        repositoryScope.launch {
            firestore.collection("products")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Firestore listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val remoteProducts = snapshot.toObjects(Product::class.java)
                        val productEntities = remoteProducts.map { product ->
                            product.toEntity()
                        }

                        // Update Room database with new data
                        repositoryScope.launch {
                            productDao.upsertProducts(productEntities)
                        }
                    }
                }
        }
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    override suspend fun upsertProduct(product: Product) {
        // Save to Room database first
        productDao.upsertProduct(product.toEntity())

        // Then save to Firebase
        val productData = hashMapOf(
            "name" to product.name,
            "brand" to product.brand,
            "category" to product.category,
            "origin" to product.origin,
            "price" to product.price,
            "stock" to product.stock,
            "imageUrl" to product.imageUrl,
            "description" to product.description
        )

        firestore.collection("products")
            .document(product.id)
            .set(productData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                println("Firestore: Successfully saved product ${product.id} to Firebase.")
            }
            .addOnFailureListener { e ->
                println("Firestore: Error saving product ${product.id} to Firebase: $e")
            }
    }

    override suspend fun deleteProduct(product: Product) {
        // First delete from Room
        productDao.deleteProduct(product.toEntity())

        // Then delete from Firebase
        firestore.collection("products")
            .document(product.id)
            .delete()
            .addOnSuccessListener {
                println("Firestore: Successfully deleted product ${product.id} from Firebase.")
            }
            .addOnFailureListener { e ->
                println("Firestore: Error deleting product ${product.id} from Firebase: $e")
            }
    }

    override suspend fun updateProductStock(productId: String, newStock: Int) {
        productDao.updateStock(productId, newStock)
    }
}

// --- Mapper Functions (Entity <-> Domain Converters) ---

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

private fun Product.toEntity(): ProductEntity {
    return ProductEntity(
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
