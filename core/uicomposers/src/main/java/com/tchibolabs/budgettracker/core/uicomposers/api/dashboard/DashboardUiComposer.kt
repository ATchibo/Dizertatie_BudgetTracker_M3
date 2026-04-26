package com.tchibolabs.budgettracker.core.uicomposers.api.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.design.api.components.PieChart
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme
import com.tchibolabs.budgettracker.core.navigation.api.BudgetRoute

@Composable
fun DashboardUiComposer(
    uiModel: DashboardUiModel,
    modifier: Modifier = Modifier,
    onIntervalSelected: (DashboardEvent) -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigate: (BudgetRoute) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IntervalRow(
            selected = uiModel.interval,
            onSelect = { onIntervalSelected(DashboardEvent.IntervalSelected(it)) },
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PieChart(slices = uiModel.slices, modifier = Modifier.size(220.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiModel.breakdown, key = { it.category }) { row ->
                Text(
                    text = "${row.category} — %.2f".format(row.totalAmount),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun IntervalRow(
    selected: DashboardInterval,
    onSelect: (DashboardInterval) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Interval", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardInterval.values().forEach { interval ->
                FilterChip(
                    selected = interval == selected,
                    onClick = { onSelect(interval) },
                    label = { Text(interval.name) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun DashboardUiComposerPreview() {
    BudgetTrackerTheme {
        DashboardUiComposer(
            uiModel = DashboardUiModel(
                interval = DashboardInterval.Monthly,
                breakdown = listOf(
                    CategoryBreakdown("Food", 320.0, Color(0xFF66BB6A)),
                    CategoryBreakdown("Rent", 800.0, Color(0xFF42A5F5)),
                ),
                isLoading = false,
            ),
            onIntervalSelected = {},
            onNavigate = {},
        )
    }
}
