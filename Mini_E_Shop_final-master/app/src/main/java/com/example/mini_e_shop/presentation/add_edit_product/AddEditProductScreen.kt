package com.example.mini_e_shop.presentation.add_edit_product

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.mini_e_shop.R
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: AddEditProductViewModel = hiltViewModel(),
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    // Updated logic: listen to `saveEvent` from ViewModel.
    // When the ViewModel finishes saving and emits the event, call `onSave()`.
    LaunchedEffect(key1 = true) {
        viewModel.saveEvent.collectLatest {
            onSave() // Navigate back only after saving is complete.
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            // Use TopAppBar to provide clear Back and Save actions.
            TopAppBar(
                title = { Text(stringResource(R.string.add_edit_product)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { // Call onBack when the back button is pressed.
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveProduct() }) {
                        Icon(Icons.Default.Done, contentDescription = "Save Product")
                    }
                },
                // Use statusBars insets so padding is automatically handled
                windowInsets = WindowInsets.statusBars
            )
        }

    ) { padding ->
        // Read all state from the ViewModel.
        val name by viewModel.name.collectAsState()
        val brand by viewModel.brand.collectAsState()
        val category by viewModel.category.collectAsState()
        val origin by viewModel.origin.collectAsState()
        val price by viewModel.price.collectAsState()
        val stock by viewModel.stock.collectAsState()
        val imageUrl by viewModel.imageUrl.collectAsState()
        val description by viewModel.description.collectAsState()

        // Add remaining text fields and enable vertical scrolling.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Image URL input and preview area ---
            OutlinedTextField(
                value = imageUrl,
                onValueChange = viewModel::onImageUrlChange,
                label = { Text(stringResource(R.string.enter_image_url)) },
                placeholder = { Text(stringResource(R.string.image_url_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Show image preview if URL is not blank
            if (imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Product Image Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            }
                            is AsyncImagePainter.State.Error -> {
                                Text(
                                    text = stringResource(R.string.image_load_error),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            else -> {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Product name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = brand,
                onValueChange = viewModel::onBrandChange,
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = category,
                onValueChange = viewModel::onCategoryChange,
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = origin,
                onValueChange = viewModel::onOriginChange,
                label = { Text("Origin") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = viewModel::onPriceChange,
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = stock,
                onValueChange = viewModel::onStockChange,
                label = { Text("Stock quantity") },
                modifier = Modifier.fillMaxWidth()
            )
            // Old image URL field kept as comment for reference
            // OutlinedTextField(value = imageUrl, onValueChange = viewModel::onImageUrlChange, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}
