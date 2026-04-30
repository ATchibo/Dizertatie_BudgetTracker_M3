package com.tchibolabs.budgettracker.feature.home.api.uicomposers

import com.tchibolabs.budgettracker.core.uisystem.api.UiModel

data class HomeUiModel(
    val totalBalance: Double,
    val displayCurrency: String,
    val recentTransactionCount: Int,
    val isLoading: Boolean,
) : UiModel {
    companion object {
        val Initial = HomeUiModel(
            totalBalance = 0.0,
            displayCurrency = "USD",
            recentTransactionCount = 0,
            isLoading = true,
        )
    }
}

sealed interface HomeEvent {
    data object Refresh : HomeEvent
}
