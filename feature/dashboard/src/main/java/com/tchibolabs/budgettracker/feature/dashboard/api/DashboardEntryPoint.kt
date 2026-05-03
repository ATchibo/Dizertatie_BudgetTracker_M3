package com.tchibolabs.budgettracker.feature.dashboard.api

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.tchibolabs.budgettracker.feature.dashboard.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.design.api.components.FilterChipCard
import com.tchibolabs.budgettracker.core.design.api.components.OptionsBottomSheet
import com.tchibolabs.budgettracker.core.design.api.components.PickerOption
import com.tchibolabs.budgettracker.core.design.api.components.PieChart
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.CategoryBreakdown
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.CurrencyMode
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardUiModel
import com.tchibolabs.budgettracker.core.uicomposers.api.formatAmount
import com.tchibolabs.budgettracker.core.uicomposers.R as UiR
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiComposer
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionRow
import com.tchibolabs.budgettracker.core.uicomposers.impl.dashboard.DashboardUiAdapter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardEntryPoint(
    modifier: Modifier = Modifier,
    adapter: DashboardUiAdapter = hiltViewModel(),
) {
    val uiModel by adapter.uiModel.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(adapter) {
        adapter.conversionError.collect {
            Toast.makeText(
                context,
                context.getString(R.string.dashboard_conversion_error),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    DashboardScreen(
        uiModel = uiModel,
        onEvent = adapter::onEvent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    uiModel: DashboardUiModel,
    onEvent: (DashboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = uiModel.isRefreshing,
        onRefresh = { onEvent(DashboardEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChipCard(
                        label = stringResource(R.string.dashboard_filter_period),
                        value = uiModel.period.toLabel(),
                        onClick = { onEvent(DashboardEvent.OpenPicker(DashboardUiModel.PICKER_PERIOD)) },
                        modifier = Modifier.weight(1f),
                    )
                    FilterChipCard(
                        label = stringResource(R.string.dashboard_filter_currency),
                        value = uiModel.currency.name,
                        onClick = { onEvent(DashboardEvent.OpenPicker(DashboardUiModel.PICKER_CURRENCY)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                FilterChipCard(
                    label = stringResource(R.string.dashboard_filter_currency_mode),
                    value = uiModel.currencyMode.toLabel(),
                    onClick = { onEvent(DashboardEvent.OpenPicker(DashboardUiModel.PICKER_CURRENCY_MODE)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                TotalsCard(label = stringResource(R.string.dashboard_total_income), amount = uiModel.totalIncome, currency = uiModel.displayCurrency)
            }
            item {
                TotalsCard(label = stringResource(R.string.dashboard_total_costs), amount = uiModel.totalCosts, currency = uiModel.displayCurrency)
            }
            item {
                TotalsCard(label = stringResource(R.string.dashboard_total_balance), amount = uiModel.totalBalance, currency = uiModel.displayCurrency)
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (uiModel.costsBreakdown.isNotEmpty()) {
                item {
                    CategorySection(title = stringResource(R.string.dashboard_section_costs_by_category)) {
                        CostsDonut(uiModel = uiModel)
                        BreakdownLegend(breakdown = uiModel.costsBreakdown)
                    }
                }
            }
            item {
                TransactionListUiComposer(
                    title = stringResource(R.string.dashboard_top_costs),
                    rows = uiModel.topCosts,
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (uiModel.incomeBreakdown.isNotEmpty()) {
                item {
                    CategorySection(title = stringResource(R.string.dashboard_section_income_by_category)) {
                        IncomePie(uiModel = uiModel)
                    }
                }
            }
            item {
                TransactionListUiComposer(
                    title = stringResource(R.string.dashboard_top_income),
                    rows = uiModel.topIncome,
                )
            }
        }

        when (uiModel.openPickerId) {
            DashboardUiModel.PICKER_PERIOD -> OptionsBottomSheet(
                title = stringResource(R.string.dashboard_filter_period),
                options = periodPickerOptions(),
                selectedOptionId = uiModel.period.name,
                onSelect = { onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_PERIOD, it.id)) },
                onDismiss = { onEvent(DashboardEvent.ClosePicker(DashboardUiModel.PICKER_PERIOD)) },
            )
            DashboardUiModel.PICKER_CURRENCY -> OptionsBottomSheet(
                title = stringResource(R.string.dashboard_filter_currency),
                options = Currency.values().map { PickerOption(it.name, it.name) },
                selectedOptionId = uiModel.currency.name,
                onSelect = { onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_CURRENCY, it.id)) },
                onDismiss = { onEvent(DashboardEvent.ClosePicker(DashboardUiModel.PICKER_CURRENCY)) },
            )
            DashboardUiModel.PICKER_CURRENCY_MODE -> OptionsBottomSheet(
                title = stringResource(R.string.dashboard_filter_currency_mode),
                options = currencyModePickerOptions(),
                selectedOptionId = uiModel.currencyMode.name,
                onSelect = { onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_CURRENCY_MODE, it.id)) },
                onDismiss = { onEvent(DashboardEvent.ClosePicker(DashboardUiModel.PICKER_CURRENCY_MODE)) },
            )
        }
    }
}

@Composable
private fun TotalsCard(
    label: String,
    amount: Double,
    currency: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${amount.formatAmount()} $currency",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

@Composable
private fun CostsDonut(uiModel: DashboardUiModel) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        PieChart(
            slices = uiModel.costsSlices,
            modifier = Modifier.size(260.dp),
        )
    }
}

@Composable
private fun IncomePie(uiModel: DashboardUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            PieChart(
                slices = uiModel.incomeSlices,
                modifier = Modifier.size(240.dp),
            )
        }
        BreakdownLegend(breakdown = uiModel.incomeBreakdown)
    }
}

@Composable
private fun BreakdownLegend(breakdown: List<CategoryBreakdown>) {
    val total = breakdown.sumOf { it.totalAmount }
    if (total <= 0.0) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        breakdown.forEach { entry ->
            LegendRow(
                color = entry.color,
                label = entry.category.uppercase(),
                percent = (entry.totalAmount / total).toFloat(),
            )
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    label: String,
    percent: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "%.2f%%".format(percent * 100f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun TransactionPeriod.toLabel(): String = when (this) {
    TransactionPeriod.TODAY -> stringResource(UiR.string.period_today)
    TransactionPeriod.PAST_7_DAYS -> stringResource(UiR.string.period_past_7_days)
    TransactionPeriod.PAST_31_DAYS -> stringResource(UiR.string.period_past_31_days)
    TransactionPeriod.PAST_YEAR -> stringResource(UiR.string.period_past_year)
    TransactionPeriod.CURRENT_MONTH -> stringResource(UiR.string.period_current_month)
    TransactionPeriod.ALL_TIME -> stringResource(UiR.string.period_all_time)
}

@Composable
private fun CurrencyMode.toLabel(): String = when (this) {
    CurrencyMode.SELECTED_ONLY -> stringResource(R.string.currency_mode_selected_only)
    CurrencyMode.ALL_CONVERTED -> stringResource(R.string.currency_mode_all_converted)
}

@Composable
private fun periodPickerOptions(): List<PickerOption> = listOf(
    PickerOption(TransactionPeriod.TODAY.name, stringResource(UiR.string.period_today)),
    PickerOption(TransactionPeriod.PAST_7_DAYS.name, stringResource(UiR.string.period_past_7_days)),
    PickerOption(TransactionPeriod.PAST_31_DAYS.name, stringResource(UiR.string.period_past_31_days)),
    PickerOption(TransactionPeriod.PAST_YEAR.name, stringResource(UiR.string.period_past_year)),
    PickerOption(TransactionPeriod.CURRENT_MONTH.name, stringResource(UiR.string.period_current_month)),
    PickerOption(TransactionPeriod.ALL_TIME.name, stringResource(UiR.string.period_all_time)),
)

@Composable
private fun currencyModePickerOptions(): List<PickerOption> = listOf(
    PickerOption(CurrencyMode.SELECTED_ONLY.name, stringResource(R.string.currency_mode_selected_only)),
    PickerOption(CurrencyMode.ALL_CONVERTED.name, stringResource(R.string.currency_mode_all_converted)),
)

@Preview
@Composable
private fun DashboardScreenPreview() {
    BudgetTrackerTheme {
        DashboardScreen(
            uiModel = DashboardUiModel(
                period = TransactionPeriod.PAST_31_DAYS,
                currency = Currency.RON,
                currencyMode = CurrencyMode.SELECTED_ONLY,
                totalIncome = 267.0,
                totalCosts = 3985.0,
                totalBalance = -3718.0,
                costsBreakdown = listOf(
                    CategoryBreakdown("Rent", 3442.0, Color(0xFF42A5F5)),
                    CategoryBreakdown("Food", 543.0, Color(0xFF8B7E16)),
                ),
                incomeBreakdown = listOf(
                    CategoryBreakdown("Salary", 257.0, Color(0xFF42A5F5)),
                    CategoryBreakdown("Revenue", 10.0, Color(0xFF26A69A)),
                ),
                topIncome = listOf(
                    TransactionRow(1, "Salary", null, "15 Mar 2026", "1000.0", "EUR", true),
                    TransactionRow(2, "Salary", null, "22 Mar 2026", "257.0", "RON", true),
                ),
                topCosts = listOf(
                    TransactionRow(10, "Rent", null, "1 Mar 2026", "3442.0", "RON", false),
                    TransactionRow(11, "Food", null, "12 Mar 2026", "543.0", "RON", false),
                ),
                costsSlices = emptyList(),
                incomeSlices = emptyList(),
                openPickerId = null,
                isLoading = false,
                isRefreshing = false,
            ),
            onEvent = {},
        )
    }
}
