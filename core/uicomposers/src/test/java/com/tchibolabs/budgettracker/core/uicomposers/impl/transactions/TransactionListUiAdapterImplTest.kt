package com.tchibolabs.budgettracker.core.uicomposers.impl.transactions

import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListScope
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListSourceRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionListUiAdapterImplTest {

    private val sourceRow = TransactionListSourceRow(
        id = 1L,
        category = "Rent",
        note = "monthly payment",
        dateLabel = "1 Jan 2026",
        amount = 1500.0,
        currency = "RON",
        isIncome = false,
    )

    @Test
    fun `TRANSACTIONS scope preserves note`() {
        val adapter = TransactionListUiAdapterImpl(TransactionListScope.TRANSACTIONS)
        val rows = adapter.composeRows(listOf(sourceRow))
        assertEquals("monthly payment", rows[0].note)
    }

    @Test
    fun `DASHBOARD scope strips note`() {
        val adapter = TransactionListUiAdapterImpl(TransactionListScope.DASHBOARD)
        val rows = adapter.composeRows(listOf(sourceRow))
        assertNull(rows[0].note)
    }

    @Test
    fun `whole amount formats without trailing zeros`() {
        val adapter = TransactionListUiAdapterImpl(TransactionListScope.TRANSACTIONS)
        val rows = adapter.composeRows(listOf(sourceRow.copy(amount = 1500.0)))
        assertEquals("1500.0", rows[0].amountText)
    }

    @Test
    fun `decimal amount formats to two places`() {
        val adapter = TransactionListUiAdapterImpl(TransactionListScope.TRANSACTIONS)
        val rows = adapter.composeRows(listOf(sourceRow.copy(amount = 99.5)))
        assertEquals("99.50", rows[0].amountText)
    }

    @Test
    fun `all other fields pass through unchanged`() {
        val adapter = TransactionListUiAdapterImpl(TransactionListScope.TRANSACTIONS)
        val rows = adapter.composeRows(listOf(sourceRow))
        val row = rows[0]
        assertEquals(sourceRow.id, row.id)
        assertEquals(sourceRow.category, row.category)
        assertEquals(sourceRow.dateLabel, row.dateLabel)
        assertEquals(sourceRow.currency, row.currency)
        assertEquals(sourceRow.isIncome, row.isIncome)
    }
}
