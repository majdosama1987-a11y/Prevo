package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PartDao {
    @Query("SELECT * FROM parts ORDER BY createdAt DESC, id DESC")
    fun observeAllParts(): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts WHERE stockQuantity > 0 ORDER BY createdAt DESC, id DESC")
    fun observeAvailableParts(): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts WHERE id = :partId")
    suspend fun getPartById(partId: Long): PartEntity?

    @Query("SELECT COUNT(*) FROM parts")
    suspend fun getPartsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPart(part: PartEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParts(parts: List<PartEntity>)

    @Update
    suspend fun updatePart(part: PartEntity)

    @Query("DELETE FROM parts WHERE id = :partId")
    suspend fun deletePartById(partId: Long)

    @Query("UPDATE parts SET stockQuantity = MAX(0, stockQuantity - :qty) WHERE id = :partId")
    suspend fun decrementStock(partId: Long, qty: Int)
}

@Dao
interface CartDao {
    @Query(
        """
        SELECT c.id AS cartItemId, c.quantity AS quantity, p.*
        FROM cart_items c
        INNER JOIN parts p ON c.partId = p.id
        ORDER BY c.id DESC
        """
    )
    fun observeCartJoined(): Flow<List<CartJoinedRow>>

    @Query("SELECT * FROM cart_items WHERE partId = :partId LIMIT 1")
    suspend fun getByPartId(partId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity): Long

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :cartItemId")
    suspend fun updateQuantity(cartItemId: Long, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteById(cartItemId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC, id DESC")
    fun observeOrdersWithItems(): Flow<List<OrderWithItemsRelation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateStatus(orderId: Long, status: OrderStatus, updatedAt: Long)
}
