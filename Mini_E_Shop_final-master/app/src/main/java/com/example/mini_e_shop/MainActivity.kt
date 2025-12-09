package com.example.mini_e_shop

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mini_e_shop.data.preferences.ThemeOption
import com.example.mini_e_shop.data.preferences.UserPreferencesManager
import com.example.mini_e_shop.presentation.add_edit_product.AddEditProductScreen
import com.example.mini_e_shop.presentation.auth.AuthState
import com.example.mini_e_shop.presentation.auth.AuthViewModel
import com.example.mini_e_shop.presentation.auth.MainUiState
import com.example.mini_e_shop.presentation.checkout.CheckoutScreen
import com.example.mini_e_shop.presentation.contact.ContactScreen
import com.example.mini_e_shop.presentation.login.LoginScreen
import com.example.mini_e_shop.presentation.main.MainScreen
import com.example.mini_e_shop.presentation.navigation.Screen
import com.example.mini_e_shop.presentation.orders.OrdersScreen
import com.example.mini_e_shop.presentation.product_detail.ProductDetailScreen
import com.example.mini_e_shop.presentation.register.RegisterScreen
import com.example.mini_e_shop.presentation.settings.SettingsScreen
import com.example.mini_e_shop.presentation.settings.SettingsViewModel
import com.example.mini_e_shop.presentation.support.SupportScreen
import com.example.mini_e_shop.base.MyApplication
import com.example.mini_e_shop.ui.theme.Mini_E_ShopTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        (application as? MyApplication)?.seedProductsToFirestore()

        setContent {
            val language by userPreferencesManager.languageFlow.collectAsState(initial = "vi")
            val settingsViewModel: SettingsViewModel = hiltViewModel(this as ViewModelStoreOwner)

            // Listen for activity recreate events from Settings (e.g. after changing language/theme)
            LaunchedEffect(Unit) {
                settingsViewModel.recreateActivityEvent.collectLatest {
                    recreate()
                }
            }

            // Force recomposition when language changes
            key(language) {
                SetLanguage(languageCode = language)

                val darkTheme = when (
                    userPreferencesManager.themeOptionFlow.collectAsState(
                        initial = ThemeOption.SYSTEM
                    ).value
                ) {
                    ThemeOption.LIGHT -> false
                    ThemeOption.DARK -> true
                    ThemeOption.SYSTEM -> isSystemInDarkTheme()
                }

                Mini_E_ShopTheme(darkTheme = darkTheme) {
                    val authViewModel = hiltViewModel<AuthViewModel>()
                    val authState by authViewModel.authState.collectAsState()
                    // Use a single NavController for the whole app
                    val navController = rememberNavController()
                    val snackbarHostState = remember { SnackbarHostState() }
                    val scope = rememberCoroutineScope()

                    Scaffold(
                        // Reserve space for system bars (status & navigation)
                        contentWindowInsets = WindowInsets.systemBars,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { padding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (authState) {
                                AuthState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                AuthState.Authenticated -> {
                                    val mainUiState by authViewModel.mainUiState.collectAsState()

                                    // Use a different name than authState to avoid confusion
                                    when (val currentState = mainUiState) {
                                        is MainUiState.Loading -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }

                                        is MainUiState.Success -> {
                                            NavHost(
                                                navController = navController,
                                                startDestination = Screen.Main.route
                                            ) {
                                                composable(Screen.Main.route) {
                                                    MainScreen(
                                                        mainUiState = currentState,
                                                        currentUser = currentState.currentUser,
                                                        mainNavController = navController,
                                                        onNavigateToOrders = {
                                                            navController.navigate(Screen.Orders.route)
                                                        },
                                                        onLogout = { authViewModel.onLogout() },
                                                        onNavigateToAddEditProduct = { productId ->
                                                            navController.navigate(
                                                                "${Screen.AddEditProduct.route}?productId=$productId"
                                                            )
                                                        },
                                                        onProductClick = { productId ->
                                                            navController.navigate(
                                                                "${Screen.ProductDetail.route}/$productId"
                                                            )
                                                        },
                                                        onNavigateToSupport = {
                                                            navController.navigate(Screen.Support.route)
                                                        },
                                                        onNavigateToCheckout = { cartItemIds ->
                                                            navController.navigate(
                                                                "${Screen.Checkout.route}/$cartItemIds"
                                                            )
                                                        }
                                                    )
                                                }

                                                composable(Screen.Orders.route) {
                                                    OrdersScreen(
                                                        viewModel = hiltViewModel(),
                                                        onBack = { navController.popBackStack() }
                                                    )
                                                }

                                                composable(
                                                    route = "${Screen.AddEditProduct.route}?productId={productId}",
                                                    arguments = listOf(
                                                        navArgument("productId") {
                                                            type = NavType.StringType
                                                            defaultValue = ""
                                                            nullable = true
                                                        }
                                                    )
                                                ) {
                                                    // Check admin permission before allowing access
                                                    if (currentState.isAdmin) {
                                                        AddEditProductScreen(
                                                            viewModel = hiltViewModel(),
                                                            onSave = { navController.popBackStack() },
                                                            onBack = { navController.popBackStack() }
                                                        )
                                                    } else {
                                                        // If not admin, navigate back immediately
                                                        LaunchedEffect(Unit) {
                                                            navController.popBackStack()
                                                        }
                                                    }
                                                }

                                                composable(
                                                    route = "${Screen.ProductDetail.route}/{productId}",
                                                    arguments = listOf(
                                                        navArgument("productId") {
                                                            type = NavType.IntType
                                                        }
                                                    )
                                                ) {
                                                    ProductDetailScreen(
                                                        onBack = { navController.popBackStack() }
                                                    )
                                                }

                                                composable(Screen.Support.route) {
                                                    SupportScreen(
                                                        onBack = { navController.popBackStack() },
                                                        onNavigateToContact = {
                                                            navController.navigate(Screen.Contact.route)
                                                        }
                                                    )
                                                }

                                                composable(Screen.Contact.route) {
                                                    ContactScreen(
                                                        onBack = { navController.popBackStack() }
                                                    )
                                                }

                                                // Checkout composable: now fully configured and valid
                                                composable(
                                                    route = "${Screen.Checkout.route}/{cartItemIds}",
                                                    arguments = listOf(
                                                        navArgument("cartItemIds") {
                                                            type = NavType.StringType
                                                        }
                                                    )
                                                ) {
                                                    CheckoutScreen(
                                                        // We don't need to pass CartViewModel here anymore
                                                        onNavigateBack = {
                                                            navController.popBackStack()
                                                        },
                                                        onShowSnackbar = { message ->
                                                            // scope and snackbarHostState are valid here
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message
                                                                )
                                                            }
                                                        }
                                                    )
                                                }

                                                composable(Screen.Settings.route) {
                                                    SettingsScreen(
                                                        viewModel = settingsViewModel,
                                                        currentUser = currentState.currentUser,
                                                        onBack = { navController.popBackStack() },
                                                        onNavigateToSupport = {
                                                            navController.navigate(Screen.Support.route)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                AuthState.Unauthenticated -> {
                                    NavHost(
                                        navController = navController,
                                        startDestination = Screen.Login.route
                                    ) {
                                        composable(Screen.Login.route) {
                                            LoginScreen(
                                                viewModel = hiltViewModel(),
                                                onLoginSuccess = {
                                                    // authViewModel.onLoginSuccess(it)
                                                },
                                                onNavigateToRegister = {
                                                    navController.navigate(Screen.Register.route)
                                                }
                                            )
                                        }

                                        composable(Screen.Register.route) {
                                            RegisterScreen(
                                                viewModel = hiltViewModel(),
                                                onRegisterSuccess = {
                                                    navController.navigate(Screen.Login.route) {
                                                        popUpTo(Screen.Register.route) {
                                                            inclusive = true
                                                        }
                                                    }
                                                },
                                                onBackToLogin = {
                                                    navController.popBackStack()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetLanguage(languageCode: String) {
    val context = LocalContext.current
    val rememberedContext = remember { context }

    LaunchedEffect(languageCode) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = rememberedContext.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        rememberedContext.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
