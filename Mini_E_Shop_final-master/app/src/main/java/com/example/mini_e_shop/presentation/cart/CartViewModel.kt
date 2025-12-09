package com.example.mini_e_shop.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_e_shop.domain.model.Product
import com.example.mini_e_shop.domain.repository.CartRepository
import com.example.mini_e_shop.domain.repository.OrderRepository
import com.example.mini_e_shop.domain.repository.ProductRepository
import com.example.mini_e_shop.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Wrapper class used to manage the "isSelected" state for UI
data class SelectableCartItem(
    val details: CartItemDetails,
    val isSelected: Boolean = true // Default: selected when loaded into cart
)

// Updated UI state model for the cart screen
sealed class CartUiState {
    object Loading : CartUiState()
    object Empty : CartUiState()
    data class Success(
        val selectableItems: List<SelectableCartItem> = emptyList(),
        val checkoutPrice: Double = 0.0,
        val isAllSelected: Boolean = true
    ) : CartUiState()
}

// Sealed class for events sent from ViewModel to UI
sealed class CartViewEvent {
    data class NavigateToCheckout(val cartItemIds: String) : CartViewEvent()
    data class ShowSnackbar(val message: String) : CartViewEvent()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<SelectableCartItem>>(emptyList())
    private val _eventChannel = Channel<CartViewEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    // Final UI state produced for the UI after processing cart items
    val uiState: StateFlow<CartUiState> = _cartItems
        .map { items ->
            if (items.isEmpty()) {
                CartUiState.Empty
            } else {
                // Calculate total price based only on selected items
                val checkoutPrice = items.filter { it.isSelected }
                    .sumOf { it.details.product.price * it.details.cartItem.quantity }

                // Check whether all items are selected
                val isAllSelected = items.isNotEmpty() && items.all { it.isSelected }

                CartUiState.Success(
                    selectableItems = items,
                    checkoutPrice = checkoutPrice,
                    isAllSelected = isAllSelected
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CartUiState.Loading
        )

    init {
        observeCartItems()
    }

    // Observes changes in user and then listens to that user's cart items
    private fun observeCartItems() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .flatMapLatest { user ->
                    if (user != null) {
                        cartRepository.getCartItems(user.id)
                    } else {
                        flowOf(emptyList()) // No user → empty cart
                    }
                }
                .collect { cartItemDetails ->
                    // When DB updates, sync the UI list while keeping existing selection states
                    val currentSelection = _cartItems.value.associateBy { it.details.cartItem.id }

                    _cartItems.value = cartItemDetails.map { detail ->
                        SelectableCartItem(
                            details = detail,
                            isSelected = currentSelection[detail.cartItem.id]?.isSelected ?: true
                        )
                    }
                }
        }
    }

    // When user toggles a single checkmark
    fun onItemCheckedChanged(cartItemId: Int, isChecked: Boolean) {
        _cartItems.value = _cartItems.value.map {
            if (it.details.cartItem.id == cartItemId) {
                it.copy(isSelected = isChecked)
            } else it
        }
    }

    // When user selects or deselects all items
    fun onSelectAllChecked(isChecked: Boolean) {
        _cartItems.value = _cartItems.value.map { it.copy(isSelected = isChecked) }
    }

    // Quantity change handler (increase / decrease)
    fun onQuantityChange(cartItemId: Int, newQuantity: Int) {
        viewModelScope.launch {
            val item = _cartItems.value.find { it.details.cartItem.id == cartItemId }

            if (item != null) {
                val product = item.details.product
                if (newQuantity > product.stock) {
                    _eventChannel.send(
                        CartViewEvent.ShowSnackbar(
                            "Quantity exceeds available stock."
                        )
                    )
                    return@launch
                }
            }

            if (newQuantity > 0) {
                cartRepository.updateQuantity(cartItemId, newQuantity)
            } else {
                cartRepository.removeItem(cartItemId)
            }
        }
    }

    // Creates an order from selected items
    fun placeOrder() {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUser().firstOrNull()?.id ?: return@launch
            val itemsToCheckout = _cartItems.value.filter { it.isSelected }.map { it.details }

            if (itemsToCheckout.isNotEmpty()) {
                orderRepository.createOrderFromCart(currentUserId, itemsToCheckout)

                // Remove only the items that were purchased, not the whole cart
                itemsToCheckout.forEach {
                    cartRepository.removeItem(it.cartItem.id)

                    val product = it.product
                    val newStock = product.stock - it.cartItem.quantity
                    productRepository.updateProductStock(product.id, newStock)
                }
            }
        }
    }

    // Triggered when the user presses the "Buy Now / Checkout" button
    fun onCheckoutClick() {
        viewModelScope.launch {
            val selectedIds = _cartItems.value
                .filter { it.isSelected }
                .map { it.details.cartItem.id }
                .joinToString(separator = ",")

            if (selectedIds.isNotEmpty()) {
                _eventChannel.send(CartViewEvent.NavigateToCheckout(selectedIds))
            }
        }
    }
}
