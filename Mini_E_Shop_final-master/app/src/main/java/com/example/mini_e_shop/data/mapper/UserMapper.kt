package com.example.mini_e_shop.data.mapper

import com.example.mini_e_shop.data.local.entity.UserEntity
import com.example.mini_e_shop.domain.model.User

fun UserEntity.toUser(): User {
    return User(
        id = this.id,
        email = this.email,

        name = this.name,
        isAdmin = this.isAdmin
    )
}

fun User.toUserEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        email = this.email,

        name = this.name,
        isAdmin = this.isAdmin
    )
}
