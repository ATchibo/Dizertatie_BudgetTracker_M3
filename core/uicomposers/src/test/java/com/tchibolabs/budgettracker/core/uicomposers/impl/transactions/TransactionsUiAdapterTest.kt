package com.tchibolabs.budgettracker.core.uicomposers.impl.transactions

import android.content.Context
import app.cash.turbine.test
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.model.TransactionOrder
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListScope
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListSourceRow
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapter
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapterFactory
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionRow
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsFilter
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionsUiModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsUiAdapterTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val repository = mockk<TransactionRepository>(relaxed = true) {
        every { observeAll() } returns transactionsFlow
    }
    private val context = mockk<Context> {
        every { getString(any()) } returns ""
    }
    private val factory = passthruFactory()

    private lateinit var adapter: TransactionsUiAdapter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        adapter = TransactionsUiAdapter(repository, context, factory)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── period filtering ───────────────────────────────────────────────────

    @Test
    fun `PAST_7_DAYS includes today but excludes 8 days ago`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, occurredAtEpochMs = now),
            makeTransaction(id = 2, occurredAtEpochMs = now - TimeUnit.DAYS.toMillis(8)),
        )

        adapter.uiModel.test {
            skipItems(1) // initial
            adapter.onEvent(TransactionsEvent.SelectOption(TransactionsFilter.ID_PERIOD, TransactionPeriod.PAST_7_DAYS.name))
            val state = awaitItem()
            assertEquals(1, state.rows.size)
            assertEquals(1L, state.rows[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ALL_TIME includes all transactions`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, occurredAtEpochMs = now),
            makeTransaction(id = 2, occurredAtEpochMs = now - TimeUnit.DAYS.toMillis(400)),
            makeTransaction(id = 3, occurredAtEpochMs = now - TimeUnit.DAYS.toMillis(800)),
        )

        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(TransactionsEvent.SelectOption(TransactionsFilter.ID_PERIOD, TransactionPeriod.ALL_TIME.name))
            val state = awaitItem()
            assertEquals(3, state.rows.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `PAST_31_DAYS excludes 35 days ago but includes 30 days ago`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, occurredAtEpochMs = now - TimeUnit.DAYS.toMillis(30)),
            makeTransaction(id = 2, occurredAtEpochMs = now - TimeUnit.DAYS.toMillis(35)),
        )

        adapter.uiModel.test {
            val state = firstComputedState()
            assertEquals(1, state.rows.size)
            assertEquals(1L, state.rows[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── sort orders ────────────────────────────────────────────────────────

    @Test
    fun `AMOUNT_DESC orders highest amount first`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, amount = 50.0, occurredAtEpochMs = now),
            makeTransaction(id = 2, amount = 200.0, occurredAtEpochMs = now),
            makeTransaction(id = 3, amount = 10.0, occurredAtEpochMs = now),
        )

        adapter.uiModel.test {
            val state = firstComputedState()
            assertEquals(listOf(2L, 1L, 3L), state.rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `AMOUNT_ASC orders lowest amount first`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, amount = 50.0, occurredAtEpochMs = now),
            makeTransaction(id = 2, amount = 200.0, occurredAtEpochMs = now),
            makeTransaction(id = 3, amount = 10.0, occurredAtEpochMs = now),
        )

        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(TransactionsEvent.SelectOption(TransactionsFilter.ID_ORDER, TransactionOrder.AMOUNT_ASC.name))
            val state = awaitItem()
            assertEquals(listOf(3L, 1L, 2L), state.rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DATE_DESC orders most recent first`() = runTest {
        val base = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, occurredAtEpochMs = base - 1000),
            makeTransaction(id = 2, occurredAtEpochMs = base - 3000),
            makeTransaction(id = 3, occurredAtEpochMs = base - 500),
        )

        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(TransactionsEvent.SelectOption(TransactionsFilter.ID_ORDER, TransactionOrder.DATE_DESC.name))
            val state = awaitItem()
            assertEquals(listOf(3L, 1L, 2L), state.rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DATE_ASC orders oldest first`() = runTest {
        val base = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, occurredAtEpochMs = base - 1000),
            makeTransaction(id = 2, occurredAtEpochMs = base - 3000),
            makeTransaction(id = 3, occurredAtEpochMs = base - 500),
        )

        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(TransactionsEvent.SelectOption(TransactionsFilter.ID_ORDER, TransactionOrder.DATE_ASC.name))
            val state = awaitItem()
            assertEquals(listOf(2L, 1L, 3L), state.rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── delete flow ────────────────────────────────────────────────────────

    @Test
    fun `RequestDelete sets pendingDeleteId`() = runTest {
        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(TransactionsEvent.RequestDelete(42L))
            val state = awaitItem()
            assertEquals(42L, state.pendingDeleteId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `CancelDelete clears pendingDeleteId`() = runTest {
        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(TransactionsEvent.RequestDelete(42L))
            skipItems(1)
            adapter.onEvent(TransactionsEvent.CancelDelete)
            val state = awaitItem()
            assertNull(state.pendingDeleteId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ConfirmDelete calls repository delete and clears pendingDeleteId`() = runTest {
        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(TransactionsEvent.RequestDelete(42L))
            skipItems(1)
            adapter.onEvent(TransactionsEvent.ConfirmDelete)
            val state = awaitItem()
            assertNull(state.pendingDeleteId)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { repository.delete(42L) }
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private suspend fun app.cash.turbine.TurbineTestContext<TransactionsUiModel>.firstComputedState(): TransactionsUiModel {
        val first = awaitItem()
        return if (first.isLoading) awaitItem() else first
    }

    private fun makeTransaction(
        id: Long,
        amount: Double = 100.0,
        occurredAtEpochMs: Long = System.currentTimeMillis(),
    ) = Transaction(
        id = id,
        kind = TransactionKind.Expense,
        amount = amount,
        currency = Currency.RON,
        category = "OTHER",
        note = null,
        occurredAtEpochMs = occurredAtEpochMs,
    )

    private fun passthruFactory(): TransactionListUiAdapterFactory =
        object : TransactionListUiAdapterFactory {
            override fun create(scope: TransactionListScope): TransactionListUiAdapter =
                object : TransactionListUiAdapter {
                    override fun composeRows(rows: List<TransactionListSourceRow>): List<TransactionRow> =
                        rows.map { row ->
                            TransactionRow(
                                id = row.id,
                                category = row.category,
                                note = row.note,
                                dateLabel = row.dateLabel,
                                amountText = row.amount.toString(),
                                currency = row.currency,
                                isIncome = row.isIncome,
                            )
                        }
                }
        }
}
