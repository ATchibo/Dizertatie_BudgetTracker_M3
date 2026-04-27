package com.tchibolabs.budgettracker.core.design.api.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import kotlin.math.atan2
import kotlin.math.sqrt

data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color,
    val formattedValue: String = if (value == value.toLong().toFloat()) "${value.toLong()}.0" else "%.2f".format(value),
)

private const val EXPAND_DP = 10f
private const val DARKEN_FACTOR = 0.6f

/**
 * Custom pie chart drawn on Canvas — no external charting library.
 * Slices render proportionally to their [PieSlice.value]; zero-total inputs render nothing.
 * Pass [strokeWidth] to render as a donut, and [centerContent] to overlay content when no
 * slice is selected. Tapping a slice pops it outward, darkens the others, and shows a center
 * info circle; tapping the same slice again deselects it.
 */
@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp? = null,
    centerContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat() }
    var selectedIndex by remember(slices) { mutableStateOf<Int?>(null) }
    val selected = selectedIndex

    Box(
        modifier = modifier.pointerInput(slices) {
            detectTapGestures { offset ->
                if (total <= 0f) return@detectTapGestures
                val cx = size.width / 2f
                val cy = size.height / 2f
                val dx = offset.x - cx
                val dy = offset.y - cy
                val distance = sqrt(dx * dx + dy * dy)
                val dim = minOf(size.width, size.height).toFloat()
                val strokePx = strokeWidth?.toPx() ?: 0f
                val expandPx = EXPAND_DP * density
                val outerRadius = dim / 2f
                val innerRadius = if (strokeWidth != null) (dim - 2f * expandPx - 2f * strokePx) / 2f else 0f

                if (distance > outerRadius) {
                    selectedIndex = null
                    return@detectTapGestures
                }
                if (strokeWidth != null && distance < innerRadius) {
                    selectedIndex = null
                    return@detectTapGestures
                }

                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                angle = (angle + 90f + 360f) % 360f

                var startAngle = 0f
                var found: Int? = null
                slices.forEachIndexed { index, slice ->
                    val sweep = (slice.value / total) * 360f
                    if (angle >= startAngle && angle < startAngle + sweep) {
                        found = index
                    }
                    startAngle += sweep
                }
                selectedIndex = if (selectedIndex == found) null else found
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (total <= 0f) return@Canvas
            val dim = size.minDimension
            val strokePx = strokeWidth?.toPx() ?: 0f
            val expandPx = EXPAND_DP * density
            val arcDim = dim - strokePx - expandPx * 2f
            val expandedArcDim = arcDim + expandPx * 2f
            val baseTopLeft = Offset(
                x = (size.width - arcDim) / 2f,
                y = (size.height - arcDim) / 2f,
            )
            val expandedTopLeft = Offset(
                x = (size.width - expandedArcDim) / 2f,
                y = (size.height - expandedArcDim) / 2f,
            )
            val arcSize = Size(arcDim, arcDim)
            val expandedArcSize = Size(expandedArcDim, expandedArcDim)
            var startAngle = -90f

            slices.forEachIndexed { index, slice ->
                val sweep = (slice.value / total) * 360f
                val isSelected = index == selected
                val drawColor = if (selected == null || isSelected) slice.color else slice.color.darken(DARKEN_FACTOR)
                val drawTopLeft = if (isSelected) expandedTopLeft else baseTopLeft
                val drawSize = if (isSelected) expandedArcSize else arcSize

                if (strokeWidth != null) {
                    drawArc(
                        color = drawColor,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = drawTopLeft,
                        size = drawSize,
                        style = Stroke(width = strokePx),
                    )
                } else {
                    drawArc(
                        color = drawColor,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = drawTopLeft,
                        size = drawSize,
                    )
                }
                startAngle += sweep
            }
        }

        if (selected != null) {
            val slice = slices[selected]
            val percentage = if (total > 0f) slice.value / total * 100f else 0f
            Box(
                modifier = Modifier
                    .fillMaxSize(0.45f)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = slice.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = slice.formattedValue,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "%.1f%%".format(percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            centerContent?.invoke(this)
        }
    }
}

private fun Color.darken(factor: Float): Color = copy(
    red = (red * factor).coerceIn(0f, 1f),
    green = (green * factor).coerceIn(0f, 1f),
    blue = (blue * factor).coerceIn(0f, 1f),
)

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
            modifier = Modifier.size(220.dp),
        )
    }
}
