package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.PartCategory
import com.example.ui.components.CategoryArtworkTile
import com.example.ui.components.EmptyIllustrationBox
import com.example.ui.components.HeroStoreBanner
import com.example.ui.components.accentColor
import com.example.ui.components.containerColor
import com.example.ui.components.formatPrice
import com.example.ui.components.icon
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HardwareAmber
import com.example.ui.theme.HardwareAmberContainer
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseError
import com.example.ui.viewmodel.StoreUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorefrontScreen(
    uiState: StoreUiState,
    onSelectCategory: (PartCategory?) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddToCart: (PartEntity, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPartForDetail by remember { mutableStateOf<PartEntity?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 900.dp -> 3
            maxWidth >= 600.dp -> 2
            else -> 1
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroStoreBanner(
                    storeName = uiState.settings.storeName,
                    totalPartsCount = uiState.allParts.size,
                    inStockCount = uiState.availableParts.size
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_search_input"),
                    placeholder = {
                        Text(
                            text = "ابحث عن شاشة، بطارية، فلاتة، موديل...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "بحث"
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "مسح البحث"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryFilterRow(
                    allParts = uiState.availableParts,
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = onSelectCategory
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قائمة قطع الغيار المتاحة (${uiState.filteredParts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الأسعار بـ ${uiState.settings.currencySymbol}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (uiState.filteredParts.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyIllustrationBox(
                        icon = Icons.Rounded.Inventory2,
                        title = "لا توجد قطع مطابقة",
                        subtitle = "جرّب تغيير كلمة البحث أو اختيار تصنيف آخر، أو أضف قطعاً جديدة من الإدارة الرئيسية."
                    )
                }
            } else {
                items(uiState.filteredParts, key = { it.id }) { part ->
                    val qtyInCart = uiState.cartQtyByPartId[part.id] ?: 0
                    PartCatalogCard(
                        part = part,
                        currency = uiState.settings.currencySymbol,
                        qtyInCart = qtyInCart,
                        onOpenDetail = { selectedPartForDetail = part },
                        onQuickAdd = { onAddToCart(part, 1) }
                    )
                }
            }
        }
    }

    selectedPartForDetail?.let { part ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var selectedQty by remember(part.id) { mutableIntStateOf(1) }

        ModalBottomSheet(
            onDismissRequest = { selectedPartForDetail = null },
            sheetState = sheetState
        ) {
            PartDetailSheetContent(
                part = part,
                currency = uiState.settings.currencySymbol,
                selectedQty = selectedQty,
                onQtyChange = { selectedQty = it },
                onAddToCart = {
                    onAddToCart(part, selectedQty)
                    selectedPartForDetail = null
                }
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    allParts: List<PartEntity>,
    selectedCategory: PartCategory?,
    onSelectCategory: (PartCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            modifier = Modifier.testTag("filter_chip_all"),
            label = { Text("الكل (${allParts.size})") },
            leadingIcon = if (selectedCategory == null) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        PartCategory.entries.forEach { category ->
            val count = allParts.count { it.category == category }
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(if (isSelected) null else category) },
                modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}"),
                label = { Text("${category.labelAr} ($count)") },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.accentColor(),
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun PartCatalogCard(
    part: PartEntity,
    currency: String,
    qtyInCart: Int,
    onOpenDetail: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val inStock = part.stockQuantity > 0
    val lowStock = part.stockQuantity in 1..5

    Card(
        onClick = onOpenDetail,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("part_card_${part.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryArtworkTile(
                category = part.category,
                modifier = Modifier.size(76.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
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
                            text = part.category.badgeAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = part.category.accentColor(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = when {
                            !inStock -> RoseContainer
                            lowStock -> HardwareAmberContainer
                            else -> EmeraldContainer
                        },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = when {
                                !inStock -> "نفذت"
                                lowStock -> "باقي ${part.stockQuantity}"
                                else -> "متوفر (${part.stockQuantity})"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                !inStock -> RoseError
                                lowStock -> HardwareAmber
                                else -> EmeraldSuccess
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (qtyInCart > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "بالسلة: $qtyInCart",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${part.compatibleModels} • ${part.qualityGrade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPrice(part.price, currency),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onQuickAdd,
                        enabled = inStock,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_to_cart_button_${part.id}"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddShoppingCart,
                            contentDescription = "إضافة للسلة",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إضافة للسلة",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartDetailSheetContent(
    part: PartEntity,
    currency: String,
    selectedQty: Int,
    onQtyChange: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    val inStock = part.stockQuantity > 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CategoryArtworkTile(
                category = part.category,
                modifier = Modifier.size(72.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = part.category.labelAr,
                    style = MaterialTheme.typography.labelMedium,
                    color = part.category.accentColor(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatPrice(part.price, currency),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpecBadge(
                icon = Icons.Rounded.Smartphone,
                label = "التوافق",
                value = part.compatibleModels,
                modifier = Modifier.weight(1f)
            )
            SpecBadge(
                icon = Icons.Rounded.Security,
                label = "الضمان",
                value = if (part.warrantyDays > 0) "${part.warrantyDays} يوم" else "فحص عند الاستلام",
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "الجودة والمواصفات (${part.qualityGrade})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = part.description.ifBlank { "قطعة صيانة مفحوصة ومطابقة للمواصفات الفنية." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (inStock) "المتوفر بالمخزون: ${part.stockQuantity} قطعة" else "نفذت الكمية حالياً",
                style = MaterialTheme.typography.labelLarge,
                color = if (inStock) EmeraldSuccess else RoseError
            )

            if (inStock) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { if (selectedQty > 1) onQtyChange(selectedQty - 1) },
                        enabled = selectedQty > 1,
                        modifier = Modifier.testTag("detail_qty_minus")
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "إنقاص الكمية")
                    }
                    Text(
                        text = "$selectedQty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { if (selectedQty < part.stockQuantity) onQtyChange(selectedQty + 1) },
                        enabled = selectedQty < part.stockQuantity,
                        modifier = Modifier.testTag("detail_qty_plus")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "زيادة الكمية")
                    }
                }
            }
        }

        Button(
            onClick = onAddToCart,
            enabled = inStock,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("detail_add_to_cart_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Rounded.AddShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (inStock) {
                    "إضافة للسلة • ${formatPrice(part.price * selectedQty, currency)}"
                } else {
                    "غير متوفر حالياً"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SpecBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
