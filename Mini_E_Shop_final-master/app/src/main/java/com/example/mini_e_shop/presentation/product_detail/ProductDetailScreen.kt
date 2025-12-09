package com.example.mini_e_shop.presentation.product_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mini_e_shop.R
import com.example.mini_e_shop.domain.model.Product
import com.example.mini_e_shop.ui.theme.PrimaryBlue
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val successState = uiState as? ProductDetailUiState.Success

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars, // Leave space for the status bar
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.product_detail_title)) },
                windowInsets = WindowInsets.statusBars, // Avoid overlap with system bars
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Only show the Add to Cart button when product details are successfully loaded
            successState?.let {
                AddToCartBar(onAddToCart = { viewModel.onAddToCart(it.product) })
            }
        }
    ) { padding ->
        when (val state = uiState) {

            // ------------------ LOADING STATE ------------------
            is ProductDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // ------------------ SUCCESS STATE ------------------
            is ProductDetailUiState.Success -> {
                ProductDetailsContent(
                    product = state.product,
                    modifier = Modifier.padding(padding)
                )
            }

            // ------------------- ERROR STATE -------------------
            is ProductDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = PrimaryBlue.copy(alpha = 0.08f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryBlue.copy(alpha = 0.9f)
            )
            Divider(
                modifier = Modifier
                    .height(12.dp)
                    .width(1.dp),
                color = PrimaryBlue.copy(alpha = 0.2f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun StockBadge(stock: Int) {
    val (bgColor, textColor, text) = when {
        stock > 10 -> Triple(
            Color(0xFFE6F4EA),
            Color(0xFF1E8E3E),
            stringResource(R.string.stock_available, stock)
        )
        stock in 1..10 -> Triple(
            Color(0xFFFFF4E5),
            Color(0xFFB26B00),
            stringResource(R.string.stock_few_left, stock)
        )
        else -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            stringResource(R.string.out_of_stock)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private fun formatCurrency(price: Double): String {
    return "$${String.format("%.2f", price)}"
}

@Composable
fun ProductDetailsContent(
    product: Product,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // ------------------- PRODUCT IMAGE -------------------
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(product.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )

        // ---------------- PRODUCT INFORMATION ----------------
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = product.brand,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatCurrency(product.price),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )

            // ----------- QUICK INFORMATION TAGS ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(label = stringResource(R.string.category), value = product.category)
                InfoChip(label = stringResource(R.string.origin), value = product.origin)
            }

            StockBadge(stock = product.stock)

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.product_description),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider()

            Text(
                text = stringResource(R.string.product_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            InfoRow(label = stringResource(R.string.brand), value = product.brand)
            InfoRow(label = stringResource(R.string.category), value = product.category)
            InfoRow(label = stringResource(R.string.origin), value = product.origin)

            InfoRow(
                label = stringResource(R.string.stock_label),
                value = if (product.stock > 0)
                    stringResource(R.string.stock_count, product.stock)
                else
                    stringResource(R.string.out_of_stock)
            )
        }
    }
}

@Composable
private fun AddToCartBar(onAddToCart: () -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 6.dp,
        color = Color.White
    ) {
        Button(
            onClick = onAddToCart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.AddShoppingCart,
                contentDescription = stringResource(R.string.add_to_cart)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_to_cart_full),
                fontSize = 16.sp
            )
        }
    }
}
