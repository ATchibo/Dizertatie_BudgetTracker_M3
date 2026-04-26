package com.tchibolabs.budgettracker.core.design.api.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.tchibolabs.budgettracker.core.design.api.theme.BudgetTrackerTheme

@Immutable
data class BottomBarItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BudgetBottomBar(
    items: List<BottomBarItem>,
    selectedKey: String,
    onSelect: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.key == selectedKey,
                onClick = { onSelect(item) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun BudgetBottomBarPreview() {
    BudgetTrackerTheme {
        BudgetBottomBar(
            items = listOf(
                BottomBarItem("transactions", "Transactions", Icons.AutoMirrored.Filled.List),
                BottomBarItem("dashboard", "Dashboard", Icons.Filled.GridView),
            ),
            selectedKey = "transactions",
            onSelect = {},
        )
    }
}
