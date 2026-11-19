package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.data.local.PartEntity
import com.example.data.local.StorePreferences
import com.example.data.local.StoreSettings
import com.example.data.model.CartItemDetail
import com.example.data.model.OrderStatus
import com.example.data.model.OrderWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PartsStoreRepository(
    private val db: AppDatabase,
    private val preferences: StorePreferences
) {
    private val partDao = db.partDao()
    private val cartDao = db.cartDao()
    private val orderDao = db.orderDao()

    val storeSettings: Flow<StoreSettings> = preferences.settingsFlow

    fun observeParts(): Flow<List<PartEntity>> = partDao.observeAllParts()

    fun observeAvailableParts(): Flow<List<PartEntity>> = partDao.observeAvailableParts()

    fun observeCartItems(): Flow<List<CartItemDetail>> =
        cartDao.observeCartJoined().map { rows ->
            rows.map { row ->
                CartItemDetail(
                    cartItemId = row.cartItemId,
                    part = row.part,
                    quantity = row.quantity
                )
            }
        }

    fun observeOrders(): Flow<List<OrderWithItems>> =
        orderDao.observeOrdersWithItems().map { relations ->
            relations.map { rel ->
                OrderWithItems(
                    order = rel.order,
                    items = rel.items
                )
            }
        }

    suspend fun addToCart(part: PartEntity, delta: Int = 1): Result<Unit> {
        return try {
            if (part.stockQuantity <= 0) {
                return Result.failure(IllegalStateException("هذه القطعة غير متوفرة حالياً في المخزون"))
            }
            val existing = cartDao.getByPartId(part.id)
            if (existing == null) {
                cartDao.insert(CartItemEntity(partId = part.id, quantity = delta.coerceAtMost(part.stockQuantity)))
            } else {
                val nextQty = existing.quantity + delta
                if (nextQty > part.stockQuantity) {
                    return Result.failure(IllegalStateException("الكمية المطلوبة تتجاوز المتاح بالمخزون (${part.stockQuantity})"))
                }
                cartDao.updateQuantity(existing.id, nextQty)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCartQuantity(cartItemId: Long, partId: Long, newQuantity: Int): Result<Unit> {
        return try {
            if (newQuantity <= 0) {
                cartDao.deleteById(cartItemId)
                return Result.success(Unit)
            }
            val part = partDao.getPartById(partId)
                ?: return Result.failure(IllegalStateException("القطعة غير موجودة"))
            if (newQuantity > part.stockQuantity) {
                return Result.failure(IllegalStateException("أقصى كمية متاحة هي ${part.stockQuantity}"))
            }
            cartDao.updateQuantity(cartItemId, newQuantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeCartItem(cartItemId: Long) {
        cartDao.deleteById(cartItemId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        workshopName: String,
        deliveryAddress: String,
        notes: String
    ): Result<String> {
        return try {
            val cartItems = observeCartItems().first()
            if (cartItems.isEmpty()) {
                return Result.failure(IllegalStateException("سلة المشتريات فارغة"))
            }
            var generatedNumber = ""
            db.withTransaction {
                for (item in cartItems) {
                    val freshPart = partDao.getPartById(item.part.id)
                        ?: throw IllegalStateException("القطعة ${item.part.name} لم تعد متوفرة")
                    if (item.quantity > freshPart.stockQuantity) {
                        throw IllegalStateException("الكمية المتاحة من ${freshPart.name} هي ${freshPart.stockQuantity} فقط")
                    }
                }

                val totalAmount = cartItems.sumOf { it.lineTotal }
                val totalItems = cartItems.sumOf { it.quantity }
                val now = System.currentTimeMillis()
                generatedNumber = "ORD-${(now % 9000 + 1000)}"

                val orderId = orderDao.insertOrder(
                    OrderEntity(
                        orderNumber = generatedNumber,
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        workshopName = workshopName.trim(),
                        deliveryAddress = deliveryAddress.trim(),
                        notes = notes.trim(),
                        status = OrderStatus.PENDING,
                        totalAmount = totalAmount,
                        totalItems = totalItems,
                        createdAt = now,
                        updatedAt = now
                    )
                )

                val orderItems = cartItems.map { item ->
                    OrderItemEntity(
                        orderId = orderId,
                        partId = item.part.id,
                        partName = item.part.name,
                        compatibleModels = item.part.compatibleModels,
                        category = item.part.category,
                        unitPrice = item.part.price,
                        quantity = item.quantity
                    )
                }
                orderDao.insertOrderItems(orderItems)

                for (item in cartItems) {
                    partDao.decrementStock(item.part.id, item.quantity)
                }
                cartDao.clearCart()
            }
            Result.success(generatedNumber)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin Catalog & Order Operations
    suspend fun savePart(part: PartEntity): Result<Unit> {
        return try {
            if (part.id == 0L) {
                partDao.insertPart(part)
            } else {
                partDao.updatePart(part)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePart(partId: Long): Result<Unit> {
        return try {
            partDao.deletePartById(partId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): Result<Unit> {
        return try {
            orderDao.updateStatus(orderId, newStatus, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCurrency(symbol: String) {
        preferences.updateCurrency(symbol)
    }

    suspend fun updateStoreName(name: String) {
        if (name.isNotBlank()) {
            preferences.updateStoreName(name)
        }
    }
}
