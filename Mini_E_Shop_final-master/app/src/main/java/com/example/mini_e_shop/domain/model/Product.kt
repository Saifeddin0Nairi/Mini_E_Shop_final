package com.example.mini_e_shop.domain.model

import com.google.firebase.firestore.DocumentId

data class Product(
    @get:DocumentId
    val id: String = "", 
    val name: String = "",
    val brand: String = "",
    val category: String = "",
    val origin: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val description: String = ""
)
