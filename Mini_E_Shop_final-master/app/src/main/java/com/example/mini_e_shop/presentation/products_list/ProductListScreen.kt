package com.example.mini_e_shop.presentation.products_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mini_e_shop.R
import com.example.mini_e_shop.domain.model.Product
import com.example.mini_e_shop.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    isAdmin: Boolean,
    onNavigateToAddEditProduct: (String?) -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateToSupport: () -> Unit
) {
    // Debug: log isAdmin value
    android.util.Log.d("ProductListScreen", "isAdmin value: $isAdmin")

    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSortType by viewModel.selectedSortType.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val rememberedContext = remember { context }

    // Prepare string resources outside LaunchedEffect
    val productOutOfStockMessage = stringResource(R.string.product_out_of_stock)
    val addToCartErrorMessage = stringResource(R.string.add_to_cart_error)
    val favoriteActionErrorMessage = stringResource(R.string.favorite_action_error)
    val pleaseLoginMessage = stringResource(R.string.please_login)

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            scope.launch {
                val translatedMessage = when {
                    event.startsWith("ADD_TO_CART_SUCCESS:") -> {
                        val productName = event.removePrefix("ADD_TO_CART_SUCCESS:")
                        rememberedContext.resources.getString(
                            R.string.add_to_cart_success,
                            productName
                        )
                    }

                    event.startsWith("ADD_TO_FAVORITES_SUCCESS:") -> {
                        val productName = event.removePrefix("ADD_TO_FAVORITES_SUCCESS:")
                        rememberedContext.resources.getString(
                            R.string.add_to_favorites_success,
                            productName
                        )
                    }

                    event.startsWith("REMOVE_FROM_FAVORITES_SUCCESS:") -> {
                        val productName = event.removePrefix("REMOVE_FROM_FAVORITES_SUCCESS:")
                        rememberedContext.resources.getString(
                            R.string.remove_from_favorites_success,
                            productName
                        )
                    }

                    event == "PRODUCT_OUT_OF_STOCK" -> productOutOfStockMessage
                    event == "ADD_TO_CART_ERROR" -> addToCartErrorMessage
                    event == "FAVORITE_ACTION_ERROR" -> favoriteActionErrorMessage
                    event == "PLEASE_LOGIN" -> pleaseLoginMessage
                    event == "PERMISSION_DENIED" ->
                        rememberedContext.resources.getString(R.string.permission_denied)

                    else -> event
                }
                snackbarHostState.showSnackbar(
                    message = translatedMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Debug: log isAdmin before rendering Scaffold
    LaunchedEffect(isAdmin) {
        android.util.Log.d("ProductListScreen", "Scaffold rendering with isAdmin: $isAdmin")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            // Always show FAB if user is admin
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        android.util.Log.d(
                            "ProductListScreen",
                            "FAB clicked, navigating to add product, isAdmin: $isAdmin"
                        )
                        onNavigateToAddEditProduct(null)
                    },
                    containerColor = PrimaryIndigo,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        // Don't use Scaffold's default insets, handle manually
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CompactHeader(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.onSearchQueryChange(it) },
                onSupportClick = onNavigateToSupport
            )

            when (val state = uiState) {
                is ProductListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ProductListUiState.Success -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Reduced vertical padding from 8.dp to 4.dp
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        CompactSortButton(
                            selectedSortType = selectedSortType,
                            onSortTypeSelected = { viewModel.onSortTypeSelected(it) }
                        )

                        CategoryTabs(
                            categories = state.categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { viewModel.onCategorySelected(it) }
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        // Increased bottom padding so bottom nav doesn't cover last row
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.products) { product ->
                            ProductCard(
                                product = product,
                                isAdmin = isAdmin,
                                onEdit = { onNavigateToAddEditProduct(product.id) },
                                onDelete = { viewModel.deleteProduct(product) },
                                onClick = { onProductClick(product.id) },
                                onAddToCart = { viewModel.addToCart(product) },
                                onToggleFavorite = { viewModel.toggleFavorite(product) },
                                isFavorite = state.favoriteStatusMap[product.id] ?: false
                            )
                        }
                    }
                }

                is ProductListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message)
                    }
                }
            }
        }
    }
}

