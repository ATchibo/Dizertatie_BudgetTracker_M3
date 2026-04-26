package com.tchibolabs.budgettracker.feature.dashboard.impl

import androidx.compose.ui.graphics.Color
import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.CategoryBreakdown
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardInterval
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardUiModel
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardUiAdapter @Inject constructor(
    private val transactions: TransactionRepository,
) : UiAdapter<DashboardUiModel, DashboardEvent>() {

    private val _uiModel = MutableStateFlow(DashboardUiModel.Initial)
    override val uiModel: StateFlow<DashboardUiModel> = _uiModel.asStateFlow()

    private val palette = listOf(
        Color(0xFF66BB6A),
        Color(0xFF42A5F5),
        Color(0xFFFFA726),
        Color(0xFFAB47BC),
        Color(0xFF26A69A),
        Color(0xFFEF5350),
    )

    init {
        observe(DashboardInterval.Monthly)
    }

    override fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.IntervalSelected -> {
                _uiModel.update { it.copy(interval = event.interval) }
                observe(event.interval)
            }
        }
    }

    private fun observe(interval: DashboardInterval) {
        scope.launch {
            transactions.observeAll().collect { all ->
                val breakdown = computeBreakdown(all, interval)
                _uiModel.update { it.copy(breakdown = breakdown, isLoading = false) }
            }
        }
    }

    private fun computeBreakdown(
        all: List<Transaction>,
        interval: DashboardInterval,
    ): List<CategoryBreakdown> {
        val cutoffMs = cutoffFor(interval)
        return all.asSequence()
            .filter { it.kind == TransactionKind.Expense && it.occurredAtEpochMs >= cutoffMs }
            .groupBy { it.category }
            .entries
            .mapIndexed { index, (category, items) ->
                CategoryBreakdown(
                    category = category,
                    totalAmount = items.sumOf { it.amount },
                    color = palette[index % palette.size],
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    private fun cutoffFor(interval: DashboardInterval): Long {
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000
        return when (interval) {
            DashboardInterval.Daily -> now - day
            DashboardInterval.Weekly -> now - 7 * day
            DashboardInterval.Monthly -> now - 30 * day
        }
    }
}
