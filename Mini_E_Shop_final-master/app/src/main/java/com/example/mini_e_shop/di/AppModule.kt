package com.example.mini_e_shop.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mini_e_shop.data.local.SampleData
import com.example.mini_e_shop.data.local.dao.CartDao
import com.example.mini_e_shop.data.local.dao.FavoriteDao
import com.example.mini_e_shop.data.local.dao.OrderDao
import com.example.mini_e_shop.data.local.dao.ProductDao
import com.example.mini_e_shop.data.local.dao.UserDao
import com.example.mini_e_shop.data.local.database.AppDatabase
import com.example.mini_e_shop.data.preferences.UserPreferencesManager
import com.example.mini_e_shop.data.repository.CartRepositoryImpl
import com.example.mini_e_shop.data.repository.FavoriteRepositoryImpl
import com.example.mini_e_shop.data.repository.OrderRepositoryImpl
import com.example.mini_e_shop.data.repository.ProductRepositoryImpl
import com.example.mini_e_shop.data.repository.UserRepositoryImpl
import com.example.mini_e_shop.domain.repository.CartRepository
import com.example.mini_e_shop.domain.repository.FavoriteRepository
import com.example.mini_e_shop.domain.repository.OrderRepository
import com.example.mini_e_shop.domain.repository.ProductRepository
import com.example.mini_e_shop.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

