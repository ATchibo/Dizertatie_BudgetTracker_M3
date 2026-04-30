package com.tchibolabs.budgettracker.core.uicomposers.impl.transactions

import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.model.TransactionOrder
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.FilterOption
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListScope
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListSourceRow
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapter
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapterFactory
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsFilter
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsUiModel
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.label
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
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

@HiltViewModel
class TransactionsUiAdapter @Inject constructor(
    private val repository: TransactionRepository,
    transactionListUiAdapterFactory: TransactionListUiAdapterFactory,
) : UiAdapter<TransactionsUiModel, TransactionsEvent>() {

    private val period = MutableStateFlow(TransactionPeriod.PAST_31_DAYS)
    private val order = MutableStateFlow(TransactionOrder.AMOUNT_DESC)
    private val openPickerId = MutableStateFlow<String?>(null)
    private val pendingDeleteId = MutableStateFlow<Long?>(null)

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
    private val transactionListUiAdapter: TransactionListUiAdapter =
        transactionListUiAdapterFactory.create(TransactionListScope.TRANSACTIONS)

    override val uiModel: StateFlow<TransactionsUiModel> = combine(
        repository.observeAll(),
        period,
        order,
        openPickerId,
        pendingDeleteId,
    ) { all, currentPeriod, currentOrder, openId, deleteId ->
        val sourceRows = all.applyFilters(currentPeriod, currentOrder).map { it.toSourceRow() }
        TransactionsUiModel(
            rows = transactionListUiAdapter.composeRows(sourceRows),
            filters = buildFilters(currentPeriod, currentOrder, openId),
            pendingDeleteId = deleteId,
            isLoading = false,
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), TransactionsUiModel.Initial)

    override fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.OpenPicker -> openPickerId.value = event.filterId
            is TransactionsEvent.ClosePicker -> {
                if (openPickerId.value == event.filterId) openPickerId.value = null
            }
            is TransactionsEvent.SelectOption -> {
                when (event.filterId) {
                    TransactionsFilter.ID_PERIOD ->
                        TransactionPeriod.values().firstOrNull { it.name == event.optionId }
                            ?.let { period.value = it }
                    TransactionsFilter.ID_ORDER ->
                        TransactionOrder.values().firstOrNull { it.name == event.optionId }
                            ?.let { order.value = it }
                }
                openPickerId.value = null
            }
            is TransactionsEvent.RequestDelete -> pendingDeleteId.value = event.id
            TransactionsEvent.CancelDelete -> pendingDeleteId.value = null
            TransactionsEvent.ConfirmDelete -> {
                val id = pendingDeleteId.value ?: return
                pendingDeleteId.value = null
                scope.launch { repository.delete(id) }
            }
        }
    }

    private fun buildFilters(
        currentPeriod: TransactionPeriod,
        currentOrder: TransactionOrder,
        openId: String?,
    ): List<TransactionsFilter> = listOf(
        TransactionsFilter(
            id = TransactionsFilter.ID_PERIOD,
            label = "Time Period",
            options = TransactionPeriod.values().map { FilterOption(it.name, it.label) },
            selectedOptionId = currentPeriod.name,
            isPickerOpen = openId == TransactionsFilter.ID_PERIOD,
        ),
        TransactionsFilter(
            id = TransactionsFilter.ID_ORDER,
            label = "Order",
            options = TransactionOrder.values().map { FilterOption(it.name, it.label) },
            selectedOptionId = currentOrder.name,
            isPickerOpen = openId == TransactionsFilter.ID_ORDER,
        ),
    )

    private fun List<Transaction>.applyFilters(
        period: TransactionPeriod,
        order: TransactionOrder,
    ): List<Transaction> {
        val cutoff = period.cutoffMs()
        val filtered = if (cutoff == null) this else filter { it.occurredAtEpochMs >= cutoff }
        return when (order) {
            TransactionOrder.AMOUNT_DESC ->
                filtered.sortedWith(compareByDescending<Transaction> { it.amount }.thenByDescending { it.id })
            TransactionOrder.AMOUNT_ASC ->
                filtered.sortedWith(compareBy<Transaction> { it.amount }.thenByDescending { it.id })
            TransactionOrder.DATE_DESC ->
                filtered.sortedWith(compareByDescending<Transaction> { it.occurredAtEpochMs }.thenByDescending { it.id })
            TransactionOrder.DATE_ASC ->
                filtered.sortedWith(compareBy<Transaction> { it.occurredAtEpochMs }.thenByDescending { it.id })
        }
    }

    private fun TransactionPeriod.cutoffMs(): Long? {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        return when (this) {
            TransactionPeriod.TODAY -> today.atStartOfDay(zone).toInstant().toEpochMilli()
            TransactionPeriod.PAST_7_DAYS -> today.minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
            TransactionPeriod.PAST_31_DAYS -> today.minusDays(31).atStartOfDay(zone).toInstant().toEpochMilli()
            TransactionPeriod.PAST_YEAR -> today.minusYears(1).atStartOfDay(zone).toInstant().toEpochMilli()
            TransactionPeriod.CURRENT_MONTH -> today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
            TransactionPeriod.ALL_TIME -> null
        }
    }

    private fun Transaction.toSourceRow(): TransactionListSourceRow {
        val date = Instant.ofEpochMilli(occurredAtEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return TransactionListSourceRow(
            id = id,
            category = category,
            note = note,
            dateLabel = date.format(dateFormatter),
            amount = amount,
            currency = currency.name,
            isIncome = kind == TransactionKind.Income,
        )
    }
}
