package com.example.mini_e_shop.base

import android.app.Application
import com.example.mini_e_shop.data.local.SampleData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    // Function to force seed/update all products to Firebase
    // Can be called from anywhere in the app when needed
    fun seedProductsToFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val productCollection = firestore.collection("products")
        val sampleProducts = SampleData.getSampleProducts()

        println("Firestore: Starting to seed/update ${sampleProducts.size} products...")

        val batchSize = 500
        val batches = sampleProducts.chunked(batchSize)

        fun processBatch(batchIndex: Int) {
            if (batchIndex >= batches.size) {
                println("Firestore: Successfully seeded/updated all ${sampleProducts.size} products.")
                return
            }

            val batch = firestore.batch()
            val currentBatch = batches[batchIndex]

            currentBatch.forEach { productEntity ->
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
                val docRef = productCollection.document(productEntity.id)
                // Use merge to update if the document already exists, create a new one if it doesn't
                batch.set(docRef, productData, SetOptions.merge())
            }

            batch.commit()
                .addOnSuccessListener {
                    println("Firestore: Batch ${batchIndex + 1}/${batches.size} completed (${currentBatch.size} products).")
                    processBatch(batchIndex + 1)
                }
                .addOnFailureListener { e ->
                    println("Firestore: Error seeding batch ${batchIndex + 1}: $e")
                }
        }

        processBatch(0)
    }
}
