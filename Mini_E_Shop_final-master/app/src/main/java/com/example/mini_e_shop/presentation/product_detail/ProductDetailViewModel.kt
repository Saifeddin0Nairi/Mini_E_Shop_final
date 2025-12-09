package com.example.mini_e_shop.presentation.product_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_e_shop.domain.model.Product
import com.example.mini_e_shop.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.mini_e_shop.domain.repository.CartRepository
import com.example.mini_e_shop.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull

// ---------------------------------------------
// UI state definition for the product detail screen
// ---------------------------------------------
sealed class ProductDetailUiState {
    data object Loading : ProductDetailUiState()
    data class Success(val product: Product) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        // Retrieve productId from navigation arguments
        val productId: String? = savedStateHandle.get("productId")
        if (productId != null) {
            fetchProductDetails(productId)
        } else {
            _uiState.value = ProductDetailUiState.Error("Product ID not found.")
        }
    }

    /**
     * Fetch product information from the repository.
     */
    private fun fetchProductDetails(productId: String) {
        viewModelScope.launch {
            try {
                val product = productRepository.getProductById(productId)
                if (product != null) {
                    _uiState.value = ProductDetailUiState.Success(product)
                } else {
                    _uiState.value = ProductDetailUiState.Error("Product not found.")
                }
            } catch (e: Exception) {
                _uiState.value = ProductDetailUiState.Error("Error loading data: ${e.message}")
            }
        }
    }

    /**
     * Add the selected product to the current user's cart.
     */
    fun onAddToCart(product: Product) {
        viewModelScope.launch {
            try {
                // Get the currently logged-in user
                val currentUser = userRepository.getCurrentUser().firstOrNull()

                if (currentUser != null) {

                    // Call CartRepository with required parameters: product + userId
                    cartRepository.addProductToCart(product, currentUser.id)

                    // TODO: Trigger success event (snackbar, toast, etc.)
                } else {
                    // TODO: Handle case where user cannot be found (e.g., logged out)
                }

            } catch (e: Exception) {
                // TODO: Handle add-to-cart errors (e.g., show UI message)
            }
        }
    }
}
