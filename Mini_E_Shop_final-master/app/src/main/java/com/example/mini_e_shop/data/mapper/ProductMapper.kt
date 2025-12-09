package com.example.mini_e_shop.data.mapper

import com.example.mini_e_shop.data.local.entity.ProductEntity
import com.example.mini_e_shop.domain.model.Product

fun ProductEntity.toProduct(): Product {
    return Product(
        id = id,
        name = name,
        brand = brand,
        category = category,
        origin = origin,
        price = price,
        stock = stock,
        imageUrl = imageUrl,
        description = description
    )
}

fun Product.toProductEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        name = name,
        brand = brand,
        category = category,
        origin = origin,
        price = price,
        stock = stock,
        imageUrl = imageUrl,
        description = description
    )
}
