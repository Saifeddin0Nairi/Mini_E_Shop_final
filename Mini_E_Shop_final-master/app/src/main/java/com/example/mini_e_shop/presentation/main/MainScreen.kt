package com.example.mini_e_shop.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mini_e_shop.data.local.entity.UserEntity
import com.example.mini_e_shop.presentation.auth.MainUiState
import com.example.mini_e_shop.presentation.main.components.BottomNavigationBar
import com.example.mini_e_shop.presentation.navigation.MainNavGraph
import com.example.mini_e_shop.presentation.navigation.Screen

@Composable
fun MainScreen(
    mainUiState: MainUiState.Success,
    currentUser: UserEntity?,
    mainNavController: NavHostController,
    onNavigateToOrders: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAddEditProduct: (String?) -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToCheckout: (String) -> Unit
) {
    // This NavController is used only for switching between bottom navigation tabs
    val bottomNavController = rememberNavController()

    // Debug logs for checking admin status consistency
    android.util.Log.d("MainScreen", "isAdmin (from mainUiState): ${mainUiState.isAdmin}")
    android.util.Log.d("MainScreen", "currentUser: ${currentUser?.email}, isAdmin: ${currentUser?.isAdmin}")

    Scaffold(
        bottomBar = {
            // FIX: Ensure the bottom navigation bar stays above the system navigation bar (gesture bar)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface, // Matches theme colors (supports dark mode)
                tonalElevation = 8.dp                       // Material3 elevation for surface layers
            ) {
                Column {

                    BottomNavigationBar(navController = bottomNavController)

                    // IMPORTANT:
                    // This spacer automatically expands to the height of the system navigation bar.
                    // It ensures the bottom bar is lifted above the gesture bar while extending
                    // the background color behind it to avoid visual gaps.
                    Spacer(
                        modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)
                    )
                }
            }
        }
    ) { paddingValues ->

        MainNavGraph(
            modifier = Modifier.padding(paddingValues),
            mainNavController = mainNavController,
            bottomNavController = bottomNavController,
            isAdmin = mainUiState.isAdmin,
            currentUser = currentUser,
            onNavigateToOrders = onNavigateToOrders,
            onNavigateToSettings = {
                mainNavController.navigate(Screen.Settings.route)
            },
            onLogout = onLogout,
            onNavigateToAddEditProduct = onNavigateToAddEditProduct,
            onProductClick = onProductClick,
            onNavigateToSupport = onNavigateToSupport,
            onNavigateToCheckout = onNavigateToCheckout
        )
    }
}