// Custom Callback class to break the circular dependency
class DatabaseCallback @Inject constructor(
    private val userDao: Provider<UserDao>,
    private val productDao: Provider<ProductDao>,
    private val firestore: Provider<FirebaseFirestore>,
    private val firebaseAuth: Provider<FirebaseAuth>,
    private val applicationScope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // Use the injected scope to launch the coroutine
        applicationScope.launch {
            createAdminAccount()
            seedProductsToRoom()
            seedProductsToFirestore()
        }
    }

    // Create admin account and store it in Room and Firebase
    private fun createAdminAccount() {
        val adminEmail = "admin@eshop.com"
        val adminPassword = "admin123456"
        val adminName = "Administrator"

        applicationScope.launch {
            try {
                // Check if admin already exists in Room
                val existingAdmin = userDao.get().getUserByEmail(adminEmail)
                if (existingAdmin != null && existingAdmin.isAdmin) {
                    println("Admin account already exists in Room database.")
                    // Ensure admin is also present in Firestore
                    firestore.get().collection("users").document(existingAdmin.id)
                        .set(existingAdmin)
                        .addOnSuccessListener {
                            println("Admin account synced to Firestore.")
                        }
                    return@launch
                }

                // If not in Room, try creating admin in Firebase Auth
                firebaseAuth.get().createUserWithEmailAndPassword(adminEmail, adminPassword)
                    .addOnCompleteListener { createTask ->
                        if (createTask.isSuccessful) {
                            // Created successfully in Firebase Auth
                            val firebaseUser = createTask.result?.user
                            if (firebaseUser != null) {
                                val adminEntity = com.example.mini_e_shop.data.local.entity.UserEntity(
                                    id = firebaseUser.uid,
                                    email = adminEmail,
                                    name = adminName,
                                    isAdmin = true
                                )

                                // Save to Room
                                applicationScope.launch {
                                    userDao.get().insertUser(adminEntity)
                                    println("Admin account created in Room database.")
                                }

                                // Save to Firestore
                                firestore.get().collection("users").document(firebaseUser.uid)
                                    .set(adminEntity)
                                    .addOnSuccessListener {
                                        println("Admin account created in Firestore.")
                                    }
                                    .addOnFailureListener { e ->
                                        println("Error creating admin in Firestore: $e")
                                    }
                            }
                        } else {
                            // Admin may already exist in Firebase Auth but not in Room
                            val errorMessage = createTask.exception?.message ?: ""
                            if (errorMessage.contains("already exists", ignoreCase = true) ||
                                errorMessage.contains("already in use", ignoreCase = true) ||
                                errorMessage.contains("email-already-in-use", ignoreCase = true)
                            ) {

                                // Try signing in to retrieve admin info
                                firebaseAuth.get().signInWithEmailAndPassword(adminEmail, adminPassword)
                                    .addOnCompleteListener { signInTask ->
                                        if (signInTask.isSuccessful) {
                                            val firebaseUser = signInTask.result?.user
                                            if (firebaseUser != null) {
                                                val adminEntity = com.example.mini_e_shop.data.local.entity.UserEntity(
                                                    id = firebaseUser.uid,
                                                    email = adminEmail,
                                                    name = adminName,
                                                    isAdmin = true
                                                )

                                                // Save to Room
                                                applicationScope.launch {
                                                    userDao.get().insertUser(adminEntity)
                                                    println("Admin account synced to Room database.")
                                                }

                                                // Save to Firestore
                                                firestore.get().collection("users").document(firebaseUser.uid)
                                                    .set(adminEntity)
                                                    .addOnSuccessListener {
                                                        println("Admin account synced to Firestore.")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        println("Error syncing admin to Firestore: $e")
                                                    }
                                            }
                                        } else {
                                            println("Error signing in admin: ${signInTask.exception?.message}")
                                        }
                                    }
                            } else {
                                println("Error creating admin account in Firebase Auth: ${createTask.exception?.message}")
                            }
                        }
                    }
            } catch (e: Exception) {
                println("Error checking/creating admin account: $e")
            }
        }
    }

    // Seed 41 products into the Room database
    private suspend fun seedProductsToRoom() {
        try {
            val productCount = productDao.get().getProductCount()
            if (productCount == 0) {
                println("Room: Seeding ${SampleData.getSampleProducts().size} products to Room database...")
                productDao.get().upsertProducts(SampleData.getSampleProducts())
                println("Room: Successfully seeded products to Room database.")
            } else {
                println("Room: Database already contains $productCount products. No seeding needed.")
            }
        } catch (e: Exception) {
            println("Room: Error seeding products: $e")
        }
    }

    private fun seedProductsToFirestore() {
        val productCollection = firestore.get().collection("products")
        val sampleProducts = SampleData.getSampleProducts()

        println("Firestore: Starting to seed/update ${sampleProducts.size} products...")

        // Split into smaller batches to avoid exceeding the 500-operations limit
        val batchSize = 500
        val batches = sampleProducts.chunked(batchSize)

        fun processBatch(batchIndex: Int) {
            if (batchIndex >= batches.size) {
                println("Firestore: Successfully seeded/updated all ${sampleProducts.size} products.")
                return
            }

            val batch = firestore.get().batch()
            val currentBatch = batches[batchIndex]

            currentBatch.forEach { productEntity ->
                // Convert ProductEntity to a Map to write to Firestore
                val productData = hashMapOf(
                    "name" to productEntity.name,
                    "brand" to productEntity.brand,
                    "category" to productEntity.category,
                    "origin" to productEntity.origin,
                    "price" to productEntity.price,
                    "stock" to productEntity.stock,
                    "imageUrl" to productEntity.imageUrl,
                    "description" to productEntity.description
                )
                // Use the ID from SampleData as the document ID in Firestore.
                // Use set() with merge to update if exists, or create if not.
                val docRef = productCollection.document(productEntity.id)
                batch.set(docRef, productData, com.google.firebase.firestore.SetOptions.merge())
            }

            batch.commit()
                .addOnSuccessListener {
                    println("Firestore: Batch ${batchIndex + 1}/${batches.size} completed (${currentBatch.size} products).")
                    // Process next batch
                    processBatch(batchIndex + 1)
                }
                .addOnFailureListener { e ->
                    println("Firestore: Error seeding batch ${batchIndex + 1}: $e")
                }
        }

        // Start processing the first batch
        processBatch(0)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        app: Application,
        callback: DatabaseCallback
    ): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "mini_e_shop.db"
        )
            .addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    @Singleton
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    @Singleton
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
}

// This module was originally commented as "add this missing module"
//@Module
//@InstallIn(SingletonComponent::class)
//object FirebaseModule {
//
//    @Provides
//    @Singleton
//    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
//
//    @Provides
//    @Singleton
//    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()
//
//    @Provides
//    @Singleton
//    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
//}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(orderRepositoryImpl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(cartRepositoryImpl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(favoriteRepositoryImpl: FavoriteRepositoryImpl): FavoriteRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides
    @Singleton
    fun provideUserPreferencesManager(@ApplicationContext context: Context): UserPreferencesManager {
        return UserPreferencesManager(context)
    }
}

// Module to provide a Singleton CoroutineScope that lives as long as the application
@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        // SupervisorJob ensures that if one child coroutine fails, the scope is not cancelled
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
