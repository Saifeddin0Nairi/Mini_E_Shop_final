package com.example.mini_e_shop.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_e_shop.domain.model.Product
import com.example.mini_e_shop.domain.repository.CartRepository
import com.example.mini_e_shop.domain.repository.FavoriteRepository
import com.example.mini_e_shop.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI state of the Favorites screen
sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()
    data class Success(val favoriteProducts: List<Product>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    // uiState will automatically update whenever the user’s favorites list changes
    val uiState: StateFlow<FavoritesUiState> = userRepository.getCurrentUser()
        .flatMapLatest { user ->
            if (user != null) {
                // If a user exists, listen to their favorite product list
                favoriteRepository.getFavoriteProducts(user.id)
                    .map<List<Product>, FavoritesUiState> { products ->
                        FavoritesUiState.Success(products)
                    }
            } else {
                // If there's no user, return an empty list
                flowOf(FavoritesUiState.Success(emptyList()))
            }
        }
        .catch { e ->
            // Catch any errors and emit an error state
            emit(FavoritesUiState.Error(e.message ?: "Unknown error occurred"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState.Loading
        )

    // Removes a product from favorites directly from the Favorites screen
    fun removeFromFavorites(product: Product) {
        viewModelScope.launch {
            userRepository.getCurrentUser().firstOrNull()?.let { user ->
                // toggleFavorite handles adding/removing automatically
                favoriteRepository.toggleFavorite(productId = product.id, userId = user.id)
            }
        }
    }

    // Adds a product to the cart
    fun addToCart(product: Product) {
        viewModelScope.launch {
            userRepository.getCurrentUser().firstOrNull()?.let { user ->
                try {
                    if (product.stock > 0) {
                        cartRepository.addProductToCart(product, user.id)
                    }
                } catch (e: Exception) {
                    // Error handling if needed
                }
            }
        }
    }
}
