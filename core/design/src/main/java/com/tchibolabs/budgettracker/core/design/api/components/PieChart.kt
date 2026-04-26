package com.tchibolabs.budgettracker.core.design.api.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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
 */
@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Canvas(modifier = modifier) {
        if (total <= 0f) return@Canvas
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = (slice.value / total) * 360f
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(size.minDimension, size.minDimension),
            )
            startAngle += sweep
        }
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
