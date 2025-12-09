package com.example.mini_e_shop.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mini_e_shop.R

sealed class Screen(
    val route: String,
    @StringRes val title: Int? = null,
    val icon: ImageVector? = null
) {
    object Login : Screen(route = "login")
    object Register : Screen(route = "register")

    object Main : Screen(route = "main")

    object Home : Screen(
        route = "home",
        title = R.string.home,
        icon = Icons.Default.Home
    )
    object Favorites : Screen(
        route = "favorites",
        title = R.string.favorites,
        icon = Icons.Default.Favorite
    )
    object Cart : Screen(
        route = "cart",
        title = R.string.cart,
        icon = Icons.Default.ShoppingCart
    )
    object Profile : Screen(
        route = "profile",
        title = R.string.profile,
        icon = Icons.Default.Person
    )

    object Orders : Screen(route = "orders")
    object AddEditProduct : Screen(route = "add_edit_product")
    object ProductDetail : Screen("product_detail")
    object Support : Screen("support")
    object Contact : Screen("contact")
    object Checkout : Screen("checkout")
    object Settings : Screen("settings")
}
