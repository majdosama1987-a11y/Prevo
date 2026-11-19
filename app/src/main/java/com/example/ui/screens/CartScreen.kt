package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCartCheckout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItemDetail
import com.example.ui.components.CategoryArtworkTile
import com.example.ui.components.EmptyIllustrationBox
import com.example.ui.components.formatPrice
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.viewmodel.StoreUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    uiState: StoreUiState,
    onUpdateQuantity: (cartItemId: Long, partId: Long, newQuantity: Int) -> Unit,
    onRemoveItem: (cartItemId: Long) -> Unit,
    onClearCart: () -> Unit,
    onPlaceOrder: (
        customerName: String,
        customerPhone: String,
        workshopName: String,
        deliveryAddress: String,
        notes: String,
        onSuccess: (String) -> Unit
    ) -> Unit,
    onBrowseStore: () -> Unit,
    onViewOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var confirmedOrderNumber by remember { mutableStateOf<String?>(null) }

    val currency = uiState.settings.currencySymbol

    if (uiState.cartItems.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmptyIllustrationBox(
                icon = Icons.Rounded.ShoppingBag,
                title = "سلة المشتريات فارغة",
                subtitle = "تصفح الشاشات والبطاريات والفلاتات وأضف القطع المطلوبة إلى سلتك.",
                action = {
                    Button(
                        onClick = onBrowseStore,
                        modifier = Modifier.testTag("empty_cart_browse_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("تصفح قطع الصيانة")
                    }
                }
            )
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "محتويات السلة (${uiState.cartTotalCount} قطعة)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onClearCart,
                    modifier = Modifier.testTag("clear_cart_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = null,
                        tint = RoseError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إفراغ السلة", color = RoseError)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.cartItems, key = { it.cartItemId }) { item ->
                    CartItemCard(
                        item = item,
                        currency = currency,
                        onIncrease = {
                            onUpdateQuantity(item.cartItemId, item.part.id, item.quantity + 1)
                        },
                        onDecrease = {
                            onUpdateQuantity(item.cartItemId, item.part.id, item.quantity - 1)
                        },
                        onRemove = { onRemoveItem(item.cartItemId) }
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإجمالي المستحق",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatPrice(uiState.cartTotalAmount, currency),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { showCheckoutSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("proceed_checkout_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.ShoppingCartCheckout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إتمام الطلب (${uiState.cartTotalCount})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showCheckoutSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = sheetState
        ) {
            CheckoutFormContent(
                totalAmount = uiState.cartTotalAmount,
                totalCount = uiState.cartTotalCount,
                currency = currency,
                onSubmit = { name, phone, workshop, address, notes ->
                    onPlaceOrder(name, phone, workshop, address, notes) { orderNumber ->
                        showCheckoutSheet = false
                        confirmedOrderNumber = orderNumber
                    }
                }
            )
        }
    }

    confirmedOrderNumber?.let { orderNum ->
        AlertDialog(
            onDismissRequest = { confirmedOrderNumber = null },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text(
                    text = "تم إرسال طلبك بنجاح!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "رقم الطلب: $orderNum\nتم استلام طلبك وإشعار الإدارة الرئيسية لتجهيز القطع وشحنها."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmedOrderNumber = null
                        onViewOrders()
                    },
                    modifier = Modifier.testTag("view_orders_after_checkout_button")
                ) {
                    Text("متابعة طلباتي")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        confirmedOrderNumber = null
                        onBrowseStore()
                    }
                ) {
                    Text("العودة للمتجر")
                }
            }
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItemDetail,
    currency: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.cartItemId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryArtworkTile(
                category = item.part.category,
                modifier = Modifier.size(62.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.part.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "سعر القطعة: ${formatPrice(item.part.price, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المجموع: ${formatPrice(item.lineTotal, currency)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove_cart_item_${item.cartItemId}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "حذف القطعة",
                        tint = RoseError
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.testTag("cart_qty_minus_${item.cartItemId}")
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "إنقاص")
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = onIncrease,
                        enabled = item.quantity < item.part.stockQuantity,
                        modifier = Modifier.testTag("cart_qty_plus_${item.cartItemId}")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "زيادة")
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutFormContent(
    totalAmount: Double,
    totalCount: Int,
    currency: String,
    onSubmit: (
        name: String,
        phone: String,
        workshop: String,
        address: String,
        notes: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var workshopName by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "بيانات إتمام الطلب",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "أدخل بيانات الفني أو محل الصيانة لإرسال الطلب إلى الإدارة الرئيسية",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it; errorMessage = null },
            label = { Text("الاسم الكامل *") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("checkout_name_input")
        )

        OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it; errorMessage = null },
            label = { Text("رقم الجوال للتواصل *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("checkout_phone_input")
        )

        OutlinedTextField(
            value = workshopName,
            onValueChange = { workshopName = it },
            label = { Text("اسم محل / ورشة الصيانة (اختياري)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("checkout_workshop_input")
        )

        OutlinedTextField(
            value = deliveryAddress,
            onValueChange = { deliveryAddress = it; errorMessage = null },
            label = { Text("عنوان التوصيل *") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("checkout_address_input")
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("ملاحظات إضافية (اختياري)") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("checkout_notes_input")
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = RoseError,
                style = MaterialTheme.typography.labelLarge
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الإجمالي ($totalCount قطعة)",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formatPrice(totalAmount, currency),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = {
                when {
                    customerName.trim().length < 2 -> errorMessage = "يرجى إدخال الاسم الكامل"
                    customerPhone.trim().length < 7 -> errorMessage = "يرجى إدخال رقم جوال صحيح"
                    deliveryAddress.trim().length < 4 -> errorMessage = "يرجى إدخال عنوان التوصيل"
                    else -> onSubmit(customerName, customerPhone, workshopName, deliveryAddress, notes)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_order_submit_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "تأكيد وإرسال الطلب",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
