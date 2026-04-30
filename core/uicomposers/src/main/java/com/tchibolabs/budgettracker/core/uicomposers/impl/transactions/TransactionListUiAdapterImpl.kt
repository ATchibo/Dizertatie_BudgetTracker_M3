package com.tchibolabs.budgettracker.core.uicomposers.impl.transactions

import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListScope
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListSourceRow
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapter
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapterFactory
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionRow
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.util.Locale
import javax.inject.Inject

class TransactionListUiAdapterProvider @Inject constructor(
    private val factory: TransactionListUiAdapterImpl.Factory,
) : TransactionListUiAdapterFactory {
    override fun create(scope: TransactionListScope): TransactionListUiAdapter = factory.create(scope)
}

class TransactionListUiAdapterImpl @AssistedInject constructor(
    @Assisted private val scope: TransactionListScope,
) : TransactionListUiAdapter {

    override fun composeRows(rows: List<TransactionListSourceRow>): List<TransactionRow> = rows.map { row ->
        TransactionRow(
            id = row.id,
            category = row.category,
            note = if (scope == TransactionListScope.DASHBOARD) null else row.note,
            dateLabel = row.dateLabel,
            amountText = row.amount.formatAmount(),
            currency = row.currency,
            isIncome = row.isIncome,
        )
    }

    private fun Double.formatAmount(): String =
        if (this == this.toLong().toDouble()) "${this.toLong()}.0" else String.format(Locale.getDefault(), "%.2f", this)

    @AssistedFactory
    interface Factory {
        fun create(scope: TransactionListScope): TransactionListUiAdapterImpl
    }
}
