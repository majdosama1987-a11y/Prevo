package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderStatus
import com.example.data.model.OrderWithItems
import com.example.ui.components.EmptyIllustrationBox
import com.example.ui.components.StatusPill
import com.example.ui.components.formatArabicDate
import com.example.ui.components.formatPrice
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HardwareAmber
import com.example.ui.theme.HardwareAmberContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.TechBlue
import com.example.ui.theme.TechBlueContainer
import com.example.ui.viewmodel.StoreUiState

@Composable
fun AdminOrdersContent(
    uiState: StoreUiState,
    onOrderFilterChange: (OrderStatus?) -> Unit,
    onUpdateOrderStatus: (Long, OrderStatus) -> Unit
) {
    val currency = uiState.settings.currencySymbol
    val orders = uiState.filteredOrdersForAdmin

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiMetricCard(
                    label = "كل الطلبات",
                    value = "${uiState.orders.size}",
                    accent = TechBlue,
                    container = TechBlueContainer,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    label = "بانتظار المراجعة",
                    value = "${uiState.pendingOrdersCount}",
                    accent = HardwareAmber,
                    container = HardwareAmberContainer,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    label = "المبيعات",
                    value = formatPrice(uiState.totalRevenue, currency),
                    accent = EmeraldSuccess,
                    container = EmeraldContainer,
                    modifier = Modifier.weight(1.3f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.adminOrderFilter == null,
                    onClick = { onOrderFilterChange(null) },
                    label = { Text("الكل (${uiState.orders.size})") }
                )
                OrderStatus.entries.forEach { status ->
                    val count = uiState.orders.count { it.order.status == status }
                    FilterChip(
                        selected = uiState.adminOrderFilter == status,
                        onClick = {
                            onOrderFilterChange(if (uiState.adminOrderFilter == status) null else status)
                        },
                        label = { Text("${status.labelAr} ($count)") }
                    )
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                EmptyIllustrationBox(
                    icon = Icons.Rounded.ReceiptLong,
                    title = "لا توجد طلبات في هذا القسم",
                    subtitle = "ستظهر الطلبات الواردة من سلة المشتريات هنا لإدارتها وتحديث حالتها."
                )
            }
        } else {
            items(orders, key = { it.order.id }) { orderWithItems ->
                AdminOrderManagementCard(
                    orderWithItems = orderWithItems,
                    currency = currency,
                    onUpdateStatus = { newStatus ->
                        onUpdateOrderStatus(orderWithItems.order.id, newStatus)
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminOrderManagementCard(
    orderWithItems: OrderWithItems,
    currency: String,
    onUpdateStatus: (OrderStatus) -> Unit
) {
    val order = orderWithItems.order

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "طلب #${order.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatArabicDate(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(status = order.status)
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "العميل: ${order.customerName} • ${order.customerPhone}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (order.workshopName.isNotBlank()) {
                        Text(
                            text = "الورشة: ${order.workshopName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "العنوان: ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (order.notes.isNotBlank()) {
                        Text(
                            text = "ملاحظة: ${order.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = HardwareAmber
                        )
                    }
                }
            }

            orderWithItems.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity} × ${item.partName}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatPrice(item.unitPrice * item.quantity, currency),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإجمالي (${order.totalItems} قطعة)",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = formatPrice(order.totalAmount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "تحديث حالة الطلب:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OrderStatus.entries.forEach { targetStatus ->
                    val isCurrent = order.status == targetStatus
                    if (isCurrent) {
                        Button(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(targetStatus.labelAr, style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onUpdateStatus(targetStatus) },
                            modifier = Modifier.testTag("order_${order.id}_status_${targetStatus.name.lowercase()}"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = targetStatus.labelAr,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (targetStatus == OrderStatus.CANCELLED) RoseError else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsContent(
    storeName: String,
    currentCurrency: String,
    onUpdateStoreName: (String) -> Unit,
    onUpdateCurrency: (String) -> Unit
) {
    var storeNameDraft by remember(storeName) { mutableStateOf(storeName) }
    val currencies = listOf("د.أ", "ر.س", "ج.م", "$", "د.ع", "د.إ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "هوية المتجر الرئيسي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = storeNameDraft,
                    onValueChange = { storeNameDraft = it },
                    label = { Text("اسم المتجر") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_store_name_input")
                )

                Button(
                    onClick = { onUpdateStoreName(storeNameDraft) },
                    enabled = storeNameDraft.isNotBlank() && storeNameDraft.trim() != storeName,
                    modifier = Modifier.testTag("settings_save_store_name_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ اسم المتجر")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "عملة التسعير",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اختر العملة التي تظهر بجانب أسعار قطع الصيانة والفواتير:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { symbol ->
                        FilterChip(
                            selected = currentCurrency == symbol,
                            onClick = { onUpdateCurrency(symbol) },
                            modifier = Modifier.testTag("currency_chip_$symbol"),
                            label = { Text(symbol) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    label: String,
    value: String,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = container.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
