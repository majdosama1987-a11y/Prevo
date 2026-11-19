package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.data.model.OrderStatus
import com.example.data.model.PartCategory

@Entity(tableName = "parts")
data class PartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val compatibleModels: String,
    val category: PartCategory,
    val price: Double,
    val stockQuantity: Int,
    val qualityGrade: String,
    val warrantyDays: Int,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = PartEntity::class,
            parentColumns = ["id"],
            childColumns = ["partId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["partId"], unique = true)]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partId: Long,
    val quantity: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val workshopName: String,
    val deliveryAddress: String,
    val notes: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val totalItems: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val partId: Long,
    val partName: String,
    val compatibleModels: String,
    val category: PartCategory,
    val unitPrice: Double,
    val quantity: Int
)

data class CartJoinedRow(
    val cartItemId: Long,
    val quantity: Int,
    @Embedded val part: PartEntity
)

data class OrderWithItemsRelation(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)
