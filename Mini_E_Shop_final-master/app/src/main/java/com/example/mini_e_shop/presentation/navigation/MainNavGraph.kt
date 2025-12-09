package com.example.mini_e_shop.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mini_e_shop.data.local.entity.UserEntity
import com.example.mini_e_shop.presentation.cart.CartScreen
import com.example.mini_e_shop.presentation.favorites.FavoritesScreen
import com.example.mini_e_shop.presentation.products_list.ProductListScreen
import com.example.mini_e_shop.presentation.profile.ProfileScreen

@Composable
fun MainNavGraph(
    modifier: Modifier = Modifier,
    mainNavController: NavHostController,
    bottomNavController: NavHostController,
    isAdmin: Boolean,
    currentUser: UserEntity?,
    onNavigateToOrders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAddEditProduct: (String?) -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToCheckout: (String) -> Unit
) {
    NavHost(
        navController = bottomNavController, // This NavHost uses the bottom navigation controller
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        // -----------------------------------------
        // HOME TAB
        // -----------------------------------------
        composable(Screen.Home.route) {
            android.util.Log.d("MainNavGraph", "ProductListScreen - isAdmin: $isAdmin")

            ProductListScreen(
                viewModel = hiltViewModel(),
                isAdmin = isAdmin,
                onNavigateToAddEditProduct = onNavigateToAddEditProduct,
                onProductClick = onProductClick,
                onNavigateToSupport = onNavigateToSupport
            )
        }

        // -----------------------------------------
        // FAVORITES TAB
        // -----------------------------------------
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onProductClick = onProductClick,
                onNavigateToAddEditProduct = onNavigateToAddEditProduct
            )
        }

        // -----------------------------------------
        // CART TAB
        // -----------------------------------------
        composable(Screen.Cart.route) {
            CartScreen(
                viewModel = hiltViewModel(),
                onNavigateToCheckout = onNavigateToCheckout // CartScreen must use the main NavController to navigate outside the tabs
            )
        }

        // -----------------------------------------
        // PROFILE TAB
        // -----------------------------------------
        composable(Screen.Profile.route) {
            ProfileScreen(
                currentUser = currentUser,
                onNavigateToOrders = onNavigateToOrders,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToFavorites = {
                    // Switch to the Favorites tab from inside the Profile screen
                    bottomNavController.navigate(Screen.Favorites.route)
                },
                onLogout = onLogout
            )
        }
    }
}
