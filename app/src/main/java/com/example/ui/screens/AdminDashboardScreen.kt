package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.PartEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PartCategory
import com.example.ui.components.CategoryArtworkTile
import com.example.ui.components.EmptyIllustrationBox
import com.example.ui.components.accentColor
import com.example.ui.components.containerColor
import com.example.ui.components.formatPrice
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HardwareAmber
import com.example.ui.theme.HardwareAmberContainer
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.TechBlue
import com.example.ui.theme.TechBlueContainer
import com.example.ui.viewmodel.StoreUiState

enum class AdminTab(val labelAr: String) {
    CATALOG("القطع والأسعار"),
    ORDERS("إدارة الطلبات"),
    SETTINGS("الإعدادات")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    uiState: StoreUiState,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (PartCategory?) -> Unit,
    onSavePart: (PartEntity, () -> Unit) -> Unit,
    onAdjustStock: (PartEntity, Int) -> Unit,
    onDeletePart: (PartEntity) -> Unit,
    onOrderFilterChange: (OrderStatus?) -> Unit,
    onUpdateOrderStatus: (Long, OrderStatus) -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onUpdateStoreName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AdminTab.CATALOG) }
    var editingPart by remember { mutableStateOf<PartEntity?>(null) }
    var showAddPartDialog by remember { mutableStateOf(false) }
    var partPendingDelete by remember { mutableStateOf<PartEntity?>(null) }

    val currency = uiState.settings.currencySymbol

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                AdminTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = AdminTab.entries.size
                        ),
                        modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}"),
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    AdminTab.CATALOG -> Icons.Rounded.Inventory2
                                    AdminTab.ORDERS -> Icons.Rounded.ReceiptLong
                                    AdminTab.SETTINGS -> Icons.Rounded.Settings
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            val badge = if (tab == AdminTab.ORDERS && uiState.pendingOrdersCount > 0) {
                                " (${uiState.pendingOrdersCount})"
                            } else ""
                            Text(
                                text = "${tab.labelAr}$badge",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            when (activeTab) {
                AdminTab.CATALOG -> AdminCatalogContent(
                    uiState = uiState,
                    onSearchChange = onSearchChange,
                    onSelectCategory = onSelectCategory,
                    onAddPart = { showAddPartDialog = true },
                    onEditPart = { editingPart = it },
                    onAdjustStock = onAdjustStock,
                    onRequestDelete = { partPendingDelete = it }
                )

                AdminTab.ORDERS -> AdminOrdersContent(
                    uiState = uiState,
                    onOrderFilterChange = onOrderFilterChange,
                    onUpdateOrderStatus = onUpdateOrderStatus
                )

                AdminTab.SETTINGS -> AdminSettingsContent(
                    storeName = uiState.settings.storeName,
                    currentCurrency = currency,
                    onUpdateStoreName = onUpdateStoreName,
                    onUpdateCurrency = onUpdateCurrency
                )
            }
        }

        if (activeTab == AdminTab.CATALOG) {
            FloatingActionButton(
                onClick = { showAddPartDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .testTag("admin_add_part_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "إضافة قطعة")
                    Text(
                        text = "إضافة قطعة",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showAddPartDialog || editingPart != null) {
        AdminPartEditorDialog(
            initialPart = editingPart,
            currency = currency,
            onDismiss = {
                showAddPartDialog = false
                editingPart = null
            },
            onSave = { part ->
                onSavePart(part) {
                    showAddPartDialog = false
                    editingPart = null
                }
            }
        )
    }

    partPendingDelete?.let { part ->
        AlertDialog(
            onDismissRequest = { partPendingDelete = null },
            title = { Text("تأكيد حذف القطعة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف \"${part.name}\" نهائياً من قاعدة البيانات؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePart(part)
                        partPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                    modifier = Modifier.testTag("confirm_delete_part_button")
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { partPendingDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun AdminCatalogContent(
    uiState: StoreUiState,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (PartCategory?) -> Unit,
    onAddPart: () -> Unit,
    onEditPart: (PartEntity) -> Unit,
    onAdjustStock: (PartEntity, Int) -> Unit,
    onRequestDelete: (PartEntity) -> Unit
) {
    val currency = uiState.settings.currencySymbol

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiMetricCard(
                    label = "إجمالي الأصناف",
                    value = "${uiState.allParts.size}",
                    accent = TechBlue,
                    container = TechBlueContainer,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    label = "مخزون منخفض",
                    value = "${uiState.lowStockCount}",
                    accent = HardwareAmber,
                    container = HardwareAmberContainer,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    label = "نفذت الكمية",
                    value = "${uiState.outOfStockCount}",
                    accent = RoseError,
                    container = RoseContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("ابحث لتعديل سعر أو مخزون...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "بحث") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_search_input")
                )
                Button(
                    onClick = onAddPart,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("admin_add_part_top_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("جديد", fontWeight = FontWeight.Bold)
                }
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
                    selected = uiState.selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text("الكل (${uiState.allParts.size})") }
                )
                PartCategory.entries.forEach { category ->
                    val count = uiState.allParts.count { it.category == category }
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = {
                            onSelectCategory(if (uiState.selectedCategory == category) null else category)
                        },
                        label = { Text("${category.labelAr} ($count)") }
                    )
                }
            }
        }

        if (uiState.adminFilteredParts.isEmpty()) {
            item {
                EmptyIllustrationBox(
                    icon = Icons.Rounded.Inventory2,
                    title = "لا توجد قطع",
                    subtitle = "اضغط على زر (+ إضافة قطعة) لإضافة قطع صيانة جديدة وتسعيرها.",
                    action = {
                        Button(
                            onClick = onAddPart,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة قطعة جديدة")
                        }
                    }
                )
            }
        } else {
            items(uiState.adminFilteredParts, key = { it.id }) { part ->
                AdminPartRowCard(
                    part = part,
                    currency = currency,
                    onEdit = { onEditPart(part) },
                    onAdjustStock = { delta -> onAdjustStock(part, delta) },
                    onDelete = { onRequestDelete(part) }
                )
            }
        }
    }
}

@Composable
private fun AdminPartRowCard(
    part: PartEntity,
    currency: String,
    onEdit: () -> Unit,
    onAdjustStock: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_part_card_${part.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryArtworkTile(
                    category = part.category,
                    modifier = Modifier.size(60.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = part.category.containerColor(),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = part.category.labelAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = part.category.accentColor(),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = if (part.stockQuantity == 0) "نفذت الكمية" else "المخزون: ${part.stockQuantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (part.stockQuantity == 0) RoseError else EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = part.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${part.compatibleModels} • ${formatPrice(part.price, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick inline stock adjustment
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "المخزون:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { onAdjustStock(-1) },
                        enabled = part.stockQuantity > 0,
                        modifier = Modifier.testTag("admin_stock_minus_${part.id}")
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "إنقاص المخزون")
                    }
                    Text(
                        text = "${part.stockQuantity}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onAdjustStock(1) },
                        modifier = Modifier.testTag("admin_stock_plus_${part.id}")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "زيادة المخزون")
                    }
                }

                // Edit & Delete buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("admin_edit_part_${part.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "تعديل",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تعديل", style = MaterialTheme.typography.labelMedium)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("admin_delete_part_${part.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "حذف القطعة",
                            tint = RoseError
                        )
                    }
                }
            }
        }
    }
}
