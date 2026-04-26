package com.tchibolabs.budgettracker.feature.transactions.impl

import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TransactionsEvent {
    data object CyclePeriod : TransactionsEvent
    data object CycleOrder : TransactionsEvent
    data class Delete(val id: Long) : TransactionsEvent
}

@HiltViewModel
class TransactionsUiAdapter @Inject constructor(
    private val repository: TransactionRepository,
) : UiAdapter<TransactionsUiModel, TransactionsEvent>() {

    private val period = MutableStateFlow(TimePeriod.Past31Days)
    private val order = MutableStateFlow(SortOrder.AmountDescending)

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    override val uiModel: StateFlow<TransactionsUiModel> = combine(
        repository.observeAll(),
        period,
        order,
    ) { all, currentPeriod, currentOrder ->
        TransactionsUiModel(
            rows = all.applyFilters(currentPeriod, currentOrder).map { it.toRow() },
            period = currentPeriod,
            order = currentOrder,
            isLoading = false,
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), TransactionsUiModel.Initial)

    override fun onEvent(event: TransactionsEvent) {
        when (event) {
            TransactionsEvent.CyclePeriod -> period.value = period.value.next()
            TransactionsEvent.CycleOrder -> order.value = order.value.next()
            is TransactionsEvent.Delete -> scope.launch { repository.delete(event.id) }
        }
    }

    private fun List<Transaction>.applyFilters(
        period: TimePeriod,
        order: SortOrder,
    ): List<Transaction> {
        val cutoff = period.cutoffMs()
        val filtered = if (cutoff == null) this else filter { it.occurredAtEpochMs >= cutoff }
        return when (order) {
            SortOrder.AmountDescending -> filtered.sortedByDescending { it.amount }
            SortOrder.AmountAscending -> filtered.sortedBy { it.amount }
            SortOrder.DateNewest -> filtered.sortedByDescending { it.occurredAtEpochMs }
            SortOrder.DateOldest -> filtered.sortedBy { it.occurredAtEpochMs }
        }
    }

    private fun TimePeriod.cutoffMs(): Long? {
        val day = 24L * 60 * 60 * 1000
        return when (this) {
            TimePeriod.Past7Days -> System.currentTimeMillis() - 7 * day
            TimePeriod.Past31Days -> System.currentTimeMillis() - 31 * day
            TimePeriod.AllTime -> null
        }
    }

    private fun TimePeriod.next(): TimePeriod {
        val all = TimePeriod.values()
        return all[(ordinal + 1) % all.size]
    }

    private fun SortOrder.next(): SortOrder {
        val all = SortOrder.values()
        return all[(ordinal + 1) % all.size]
    }

    private fun Transaction.toRow(): TransactionRow {
        val date = Instant.ofEpochMilli(occurredAtEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return TransactionRow(
            id = id,
            category = category,
            note = note,
            dateLabel = date.format(dateFormatter),
            amountText = amount.formatAmount(),
            currency = currency,
            isIncome = kind == TransactionKind.Income,
        )
    }

    private fun Double.formatAmount(): String =
        if (this == this.toLong().toDouble()) "${this.toLong()}.0" else "%.2f".format(this)
}
