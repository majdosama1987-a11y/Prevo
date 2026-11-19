package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderStatus
import com.example.data.model.PartCategory
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HardwareAmber
import com.example.ui.theme.HardwareAmberContainer
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.TechBlue
import com.example.ui.theme.TechBlueContainer
import com.example.ui.theme.TechBlueLight
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatPrice(amount: Double, currency: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        minimumFractionDigits = 0
    }
    return "${formatter.format(amount)} $currency"
}

fun formatArabicDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM • hh:mm a", Locale("ar"))
    return sdf.format(Date(timestamp))
}

fun PartCategory.icon(): ImageVector = when (this) {
    PartCategory.SCREENS -> Icons.Rounded.PhoneAndroid
    PartCategory.BATTERIES -> Icons.Rounded.BatteryChargingFull
    PartCategory.FLEX_CABLES -> Icons.Rounded.Cable
    PartCategory.OTHER -> Icons.Rounded.Memory
}

fun PartCategory.accentColor(): Color = when (this) {
    PartCategory.SCREENS -> TechBlue
    PartCategory.BATTERIES -> EmeraldSuccess
    PartCategory.FLEX_CABLES -> HardwareAmber
    PartCategory.OTHER -> IndigoAccent
}

fun PartCategory.containerColor(): Color = when (this) {
    PartCategory.SCREENS -> TechBlueContainer
    PartCategory.BATTERIES -> EmeraldContainer
    PartCategory.FLEX_CABLES -> HardwareAmberContainer
    PartCategory.OTHER -> IndigoContainer
}

fun OrderStatus.colors(): Pair<Color, Color> = when (this) {
    OrderStatus.PENDING -> HardwareAmber to HardwareAmberContainer
    OrderStatus.PROCESSING -> TechBlue to TechBlueContainer
    OrderStatus.SHIPPED -> IndigoAccent to IndigoContainer
    OrderStatus.DELIVERED -> EmeraldSuccess to EmeraldContainer
    OrderStatus.CANCELLED -> RoseError to RoseContainer
}

@Composable
fun HeroStoreBanner(
    storeName: String,
    totalPartsCount: Int,
    inStockCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = DeepSlate,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(DeepSlate, Slate800, Color(0xFF0C4A6E))
                    )
                )
        ) {
            // Precision circuit board decorative backdrop
            Canvas(modifier = Modifier.matchParentSize()) {
                val traceColor = TechBlueLight.copy(alpha = 0.14f)
                val amberColor = Color(0xFFF59E0B).copy(alpha = 0.18f)

                drawCircle(
                    color = traceColor,
                    radius = size.height * 0.65f,
                    center = Offset(size.width * 0.12f, size.height * 0.15f)
                )
                drawCircle(
                    color = amberColor,
                    radius = size.height * 0.4f,
                    center = Offset(size.width * 0.88f, size.height * 0.85f)
                )

                // PCB trace lines
                drawLine(
                    color = traceColor,
                    start = Offset(0f, size.height * 0.72f),
                    end = Offset(size.width * 0.35f, size.height * 0.72f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = traceColor,
                    start = Offset(size.width * 0.35f, size.height * 0.72f),
                    end = Offset(size.width * 0.45f, size.height * 0.42f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = amberColor,
                    radius = 7f,
                    center = Offset(size.width * 0.45f, size.height * 0.42f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = TechBlueLight.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = TechBlueLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "قطع أصلية ومختبرة",
                                style = MaterialTheme.typography.labelSmall,
                                color = TechBlueLight
                            )
                        }
                    }
                    Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.20f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "$inStockCount متوفر حالياً",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFCD34D),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = storeName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "شاشات • بطاريات • فلاتات شحن • آيسيات وقطع غيار دقيقة ($totalPartsCount صنف)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
fun CategoryArtworkTile(
    category: PartCategory,
    modifier: Modifier = Modifier
) {
    val accent = category.accentColor()
    val bgTop = when (category) {
        PartCategory.SCREENS -> Color(0xFF0F172A)
        PartCategory.BATTERIES -> Color(0xFF064E3B)
        PartCategory.FLEX_CABLES -> Color(0xFF451A03)
        PartCategory.OTHER -> Color(0xFF1E1B4B)
    }
    val bgBottom = when (category) {
        PartCategory.SCREENS -> Color(0xFF0369A1)
        PartCategory.BATTERIES -> Color(0xFF059669)
        PartCategory.FLEX_CABLES -> Color(0xFFB45309)
        PartCategory.OTHER -> Color(0xFF4338CA)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(bgTop, bgBottom))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (category) {
                PartCategory.SCREENS -> {
                    // Stylized OLED phone screen with luminous diagonal reflection
                    val w = size.width * 0.48f
                    val h = size.height * 0.72f
                    val left = (size.width - w) / 2f
                    val top = (size.height - h) / 2f
                    drawRoundRect(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = 4f)
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(left + 8f, top + h * 0.35f),
                        end = Offset(left + w - 8f, top + h * 0.15f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
                PartCategory.BATTERIES -> {
                    val w = size.width * 0.44f
                    val h = size.height * 0.65f
                    val left = (size.width - w) / 2f
                    val top = (size.height - h) / 2f + 4f
                    drawRoundRect(
                        color = Color(0xFF6EE7B7),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 4f)
                    )
                    drawRoundRect(
                        color = Color(0xFF6EE7B7),
                        topLeft = Offset(size.width * 0.42f, top - 8f),
                        size = Size(size.width * 0.16f, 8f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
                PartCategory.FLEX_CABLES -> {
                    // Copper flex traces & connector
                    drawRoundRect(
                        color = Color(0xFFFCD34D).copy(alpha = 0.28f),
                        topLeft = Offset(size.width * 0.2f, size.height * 0.3f),
                        size = Size(size.width * 0.6f, size.height * 0.4f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    for (i in 0..3) {
                        val y = size.height * (0.38f + i * 0.08f)
                        drawLine(
                            color = Color(0xFFFBBF24),
                            start = Offset(size.width * 0.24f, y),
                            end = Offset(size.width * 0.76f, y),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                PartCategory.OTHER -> {
                    // Precision IC microchip grid
                    val boxSize = size.minDimension * 0.52f
                    val left = (size.width - boxSize) / 2f
                    val top = (size.height - boxSize) / 2f
                    drawRoundRect(
                        color = Color(0xFFA5B4FC),
                        topLeft = Offset(left, top),
                        size = Size(boxSize, boxSize),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 4f)
                    )
                }
            }
        }
        Icon(
            imageVector = category.icon(),
            contentDescription = category.labelAr,
            tint = PureWhite.copy(alpha = 0.92f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun EmptyIllustrationBox(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(4.dp))
            action()
        }
    }
}

@Composable
fun StatusPill(status: OrderStatus, modifier: Modifier = Modifier) {
    val (fg, bg) = status.colors()
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = status.labelAr,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun OrderProgressStepper(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    if (status == OrderStatus.CANCELLED) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = RoseContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "تم إلغاء هذا الطلب",
                style = MaterialTheme.typography.labelLarge,
                color = RoseError,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        return
    }

    val steps = listOf(
        OrderStatus.PENDING,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED
    )
    val currentStep = status.stepIndex

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val reached = index <= currentStep
            val activeColor = if (reached) TechBlue else MaterialTheme.colorScheme.outlineVariant

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(
                                    if (index <= currentStep) TechBlue else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = if (reached) TechBlue else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (reached) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = step.labelAr,
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700
                                )
                            }
                        }
                    }

                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(
                                    if (index < currentStep) TechBlue else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = step.labelAr,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reached) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
