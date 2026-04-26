package com.tchibolabs.budgettracker.core.uicomposers.api.dashboard

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.design.api.components.FilterChipCard
import com.tchibolabs.budgettracker.core.design.api.components.OptionsBottomSheet
import com.tchibolabs.budgettracker.core.design.api.components.PickerOption
import com.tchibolabs.budgettracker.core.design.api.components.PieChart
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute

@Composable
fun DashboardUiComposer(
    uiModel: DashboardUiModel,
    modifier: Modifier = Modifier,
    onEvent: (DashboardEvent) -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigate: (BudgetRoute) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                        label = "Time Period",
                        value = uiModel.period.dashboardLabel,
                        onClick = { onEvent(DashboardEvent.OpenPicker(DashboardUiModel.PICKER_PERIOD)) },
                        modifier = Modifier.weight(1f),
                    )
                    FilterChipCard(
                        label = "Currency",
                        value = uiModel.currency.name,
                        onClick = { onEvent(DashboardEvent.OpenPicker(DashboardUiModel.PICKER_CURRENCY)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                FilterChipCard(
                    label = "Currency Mode",
                    value = uiModel.currencyMode.label,
                    onClick = { onEvent(DashboardEvent.OpenPicker(DashboardUiModel.PICKER_CURRENCY_MODE)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                TotalsCard(label = "Total Income", amount = uiModel.totalIncome, currency = uiModel.displayCurrency)
            }
            item {
                TotalsCard(label = "Total Costs", amount = uiModel.totalCosts, currency = uiModel.displayCurrency)
            }
            item {
                TotalsCard(label = "Total Balance", amount = uiModel.totalBalance, currency = uiModel.displayCurrency)
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                CategorySection(title = "Costs by category") {
                    CostsDonut(uiModel = uiModel)
                    BreakdownLegend(breakdown = uiModel.costsBreakdown)
                }
            }
            if (uiModel.topCosts.isNotEmpty()) {
                item {
                    Text(
                        text = "Top 5 Costs",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                items(uiModel.topCosts, key = { "cost-${it.id}" }) { row ->
                    DashboardTransactionRowCard(row = row)
                }
            }
            item {
                CategorySection(title = "Income by category") {
                    IncomePie(uiModel = uiModel)
                }
            }
            if (uiModel.topIncome.isNotEmpty()) {
                item {
                    Text(
                        text = "Top 5 Income",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                items(uiModel.topIncome, key = { "income-${it.id}" }) { row ->
                    DashboardTransactionRowCard(row = row)
                }
            }
        }

        when (uiModel.openPickerId) {
            DashboardUiModel.PICKER_PERIOD -> OptionsBottomSheet(
                title = "Time Period",
                options = TransactionPeriod.values().map { PickerOption(it.name, it.dashboardLabel) },
                selectedOptionId = uiModel.period.name,
                onSelect = { onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_PERIOD, it.id)) },
                onDismiss = { onEvent(DashboardEvent.ClosePicker(DashboardUiModel.PICKER_PERIOD)) },
            )
            DashboardUiModel.PICKER_CURRENCY -> OptionsBottomSheet(
                title = "Currency",
                options = Currency.values().map { PickerOption(it.name, it.name) },
                selectedOptionId = uiModel.currency.name,
                onSelect = { onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_CURRENCY, it.id)) },
                onDismiss = { onEvent(DashboardEvent.ClosePicker(DashboardUiModel.PICKER_CURRENCY)) },
            )
            DashboardUiModel.PICKER_CURRENCY_MODE -> OptionsBottomSheet(
                title = "Currency Mode",
                options = CurrencyMode.values().map { PickerOption(it.name, it.label) },
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
            strokeWidth = 56.dp,
        ) {
            uiModel.highlightedCost?.let { highlight ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = highlight.category.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = highlight.totalAmount.formatAmount(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "%.1f%%".format(highlight.share * 100f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
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
private fun DashboardTransactionRowCard(
    row: DashboardTransactionRow,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = row.category.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = row.dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${row.amountText} ${row.currency}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun Double.formatAmount(): String =
    if (this == this.toLong().toDouble()) "${this.toLong()}.0" else "%.2f".format(this)

@Preview
@Composable
private fun DashboardUiComposerPreview() {
    BudgetTrackerTheme {
        DashboardUiComposer(
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
                    DashboardTransactionRow(1, "Salary", "15 Mar 2026", "1000.0", "EUR"),
                    DashboardTransactionRow(2, "Salary", "22 Mar 2026", "257.0", "RON"),
                    DashboardTransactionRow(3, "Revenue", "15 Mar 2026", "10.0", "RON"),
                ),
                topCosts = listOf(
                    DashboardTransactionRow(10, "Rent", "1 Mar 2026", "3442.0", "RON"),
                    DashboardTransactionRow(11, "Food", "12 Mar 2026", "543.0", "RON"),
                ),
                openPickerId = null,
                isLoading = false,
            ),
            onEvent = {},
            onNavigate = {},
        )
    }
}
