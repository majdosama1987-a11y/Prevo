package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.PartEntity
import com.example.data.model.PartCategory
import com.example.ui.components.accentColor
import com.example.ui.components.icon
import com.example.ui.theme.RoseError

@Composable
fun AdminPartEditorDialog(
    initialPart: PartEntity?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (PartEntity) -> Unit
) {
    var name by remember(initialPart) { mutableStateOf(initialPart?.name ?: "") }
    var compatibleModels by remember(initialPart) { mutableStateOf(initialPart?.compatibleModels ?: "") }
    var selectedCategory by remember(initialPart) { mutableStateOf(initialPart?.category ?: PartCategory.SCREENS) }
    var priceText by remember(initialPart) {
        mutableStateOf(
            initialPart?.price?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: ""
        )
    }
    var stockText by remember(initialPart) { mutableStateOf(initialPart?.stockQuantity?.toString() ?: "10") }
    var qualityGrade by remember(initialPart) { mutableStateOf(initialPart?.qualityGrade ?: "أصلي") }
    var warrantyText by remember(initialPart) { mutableStateOf(initialPart?.warrantyDays?.toString() ?: "60") }
    var description by remember(initialPart) { mutableStateOf(initialPart?.description ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialPart == null) "إضافة قطعة صيانة جديدة" else "تعديل بيانات القطعة",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "التصنيف *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PartCategory.entries.forEach { category ->
                        val selected = selectedCategory == category
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = category },
                            modifier = Modifier.testTag("admin_category_chip_${category.name.lowercase()}"),
                            label = { Text(category.labelAr) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (selected) Icons.Rounded.Check else category.icon(),
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

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("اسم القطعة *") },
                    placeholder = { Text("مثال: شاشة آيفون 14 برو OLED") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_part_name_input")
                )

                OutlinedTextField(
                    value = compatibleModels,
                    onValueChange = { compatibleModels = it; errorMessage = null },
                    label = { Text("الأجهزة المتوافقة *") },
                    placeholder = { Text("مثال: iPhone 14 Pro") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_part_models_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it; errorMessage = null },
                        label = { Text("السعر ($currency) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_part_price_input")
                    )
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it; errorMessage = null },
                        label = { Text("المخزون *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_part_stock_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = qualityGrade,
                        onValueChange = { qualityGrade = it },
                        label = { Text("الجودة / الفئة") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("admin_part_quality_input")
                    )
                    OutlinedTextField(
                        value = warrantyText,
                        onValueChange = { warrantyText = it },
                        label = { Text("الضمان (يوم)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("admin_part_warranty_input")
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف وملاحظات فنية") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_part_description_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = RoseError,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = priceText.trim().toDoubleOrNull()
                    val parsedStock = stockText.trim().toIntOrNull()
                    val parsedWarranty = warrantyText.trim().toIntOrNull() ?: 0

                    when {
                        name.trim().length < 2 -> errorMessage = "يرجى إدخال اسم القطعة"
                        compatibleModels.trim().isEmpty() -> errorMessage = "يرجى تحديد الموديلات المتوافقة"
                        parsedPrice == null || parsedPrice <= 0.0 -> errorMessage = "يرجى إدخال سعر صحيح أكبر من صفر"
                        parsedStock == null || parsedStock < 0 -> errorMessage = "يرجى إدخال كمية مخزون صحيحة"
                        else -> {
                            onSave(
                                PartEntity(
                                    id = initialPart?.id ?: 0L,
                                    name = name.trim(),
                                    compatibleModels = compatibleModels.trim(),
                                    category = selectedCategory,
                                    price = parsedPrice,
                                    stockQuantity = parsedStock,
                                    qualityGrade = qualityGrade.trim().ifBlank { "أصلي" },
                                    warrantyDays = parsedWarranty.coerceAtLeast(0),
                                    description = description.trim(),
                                    createdAt = initialPart?.createdAt ?: System.currentTimeMillis()
                                )
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("admin_save_part_button")
            ) {
                Text(if (initialPart == null) "إضافة القطعة" else "حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("admin_cancel_part_button")
            ) {
                Text("إلغاء")
            }
        }
    )
}
