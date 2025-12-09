package com.example.mini_e_shop.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mini_e_shop.data.preferences.UserPreferencesManager
import com.example.mini_e_shop.domain.model.Order
import com.example.mini_e_shop.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    val orderState: StateFlow<OrderUiState> = userPreferencesManager.authPreferencesFlow
        .flatMapLatest { prefs ->
            if (prefs.isLoggedIn) {
                orderRepository.getOrdersForUser(prefs.loggedInUserId)
            } else {
                // Should not happen if the user is logged in, but as a fallback, return empty list
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .map { orders ->
            if (orders.isEmpty()) {
                OrderUiState.Empty
            } else {
                OrderUiState.Success(orders)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OrderUiState.Loading
        )
}

sealed class OrderUiState {
    object Loading : OrderUiState()
    object Empty : OrderUiState()
    data class Success(val orders: List<Order>) : OrderUiState()
}
