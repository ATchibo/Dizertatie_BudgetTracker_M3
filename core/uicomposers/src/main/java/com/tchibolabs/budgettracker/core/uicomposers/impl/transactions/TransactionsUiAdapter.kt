package com.tchibolabs.budgettracker.core.uicomposers.impl.transactions

import android.content.Context
import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.model.TransactionOrder
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.R
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.FilterOption
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListScope
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListSourceRow
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapter
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapterFactory
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsFilter
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsUiModel
import com.tchibolabs.budgettracker.core.data.api.model.cutoffMs
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.tchibolabs.budgettracker.core.uicomposers.impl.transactionDateFormatter
import java.time.Instant
import java.time.ZoneId
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
    @ApplicationContext private val context: Context,
    transactionListUiAdapterFactory: TransactionListUiAdapterFactory,
) : UiAdapter<TransactionsUiModel, TransactionsEvent>() {

    private val period = MutableStateFlow(TransactionPeriod.PAST_31_DAYS)
    private val order = MutableStateFlow(TransactionOrder.AMOUNT_DESC)
    private val openPickerId = MutableStateFlow<String?>(null)
    private val pendingDeleteId = MutableStateFlow<Long?>(null)

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
            label = context.getString(R.string.transactions_filter_period_label),
            options = listOf(
                FilterOption(TransactionPeriod.TODAY.name, context.getString(R.string.period_today)),
                FilterOption(TransactionPeriod.PAST_7_DAYS.name, context.getString(R.string.period_past_7_days)),
                FilterOption(TransactionPeriod.PAST_31_DAYS.name, context.getString(R.string.period_past_31_days)),
                FilterOption(TransactionPeriod.PAST_YEAR.name, context.getString(R.string.period_past_year)),
                FilterOption(TransactionPeriod.CURRENT_MONTH.name, context.getString(R.string.period_current_month)),
                FilterOption(TransactionPeriod.ALL_TIME.name, context.getString(R.string.period_all_time)),
            ),
            selectedOptionId = currentPeriod.name,
            isPickerOpen = openId == TransactionsFilter.ID_PERIOD,
        ),
        TransactionsFilter(
            id = TransactionsFilter.ID_ORDER,
            label = context.getString(R.string.transactions_filter_order_label),
            options = listOf(
                FilterOption(TransactionOrder.DATE_ASC.name, context.getString(R.string.order_date_oldest_first)),
                FilterOption(TransactionOrder.DATE_DESC.name, context.getString(R.string.order_date_newest_first)),
                FilterOption(TransactionOrder.AMOUNT_ASC.name, context.getString(R.string.order_amount_ascending)),
                FilterOption(TransactionOrder.AMOUNT_DESC.name, context.getString(R.string.order_amount_descending)),
            ),
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

    private fun Transaction.toSourceRow(): TransactionListSourceRow {
        val date = Instant.ofEpochMilli(occurredAtEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return TransactionListSourceRow(
            id = id,
            category = category,
            note = note,
            dateLabel = date.format(transactionDateFormatter),
            amount = amount,
            currency = currency.name,
            isIncome = kind == TransactionKind.Income,
        )
    }
}
