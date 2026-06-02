package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryBreakdown
import com.example.data.MonthlyTrend
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

// Map categories to modern visual colors
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Alimentación" -> Color(0xFFFFA726) // Orange
        "Transporte" -> Color(0xFF29B6F6) // Light Blue
        "Vivienda" -> Color(0xFFEC407A) // Pink
        "Ocio y Entretenimiento" -> Color(0xFFAB47BC) // Purple
        "Salud" -> Color(0xFF26A69A) // Teal
        "Ingresos" -> IncomeGreen // Emerald Teal
        else -> Color(0xFF78909C) // Blue Grey
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    breakdown: List<CategoryBreakdown>,
    modifier: Modifier = Modifier
) {
    if (breakdown.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay datos de gastos en esta selección.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    var animationPlayed by remember { mutableStateOf(false) }
    val animateFactor by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    val totalAmount = breakdown.sumOf { it.amount }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .testTag("category_donut_chart"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 28.dp.toPx()
                val sizeMin = size.minDimension
                val arcSize = sizeMin - strokeWidth
                val topLeft = Offset(
                    (size.width - arcSize) / 2,
                    (size.height - arcSize) / 2
                )

                var startAngle = -90f

                breakdown.forEach { item ->
                    val sweepAngle = (item.percentage / 100f) * 360f * animateFactor
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = getCategoryColor(item.category),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Total Gastado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("-%.2f €", totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid-based category tags matching design style
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            breakdown.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(getCategoryColor(item.category), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.category}: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format("%.1f%% (%.1f€)", item.percentage, item.amount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyHistoryTrendChart(
    trends: List<MonthlyTrend>,
    modifier: Modifier = Modifier
) {
    if (trends.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Se necesitan más registros mensuales para mostrar tendencias.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    var animateTrigger by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        animateTrigger = true
    }

    // Determine max value for visual scale
    val maxVal = trends.flatMap { listOf(it.income, it.expenses) }.maxOrNull() ?: 100.0
    val scaleMax = (maxVal * 1.15).coerceAtLeast(100.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Evolución Mensual (Ingresos vs Gastos)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .testTag("monthly_trends_chart"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Label-Y indicator logic
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(String.format("%.0f€", scaleMax), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(String.format("%.0f€", scaleMax * 0.66), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(String.format("%.0f€", scaleMax * 0.33), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("0€", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Bars Area
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                trends.forEach { trend ->
                    val incomeHeightFrac = (trend.income / scaleMax).toFloat() * progress
                    val expenseHeightFrac = (trend.expenses / scaleMax).toFloat() * progress

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Bars side by side
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Income Bar
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .fillMaxHeight(incomeHeightFrac.coerceAtLeast(0.02f))
                                    .background(
                                        color = IncomeGreen,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Expense Bar
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .fillMaxHeight(expenseHeightFrac.coerceAtLeast(0.02f))
                                    .background(
                                        color = ExpenseRed,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Category Label Month Name
                        Text(
                            text = trend.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart reference label indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(IncomeGreen, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ingresos", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(ExpenseRed, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gastos", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