@Composable
fun CompactHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSupportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Use windowInsetsPadding to align with top system bar
            .windowInsetsPadding(WindowInsets.statusBars)
            // Small top padding to keep it close to the top edge
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.dinh_manh_electronics),
                // Reduced from headlineSmall to titleLarge
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    // Reduced from 48.dp to 40.dp
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(HomeColor.copy(alpha = 0.1f))
                    .clickable(onClick = onSupportClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ContactSupport,
                    contentDescription = "Support",
                    tint = HomeColor,
                    // Reduced from 24.dp to 20.dp
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        // Reduced from 12.dp to 8.dp
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_at_dinh_manh),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = PrimaryIndigo,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
        )
    }
}

@Composable
private fun CompactSortButton(
    selectedSortType: SortType,
    onSortTypeSelected: (SortType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(start = 16.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selectedSortType != SortType.NONE) PrimaryIndigo
                    else MaterialTheme.colorScheme.surface
                )
                .border(
                    1.dp,
                    if (selectedSortType != SortType.NONE) PrimaryIndigo
                    else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(12.dp)
                )
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Sort,
                contentDescription = "Sort",
                tint =
                    if (selectedSortType != SortType.NONE)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val options = listOf(
                SortType.PRICE_ASC to stringResource(R.string.price_asc),
                SortType.PRICE_DESC to stringResource(R.string.price_desc),
                SortType.NAME_ASC to stringResource(R.string.name_asc)
            )

            options.forEach { (type, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSortTypeSelected(type)
                        expanded = false
                    },
                    leadingIcon = {
                        if (selectedSortType == type) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                        }
                    }
                )
            }
            Divider()
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.default_sort),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                },
                onClick = {
                    onSortTypeSelected(SortType.NONE)
                    expanded = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        // Reduced horizontal padding from 12.dp to 8.dp
        contentPadding = PaddingValues(horizontal = 8.dp),
        // Reduced spacing from 8.dp to 6.dp
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = getCategoryDisplayName(category),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryIndigo,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(20.dp),
                // Reduced height from 32.dp to 28.dp
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

@Composable
private fun getCategoryDisplayName(category: String): String {
    // Note: category values can be Vietnamese or English; we map them to string resources
    return when (category) {
        "All", "Tất cả" -> stringResource(R.string.category_all)
        "Điện thoại", "Phone" -> stringResource(R.string.category_phone)
        "Laptop" -> stringResource(R.string.category_laptop)
        "Tai nghe", "Headphone" -> stringResource(R.string.category_headphone)
        "Máy tính bảng", "Tablet" -> stringResource(R.string.category_tablet)
        "Đồng hồ", "Watch" -> stringResource(R.string.category_watch)
        "Máy ảnh", "Camera" -> stringResource(R.string.category_camera)
        "Gaming" -> stringResource(R.string.category_gaming)
        "Màn hình", "Monitor" -> stringResource(R.string.category_monitor)
        "Phụ kiện", "Accessory" -> stringResource(R.string.category_accessory)
        else -> category
    }
}

// --- NEW PRODUCT CARD (LAYOUT SPACE OPTIMIZED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean
) {
    val isOutOfStock = product.stock == 0
    // Mocked rating & sold count just to demonstrate UI (you can later move this into Product model)
    val rating = 4.8
    val soldCount = 120

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Increased height to fit all info
            .height(285.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { if (!isOutOfStock) onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- PART 1: IMAGE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Favorite button / Admin actions
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    // Debug: log isAdmin for each product card
                    LaunchedEffect(isAdmin) {
                        android.util.Log.d(
                            "ProductCard",
                            "isAdmin in ProductCard: $isAdmin, product: ${product.name}"
                        )
                    }

                    if (isAdmin) {
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = if (isFavorite)
                                Icons.Filled.Favorite
                            else
                                Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite)
                                Color.Red
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    CircleShape
                                )
                                .padding(2.dp)
                                .clickable(onClick = onToggleFavorite)
                        )
                    }
                }

                // Out of stock overlay
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.out_of_stock_uppercase),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // --- PART 2: DETAILS ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                // Brand tag
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = product.brand.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }

                // Rating & Sold count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "$rating",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sold_count, soldCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price & Add-to-cart button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.0f", product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                        fontSize = 18.sp
                    )

                    if (!isAdmin && !isOutOfStock) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CartColor)
                                .clickable(onClick = onAddToCart),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AddShoppingCart,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
