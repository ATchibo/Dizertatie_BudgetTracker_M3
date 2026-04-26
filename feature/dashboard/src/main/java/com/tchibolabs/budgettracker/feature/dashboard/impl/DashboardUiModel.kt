package com.tchibolabs.budgettracker.feature.dashboard.impl

import androidx.compose.ui.graphics.Color
import com.tchibolabs.budgettracker.core.design.api.components.PieSlice
import com.tchibolabs.budgettracker.core.uisystem.api.UiModel

enum class DashboardInterval { Daily, Weekly, Monthly }

data class CategoryBreakdown(
    val category: String,
    val totalAmount: Double,
    val color: Color,
)

data class DashboardUiModel(
    val interval: DashboardInterval,
    val breakdown: List<CategoryBreakdown>,
    val isLoading: Boolean,
) : UiModel {
    val slices: List<PieSlice>
        get() = breakdown.map {
            PieSlice(label = it.category, value = it.totalAmount.toFloat(), color = it.color)
        }

    companion object {
        val Initial = DashboardUiModel(
            interval = DashboardInterval.Monthly,
            breakdown = emptyList(),
            isLoading = true,
        )
    }
}
