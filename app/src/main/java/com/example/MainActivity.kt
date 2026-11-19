package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.local.StorePreferences
import com.example.data.model.AppMode
import com.example.data.repository.PartsStoreRepository
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.StorefrontScreen
import com.example.ui.theme.MobilePartsTheme
import com.example.ui.viewmodel.StoreViewModel

const val ROUTE_STORE = "storefront"
const val ROUTE_CART = "shopping_cart"
const val ROUTE_ORDERS = "customer_orders"
const val ROUTE_ADMIN = "admin_dashboard"

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobilePartsTheme {
                MobilePartsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobilePartsApp() {
    val context = LocalContext.current.applicationContext
    val repository = remember {
        PartsStoreRepository(
            db = AppDatabase.getInstance(context),
            preferences = StorePreferences(context)
        )
    }
    val viewModel: StoreViewModel = viewModel(
        factory = StoreViewModel.Factory(repository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_STORE

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val destinations = remember {
        listOf(
            NavDestination(
                route = ROUTE_STORE,
                label = "المتجر",
                selectedIcon = Icons.Rounded.Storefront,
                unselectedIcon = Icons.Outlined.Storefront,
                testTag = "nav_tab_store"
            ),
            NavDestination(
                route = ROUTE_CART,
                label = "السلة",
                selectedIcon = Icons.Rounded.ShoppingCart,
                unselectedIcon = Icons.Outlined.ShoppingCart,
                testTag = "nav_tab_cart"
            ),
            NavDestination(
                route = ROUTE_ORDERS,
                label = "طلباتي",
                selectedIcon = Icons.Rounded.ReceiptLong,
                unselectedIcon = Icons.Outlined.ReceiptLong,
                testTag = "nav_tab_orders"
            ),
            NavDestination(
                route = ROUTE_ADMIN,
                label = "الإدارة",
                selectedIcon = Icons.Rounded.AdminPanelSettings,
                unselectedIcon = Icons.Outlined.AdminPanelSettings,
                testTag = "nav_tab_admin"
            )
        )
    }

    fun navigateToRoute(route: String) {
        viewModel.setAppMode(if (route == ROUTE_ADMIN) AppMode.ADMIN else AppMode.STORE)
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BackHandler(enabled = currentRoute != ROUTE_STORE) {
        navigateToRoute(ROUTE_STORE)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (currentRoute == ROUTE_ADMIN) {
                                    "الإدارة الرئيسية"
                                } else {
                                    uiState.settings.storeName
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentRoute == ROUTE_ADMIN) {
                                    "إضافة القطع • التسعير • إدارة الطلبات"
                                } else {
                                    "شاشات • بطاريات • فلاتات • قطع صيانة"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        Surface(
                            color = if (currentRoute == ROUTE_ADMIN) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    if (currentRoute == ROUTE_ADMIN) {
                                        navigateToRoute(ROUTE_STORE)
                                    } else {
                                        navigateToRoute(ROUTE_ADMIN)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("top_mode_switch_button")
                            ) {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_ADMIN) {
                                        Icons.Rounded.Storefront
                                    } else {
                                        Icons.Rounded.AdminPanelSettings
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (currentRoute == ROUTE_ADMIN) " واجهة المتجر" else " الإدارة",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToRoute(dest.route) },
                                modifier = Modifier.testTag(dest.testTag),
                                icon = {
                                    NavIconWithBadge(
                                        dest = dest,
                                        selected = selected,
                                        cartCount = uiState.cartTotalCount,
                                        pendingOrdersCount = uiState.pendingOrdersCount
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            destinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(dest.route) },
                                    modifier = Modifier.testTag(dest.testTag),
                                    icon = {
                                        NavIconWithBadge(
                                            dest = dest,
                                            selected = selected,
                                            cartCount = uiState.cartTotalCount,
                                            pendingOrdersCount = uiState.pendingOrdersCount
                                        )
                                    },
                                    label = { Text(dest.label) }
                                )
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_STORE,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_STORE) {
                        StorefrontScreen(
                            uiState = uiState,
                            onSelectCategory = viewModel::selectCategory,
                            onSearchChange = viewModel::updateSearchQuery,
                            onAddToCart = viewModel::addToCart
                        )
                    }

                    composable(ROUTE_CART) {
                        CartScreen(
                            uiState = uiState,
                            onUpdateQuantity = viewModel::updateCartQuantity,
                            onRemoveItem = viewModel::removeCartItem,
                            onClearCart = viewModel::clearCart,
                            onPlaceOrder = viewModel::placeOrder,
                            onBrowseStore = { navigateToRoute(ROUTE_STORE) },
                            onViewOrders = { navigateToRoute(ROUTE_ORDERS) }
                        )
                    }

                    composable(ROUTE_ORDERS) {
                        OrdersScreen(
                            uiState = uiState,
                            onBrowseStore = { navigateToRoute(ROUTE_STORE) }
                        )
                    }

                    composable(ROUTE_ADMIN) {
                        AdminDashboardScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearchQuery,
                            onSelectCategory = viewModel::selectCategory,
                            onSavePart = viewModel::savePart,
                            onAdjustStock = viewModel::adjustPartStock,
                            onDeletePart = viewModel::deletePart,
                            onOrderFilterChange = viewModel::setAdminOrderFilter,
                            onUpdateOrderStatus = viewModel::updateOrderStatus,
                            onUpdateCurrency = viewModel::updateCurrency,
                            onUpdateStoreName = viewModel::updateStoreName
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIconWithBadge(
    dest: NavDestination,
    selected: Boolean,
    cartCount: Int,
    pendingOrdersCount: Int
) {
    val badgeValue = when (dest.route) {
        ROUTE_CART -> cartCount
        ROUTE_ADMIN -> pendingOrdersCount
        else -> 0
    }
    val iconVector = if (selected) dest.selectedIcon else dest.unselectedIcon

    if (badgeValue > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(text = if (badgeValue > 99) "99+" else "$badgeValue")
                }
            }
        ) {
            Icon(imageVector = iconVector, contentDescription = dest.label)
        }
    } else {
        Icon(imageVector = iconVector, contentDescription = dest.label)
    }
}
