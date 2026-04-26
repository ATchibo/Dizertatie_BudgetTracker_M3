package com.tchibolabs.budgettracker.core.design.api.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme

data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color,
)

/**
 * Custom pie chart drawn on Canvas — no external charting library.
 * Slices render proportionally to their [PieSlice.value]; zero-total inputs render nothing.
 * Pass [strokeWidth] to render as a donut, and [centerContent] to overlay text inside it.
 */
@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp? = null,
    centerContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val total = slices.sumOf { it.value.toDouble() }.toFloat()
            if (total <= 0f) return@Canvas
            val dim = size.minDimension
            val strokePx = strokeWidth?.toPx() ?: 0f
            val arcDim = dim - strokePx
            val topLeft = Offset(
                x = (size.width - arcDim) / 2f,
                y = (size.height - arcDim) / 2f,
            )
            val arcSize = Size(arcDim, arcDim)
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total) * 360f
                if (strokeWidth != null) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx),
                    )
                } else {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize,
                    )
                }
                startAngle += sweep
            }
        }
        centerContent?.invoke(this)
    }
}

@Preview
@Composable
private fun PieChartPreview() {
    BudgetTrackerTheme {
        PieChart(
            slices = listOf(
                PieSlice("Food", 40f, Color(0xFF66BB6A)),
                PieSlice("Rent", 30f, Color(0xFF42A5F5)),
                PieSlice("Travel", 20f, Color(0xFFFFA726)),
                PieSlice("Other", 10f, Color(0xFFAB47BC)),
            ),
            modifier = Modifier.size(200.dp),
        )
    }
}

@Preview
@Composable
private fun PieChartDonutPreview() {
    BudgetTrackerTheme {
        PieChart(
            slices = listOf(
                PieSlice("Rent", 86f, Color(0xFF42A5F5)),
                PieSlice("Other", 14f, Color(0xFFAB47BC)),
            ),
            modifier = Modifier.size(220.dp),
            strokeWidth = 56.dp,
        )
    }
}
