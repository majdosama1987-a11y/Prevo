package com.example.data.model

import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.data.local.PartEntity

enum class PartCategory(val labelAr: String, val badgeAr: String) {
    SCREENS("شاشات", "شاشة"),
    BATTERIES("بطاريات", "بطارية"),
    FLEX_CABLES("فلاتات", "فلاتة"),
    OTHER("قطع أخرى", "قطعة غيار")
}

enum class OrderStatus(val labelAr: String, val stepIndex: Int) {
    PENDING("قيد المراجعة", 0),
    PROCESSING("جاري التجهيز", 1),
    SHIPPED("تم الشحن", 2),
    DELIVERED("تم التسليم", 3),
    CANCELLED("ملغي", -1)
}

enum class AppMode(val labelAr: String) {
    STORE("المتجر"),
    ADMIN("الإدارة الرئيسية")
}

data class CartItemDetail(
    val cartItemId: Long,
    val part: PartEntity,
    val quantity: Int
) {
    val lineTotal: Double get() = part.price * quantity
}

data class OrderWithItems(
    val order: OrderEntity,
    val items: List<OrderItemEntity>
)
