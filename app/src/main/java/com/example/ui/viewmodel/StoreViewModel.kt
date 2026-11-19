package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PartEntity
import com.example.data.local.StoreSettings
import com.example.data.model.AppMode
import com.example.data.model.CartItemDetail
import com.example.data.model.OrderStatus
import com.example.data.model.OrderWithItems
import com.example.data.model.PartCategory
import com.example.data.repository.PartsStoreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StoreUiState(
    val appMode: AppMode = AppMode.STORE,
    val allParts: List<PartEntity> = emptyList(),
    val availableParts: List<PartEntity> = emptyList(),
    val filteredParts: List<PartEntity> = emptyList(),
    val adminFilteredParts: List<PartEntity> = emptyList(),
    val selectedCategory: PartCategory? = null,
    val searchQuery: String = "",
    val cartItems: List<CartItemDetail> = emptyList(),
    val orders: List<OrderWithItems> = emptyList(),
    val adminOrderFilter: OrderStatus? = null,
    val settings: StoreSettings = StoreSettings(),
    val isLoading: Boolean = true
) {
    val cartTotalAmount: Double get() = cartItems.sumOf { it.lineTotal }
    val cartTotalCount: Int get() = cartItems.sumOf { it.quantity }
    val cartQtyByPartId: Map<Long, Int> get() = cartItems.associate { it.part.id to it.quantity }

    val filteredOrdersForAdmin: List<OrderWithItems>
        get() = if (adminOrderFilter == null) orders else orders.filter { it.order.status == adminOrderFilter }

    val totalRevenue: Double
        get() = orders.filter { it.order.status != OrderStatus.CANCELLED }.sumOf { it.order.totalAmount }

    val pendingOrdersCount: Int
        get() = orders.count { it.order.status == OrderStatus.PENDING }

    val lowStockCount: Int
        get() = allParts.count { it.stockQuantity in 1..5 }

    val outOfStockCount: Int
        get() = allParts.count { it.stockQuantity == 0 }
}

private data class FilterState(
    val appMode: AppMode,
    val selectedCategory: PartCategory?,
    val searchQuery: String,
    val adminOrderFilter: OrderStatus?
)

class StoreViewModel(
    private val repository: PartsStoreRepository
) : ViewModel() {

    private val _appMode = MutableStateFlow(AppMode.STORE)
    private val _selectedCategory = MutableStateFlow<PartCategory?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _adminOrderFilter = MutableStateFlow<OrderStatus?>(null)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val filterFlow = combine(
        _appMode,
        _selectedCategory,
        _searchQuery,
        _adminOrderFilter
    ) { mode, category, query, orderFilter ->
        FilterState(mode, category, query, orderFilter)
    }

    private val catalogFlow = combine(
        repository.observeParts(),
        repository.observeAvailableParts()
    ) { all, available -> all to available }

    val uiState: StateFlow<StoreUiState> = combine(
        catalogFlow,
        repository.observeCartItems(),
        repository.observeOrders(),
        repository.storeSettings,
        filterFlow
    ) { (allParts, availableParts), cartItems, orders, settings, filters ->
        val query = filters.searchQuery.trim().lowercase()
        fun matchesFilter(part: PartEntity): Boolean {
            val matchesCategory = filters.selectedCategory == null || part.category == filters.selectedCategory
            val matchesQuery = query.isEmpty() ||
                part.name.lowercase().contains(query) ||
                part.compatibleModels.lowercase().contains(query) ||
                part.qualityGrade.lowercase().contains(query) ||
                part.category.labelAr.contains(query)
            return matchesCategory && matchesQuery
        }

        StoreUiState(
            appMode = filters.appMode,
            allParts = allParts,
            availableParts = availableParts,
            filteredParts = availableParts.filter(::matchesFilter),
            adminFilteredParts = allParts.filter(::matchesFilter),
            selectedCategory = filters.selectedCategory,
            searchQuery = filters.searchQuery,
            cartItems = cartItems,
            orders = orders,
            adminOrderFilter = filters.adminOrderFilter,
            settings = settings,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = StoreUiState()
    )

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun selectCategory(category: PartCategory?) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAdminOrderFilter(status: OrderStatus?) {
        _adminOrderFilter.value = status
    }

    fun addToCart(part: PartEntity, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(part, quantity)
                .onSuccess { _messages.tryEmit("تمت إضافة \"${part.name}\" إلى السلة") }
                .onFailure { _messages.tryEmit(it.message ?: "تعذر الإضافة للسلة") }
        }
    }

    fun updateCartQuantity(cartItemId: Long, partId: Long, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, partId, newQuantity)
                .onFailure { _messages.tryEmit(it.message ?: "تعذر تحديث الكمية") }
        }
    }

    fun removeCartItem(cartItemId: Long) {
        viewModelScope.launch {
            repository.removeCartItem(cartItemId)
            _messages.tryEmit("تم حذف القطعة من السلة")
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
            _messages.tryEmit("تم إفراغ السلة")
        }
    }

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        workshopName: String,
        deliveryAddress: String,
        notes: String,
        onSuccess: (orderNumber: String) -> Unit
    ) {
        viewModelScope.launch {
            repository.placeOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                workshopName = workshopName,
                deliveryAddress = deliveryAddress,
                notes = notes
            ).onSuccess { orderNumber ->
                onSuccess(orderNumber)
            }.onFailure { err ->
                _messages.tryEmit(err.message ?: "حدث خطأ أثناء إرسال الطلب")
            }
        }
    }

    fun savePart(part: PartEntity, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.savePart(part)
                .onSuccess {
                    _messages.tryEmit(if (part.id == 0L) "تمت إضافة القطعة للمتجر" else "تم تحديث بيانات القطعة")
                    onSaved()
                }
                .onFailure {
                    _messages.tryEmit(it.message ?: "تعذر حفظ القطعة")
                }
        }
    }

    fun deletePart(part: PartEntity) {
        viewModelScope.launch {
            repository.deletePart(part.id)
                .onSuccess { _messages.tryEmit("تم حذف \"${part.name}\"") }
                .onFailure { _messages.tryEmit(it.message ?: "تعذر حذف القطعة") }
        }
    }

    fun adjustPartStock(part: PartEntity, delta: Int) {
        val nextStock = (part.stockQuantity + delta).coerceAtLeast(0)
        if (nextStock == part.stockQuantity) return
        viewModelScope.launch {
            repository.savePart(part.copy(stockQuantity = nextStock))
                .onFailure { _messages.tryEmit(it.message ?: "تعذر تحديث المخزون") }
        }
    }

    fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
                .onSuccess { _messages.tryEmit("تم تحديث حالة الطلب إلى: ${status.labelAr}") }
                .onFailure { _messages.tryEmit(it.message ?: "تعذر تحديث حالة الطلب") }
        }
    }

    fun updateCurrency(symbol: String) {
        viewModelScope.launch {
            repository.updateCurrency(symbol)
            _messages.tryEmit("تم تغيير العملة إلى ($symbol)")
        }
    }

    fun updateStoreName(name: String) {
        viewModelScope.launch {
            repository.updateStoreName(name)
            _messages.tryEmit("تم حفظ اسم المتجر")
        }
    }

    class Factory(private val repository: PartsStoreRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StoreViewModel(repository) as T
        }
    }
}
