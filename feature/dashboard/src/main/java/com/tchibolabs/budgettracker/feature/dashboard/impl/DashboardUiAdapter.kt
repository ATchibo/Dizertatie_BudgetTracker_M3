package com.tchibolabs.budgettracker.feature.dashboard.impl

import androidx.compose.ui.graphics.Color
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.data.api.repository.ExchangeRateRepository
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.CategoryBreakdown
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.CurrencyMode
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardTransactionRow
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardUiModel
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardUiAdapter @Inject constructor(
    private val transactions: TransactionRepository,
    private val rates: ExchangeRateRepository,
) : UiAdapter<DashboardUiModel, DashboardEvent>() {

    private val period = MutableStateFlow(TransactionPeriod.PAST_31_DAYS)
    private val currency = MutableStateFlow(Currency.RON)
    private val currencyMode = MutableStateFlow(CurrencyMode.SELECTED_ONLY)
    private val openPickerId = MutableStateFlow<String?>(null)

    private val palette = listOf(
        Color(0xFF42A5F5),
        Color(0xFF8B7E16),
        Color(0xFF66BB6A),
        Color(0xFFFFA726),
        Color(0xFFAB47BC),
        Color(0xFF26A69A),
        Color(0xFFEF5350),
    )

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val uiModel: StateFlow<DashboardUiModel> = combine(
        transactions.observeAll(),
        period,
        currency,
        currencyMode,
        openPickerId,
    ) { all, p, c, mode, openId ->
        Inputs(all = all, period = p, currency = c, mode = mode, openId = openId)
    }.mapLatest { compute(it) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), DashboardUiModel.Initial)

    override fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OpenPicker -> openPickerId.value = event.pickerId
            is DashboardEvent.ClosePicker -> {
                if (openPickerId.value == event.pickerId) openPickerId.value = null
            }
            is DashboardEvent.SelectOption -> {
                when (event.pickerId) {
                    DashboardUiModel.PICKER_PERIOD ->
                        TransactionPeriod.values().firstOrNull { it.name == event.optionId }
                            ?.let { period.value = it }
                    DashboardUiModel.PICKER_CURRENCY ->
                        Currency.values().firstOrNull { it.name == event.optionId }
                            ?.let { currency.value = it }
                    DashboardUiModel.PICKER_CURRENCY_MODE ->
                        CurrencyMode.values().firstOrNull { it.name == event.optionId }
                            ?.let { currencyMode.value = it }
                }
                openPickerId.value = null
            }
        }
    }

    private suspend fun compute(inputs: Inputs): DashboardUiModel {
        val cutoff = inputs.period.cutoffMs()
        val withinPeriod = inputs.all.filter { cutoff == null || it.occurredAtEpochMs >= cutoff }

        val resolved = withinPeriod.mapNotNull { tx ->
            val resolvedAmount = when (inputs.mode) {
                CurrencyMode.SELECTED_ONLY ->
                    if (tx.currency == inputs.currency) tx.amount else null
                CurrencyMode.ALL_CONVERTED ->
                    rates.convert(tx.amount, tx.currency.name, inputs.currency.name)
            }
            resolvedAmount?.let { tx to it }
        }

        val incomeItems = resolved.filter { it.first.kind == TransactionKind.Income }
        val expenseItems = resolved.filter { it.first.kind == TransactionKind.Expense }
        val totalIncome = incomeItems.sumOf { it.second }
        val totalCosts = expenseItems.sumOf { it.second }

        val topIncome = incomeItems
            .sortedByDescending { it.second }
            .take(5)
            .map { it.toRow(inputs.currency) }

        val topCosts = expenseItems
            .sortedByDescending { it.second }
            .take(5)
            .map { it.toRow(inputs.currency) }

        return DashboardUiModel(
            period = inputs.period,
            currency = inputs.currency,
            currencyMode = inputs.mode,
            totalIncome = totalIncome,
            totalCosts = totalCosts,
            totalBalance = totalIncome - totalCosts,
            costsBreakdown = expenseItems.toBreakdown(),
            incomeBreakdown = incomeItems.toBreakdown(),
            topIncome = topIncome,
            topCosts = topCosts,
            openPickerId = inputs.openId,
            isLoading = false,
        )
    }

    private fun List<Pair<Transaction, Double>>.toBreakdown(): List<CategoryBreakdown> =
        groupBy { it.first.category }
            .entries
            .map { (category, items) -> category to items.sumOf { it.second } }
            .sortedByDescending { it.second }
            .mapIndexed { index, (category, total) ->
                CategoryBreakdown(
                    category = category,
                    totalAmount = total,
                    color = palette[index % palette.size],
                )
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

    private fun Transaction.toRow(): DashboardTransactionRow {
        val date = Instant.ofEpochMilli(occurredAtEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return DashboardTransactionRow(
            id = id,
            category = category,
            dateLabel = date.format(dateFormatter),
            amountText = amount.formatAmount(),
            currency = currency.name,
        )
    }

    private fun Pair<Transaction, Double>.toRow(displayCurrency: Currency): DashboardTransactionRow {
        val transaction = first
        val amount = second
        val date = Instant.ofEpochMilli(transaction.occurredAtEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return DashboardTransactionRow(
            id = transaction.id,
            category = transaction.category,
            dateLabel = date.format(dateFormatter),
            amountText = amount.formatAmount(),
            currency = displayCurrency.name,
        )
    }

    private fun Double.formatAmount(): String =
        if (this == this.toLong().toDouble()) "${this.toLong()}.0" else "%.2f".format(this)

    private data class Inputs(
        val all: List<Transaction>,
        val period: TransactionPeriod,
        val currency: Currency,
        val mode: CurrencyMode,
        val openId: String?,
    )
}
