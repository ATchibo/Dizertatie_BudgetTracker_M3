package com.tchibolabs.budgettracker.core.uicomposers.api.dashboard

import androidx.compose.ui.graphics.Color
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.design.api.components.PieSlice
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionRow
import com.tchibolabs.budgettracker.core.uisystem.api.UiModel

enum class CurrencyMode { SELECTED_ONLY, ALL_CONVERTED }

data class CategoryBreakdown(
    val category: String,
    val totalAmount: Double,
    val color: Color,
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
    val costsSlices: List<PieSlice>,
    val incomeSlices: List<PieSlice>,
    val topIncome: List<TransactionRow>,
    val topCosts: List<TransactionRow>,
    val openPickerId: String?,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
) : UiModel {
    val displayCurrency: String get() = currency.name

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
            costsSlices = emptyList(),
            incomeSlices = emptyList(),
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
