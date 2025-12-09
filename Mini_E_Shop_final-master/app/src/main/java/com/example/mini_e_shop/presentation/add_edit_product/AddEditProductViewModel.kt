package com.example.mini_e_shop.presentation.add_edit_product

import android.net.Uri
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.UUID

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- State for product fields ---
    // These StateFlows are bound to the TextFields in the UI
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _brand = MutableStateFlow("")
    val brand = _brand.asStateFlow()

    private val _category = MutableStateFlow("")
    val category = _category.asStateFlow()

    private val _origin = MutableStateFlow("")
    val origin = _origin.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _stock = MutableStateFlow("")
    val stock = _stock.asStateFlow()

    // Image URL input (replaces selecting image from device)
    private val _imageUrl = MutableStateFlow("")
    val imageUrl = _imageUrl.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _saveEvent = Channel<Unit>()
    val saveEvent = _saveEvent.receiveAsFlow()

    // Get productId directly from SavedStateHandle.
    // If it's null or blank, we treat it as a new product.
    private var currentProductId: String? =
        savedStateHandle.get<String>("productId")?.takeIf { it.isNotBlank() }

    init {
        // Only load product data if productId is not null/blank (editing existing product)
        if (currentProductId != null && currentProductId!!.isNotBlank()) {
            viewModelScope.launch {
                // Fetch product from repository
                productRepository.getProductById(currentProductId!!)?.let { product ->
                    // Fill StateFlows with product data
                    _name.value = product.name
                    _brand.value = product.brand
                    _category.value = product.category
                    _origin.value = product.origin
                    _price.value = product.price.toString()
                    _stock.value = product.stock.toString()
                    _imageUrl.value = product.imageUrl
                    _description.value = product.description
                }
            }
        }
    }

    // --- Event handlers for UI changes ---
    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onBrandChange(newBrand: String) {
        _brand.value = newBrand
    }

    fun onCategoryChange(newCategory: String) {
        _category.value = newCategory
    }

    fun onOriginChange(newOrigin: String) {
        _origin.value = newOrigin
    }

    fun onPriceChange(newPrice: String) {
        _price.value = newPrice
    }

    fun onStockChange(newStock: String) {
        _stock.value = newStock
    }

    fun onImageUrlChange(newUrl: String) {
        _imageUrl.value = newUrl
    }

    fun onDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    fun saveProduct() {
        viewModelScope.launch {
            // Determine product ID:
            // if editing, keep existing ID; if creating, generate a new one.
            val productId = currentProductId ?: UUID.randomUUID().toString()

            val productToSave = Product(
                id = productId,
                name = name.value.trim(),
                brand = brand.value.trim(),
                category = category.value.trim(),
                origin = origin.value.trim(),
                price = price.value.toDoubleOrNull() ?: 0.0,
                stock = stock.value.toIntOrNull() ?: 0,
                imageUrl = imageUrl.value.trim(),
                description = description.value.trim()
            )

            // Validate required fields
            if (productToSave.name.isBlank() || productToSave.category.isBlank()) {
                // TODO: send an event to show error to the user (e.g. via a separate StateFlow)
                // Example: _errorEvent.send("Product name and category must not be empty")
                return@launch
            }

            productRepository.upsertProduct(productToSave)
            _saveEvent.send(Unit)
        }
    }
}
