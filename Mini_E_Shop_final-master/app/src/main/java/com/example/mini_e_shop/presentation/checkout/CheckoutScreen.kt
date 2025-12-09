package com.example.mini_e_shop.presentation.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mini_e_shop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    // EDIT: Remove CartViewModel
    checkoutViewModel: CheckoutViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    // EDIT: Add a lambda to show snackbar from MainActivity
    onShowSnackbar: (String) -> Unit
) {
    val uiState by checkoutViewModel.uiState.collectAsState()
    
    // Get string resources for translation (outside LaunchedEffect)
    val orderSuccessMessage = stringResource(R.string.checkout_order_success)

    // EDIT: Listen for events from CheckoutViewModel
    LaunchedEffect(key1 = true) {
        checkoutViewModel.eventFlow.collect { event ->
            when(event) {
                is CheckoutEvent.NavigateBack -> onNavigateBack()
                is CheckoutEvent.ShowSnackbar -> {
                    val translatedMessage = when {
                        event.message.startsWith("INSUFFICIENT_STOCK:") -> {
                            val productName = event.message.removePrefix("INSUFFICIENT_STOCK:")
                            // Format message with product name (will be translated in UI if needed)
                            "Sản phẩm '$productName' không đủ hàng trong kho."
                        }
                        event.message == "Order placed successfully!" -> orderSuccessMessage
                        else -> event.message
                    }
                    onShowSnackbar(translatedMessage)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkout_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            if (uiState is CheckoutUiState.Success) {
                val successState = uiState as CheckoutUiState.Success
                Button(
                    // EDIT: Call the placeOrder function on the correct ViewModel
                    onClick = { checkoutViewModel.placeOrder() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.checkout_confirm_order, successState.totalPrice), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is CheckoutUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CheckoutUiState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        item {
                            Text(stringResource(R.string.checkout_products_in_order), style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        items(state.items) { cartItemDetail ->
                            // TODO: Create a nicer Composable to display the item
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${cartItemDetail.product.name} (x${cartItemDetail.cartItem.quantity})")
                                Text("$${cartItemDetail.product.price * cartItemDetail.cartItem.quantity}")
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
                is CheckoutUiState.Error -> {
                    val errorMessage = when (state.message) {
                        "No items to check out." -> stringResource(R.string.checkout_no_items)
                        "Could not load item details." -> stringResource(R.string.checkout_load_error)
                        else -> {
                            if (state.message.startsWith("Error: ")) {
                                val errorDetail = state.message.removePrefix("Error: ")
                                stringResource(R.string.checkout_error, errorDetail)
                            } else {
                                state.message
                            }
                        }
                    }
                    Text(errorMessage, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

