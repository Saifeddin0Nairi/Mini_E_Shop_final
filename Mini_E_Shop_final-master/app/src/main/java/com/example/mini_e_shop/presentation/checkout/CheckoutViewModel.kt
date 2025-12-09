package com.example.mini_e_shop.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_e_shop.domain.repository.CartRepository
import com.example.mini_e_shop.domain.repository.OrderRepository
import com.example.mini_e_shop.domain.repository.ProductRepository
import com.example.mini_e_shop.domain.repository.UserRepository
import com.example.mini_e_shop.presentation.cart.CartItemDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for this screen
sealed class CheckoutUiState {
    object Loading : CheckoutUiState()
    data class Success(
        val items: List<CartItemDetails>,
        val totalPrice: Double
    ) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

// Events this ViewModel can send to the UI
sealed class CheckoutEvent {
    data class ShowSnackbar(val message: String) : CheckoutEvent()
    object NavigateBack : CheckoutEvent()
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<CheckoutEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    private var itemIds: List<Int> = emptyList()

    init {
        val cartItemIdsString: String? = savedStateHandle.get("cartItemIds")
        if (cartItemIdsString != null && cartItemIdsString.isNotEmpty()) {
            itemIds = cartItemIdsString.split(",").mapNotNull { it.toIntOrNull() }
            loadCheckoutItems(itemIds)
        } else {
            _uiState.value = CheckoutUiState.Error("No items to check out.")
        }
    }

    private fun loadCheckoutItems(ids: List<Int>) {
        viewModelScope.launch {
            try {
                // Get the full item details (including quantity) using the new repository method
                val items = cartRepository.getCartItemsByIds(ids)
                if (items.isNotEmpty()) {
                    val totalPrice = items.sumOf { it.product.price * it.cartItem.quantity }
                    _uiState.value = CheckoutUiState.Success(items, totalPrice)
                } else {
                    _uiState.value = CheckoutUiState.Error("Could not load item details.")
                }
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error("Error: ${e.message}")
            }
        }
    }

    // The order placement logic now lives here
    fun placeOrder() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is CheckoutUiState.Success) {
                val currentUserId = userRepository.getCurrentUser().firstOrNull()?.id ?: return@launch

                // Check stock before placing order
                for (item in currentState.items) {
                    val product = productRepository.getProductById(item.product.id)
                    if (product == null || product.stock < item.cartItem.quantity) {
                        // Use a format that can be recognized and translated in UI layer
                        _eventChannel.send(CheckoutEvent.ShowSnackbar("INSUFFICIENT_STOCK:${item.product.name}"))
                        return@launch
                    }
                }

                if (currentState.items.isNotEmpty()) {
                    orderRepository.createOrderFromCart(currentUserId, currentState.items)
                    // Remove the ordered items from the cart and update stock
                    currentState.items.forEach {
                        cartRepository.removeItem(it.cartItem.id)
                        val product = it.product
                        val newStock = product.stock - it.cartItem.quantity
                        productRepository.updateProductStock(product.id, newStock)
                    }
                    _eventChannel.send(CheckoutEvent.ShowSnackbar("Order placed successfully!"))
                    _eventChannel.send(CheckoutEvent.NavigateBack)
                }
            }
        }
    }
}
