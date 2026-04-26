package com.tchibolabs.budgettracker.core.design.api.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme

data class CategoryOption(
    val id: String,
    val label: String,
)

@Composable
fun CategoryPicker(
    options: List<CategoryOption>,
    selectedId: String?,
    onSelected: (CategoryOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options, key = { it.id }) { option ->
            val isSelected = option.id == selectedId
            AssistChip(
                onClick = { onSelected(option) },
                label = { Text(option.label) },
                colors = if (isSelected) {
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    AssistChipDefaults.assistChipColors()
                },
            )
        }
    }
}

@Preview
@Composable
private fun CategoryPickerPreview() {
    BudgetTrackerTheme {
        CategoryPicker(
            options = listOf(
                CategoryOption("food", "Food"),
                CategoryOption("rent", "Rent"),
                CategoryOption("travel", "Travel"),
            ),
            selectedId = "rent",
            onSelected = {},
        )
    }
}
