package com.tchibolabs.budgettracker.core.navigation.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface BudgetRoute : NavKey {

    @Serializable
    data object Home : BudgetRoute

    @Serializable
    data object Dashboard : BudgetRoute

    @Serializable
    data object Transactions : BudgetRoute

    @Serializable
    data class TransactionsForm(val transactionId: Long? = null) : BudgetRoute
}

enum class BudgetTab(val routeKey: String) {
    Transactions("transactions"),
    Dashboard("dashboard"),
}
