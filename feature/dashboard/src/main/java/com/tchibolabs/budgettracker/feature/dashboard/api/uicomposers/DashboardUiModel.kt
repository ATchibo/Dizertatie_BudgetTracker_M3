package com.tchibolabs.budgettracker.feature.dashboard.api.uicomposers

import androidx.compose.ui.graphics.Color
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.design.api.components.PieSlice
import com.tchibolabs.budgettracker.core.uisystem.api.UiModel

enum class CurrencyMode { SELECTED_ONLY, ALL_CONVERTED }

val CurrencyMode.label: String
    get() = when (this) {
        CurrencyMode.SELECTED_ONLY -> "Selected only"
        CurrencyMode.ALL_CONVERTED -> "All (converted)"
    }

val TransactionPeriod.dashboardLabel: String
    get() = when (this) {
        TransactionPeriod.TODAY -> "Today"
        TransactionPeriod.PAST_7_DAYS -> "Past 7 days"
        TransactionPeriod.PAST_31_DAYS -> "Past 31 days"
        TransactionPeriod.PAST_YEAR -> "Past year"
        TransactionPeriod.CURRENT_MONTH -> "Current month"
        TransactionPeriod.ALL_TIME -> "All time"
    }

data class CategoryBreakdown(
    val category: String,
    val totalAmount: Double,
    val color: Color,
)

data class DashboardTransactionRow(
    val id: Long,
    val category: String,
    val dateLabel: String,
    val amountText: String,
    val currency: String,
)

data class DashboardUiModel(
    val period: TransactionPeriod,
    val currency: Currency,
    val currencyMode: CurrencyMode,
    val totalIncome: Double,
    val totalCosts: Double,
    val totalBalance: Double,
    val costsBreakdown: List<CategoryBreakdown>,
    val incomeBreakdown: List<CategoryBreakdown>,
    val topIncome: List<DashboardTransactionRow>,
    val topCosts: List<DashboardTransactionRow>,
    val openPickerId: String?,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
) : UiModel {
    val displayCurrency: String get() = currency.name

    val costsSlices: List<PieSlice>
        get() = costsBreakdown.toSlices()

    val incomeSlices: List<PieSlice>
        get() = incomeBreakdown.toSlices()

    private fun List<CategoryBreakdown>.toSlices(): List<PieSlice> = map {
        PieSlice(label = it.category, value = it.totalAmount.toFloat(), color = it.color)
    }

    companion object {
        const val PICKER_PERIOD = "period"
        const val PICKER_CURRENCY = "currency"
        const val PICKER_CURRENCY_MODE = "currency_mode"

        val Initial = DashboardUiModel(
            period = TransactionPeriod.PAST_31_DAYS,
            currency = Currency.RON,
            currencyMode = CurrencyMode.SELECTED_ONLY,
            totalIncome = 0.0,
            totalCosts = 0.0,
            totalBalance = 0.0,
            costsBreakdown = emptyList(),
            incomeBreakdown = emptyList(),
            topIncome = emptyList(),
            topCosts = emptyList(),
            openPickerId = null,
            isLoading = true,
            isRefreshing = false,
        )
    }
}

sealed interface DashboardEvent {
    data class OpenPicker(val pickerId: String) : DashboardEvent
    data class ClosePicker(val pickerId: String) : DashboardEvent
    data class SelectOption(val pickerId: String, val optionId: String) : DashboardEvent
    data object Refresh : DashboardEvent
}
