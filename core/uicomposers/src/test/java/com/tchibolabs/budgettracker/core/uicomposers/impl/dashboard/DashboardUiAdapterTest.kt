package com.tchibolabs.budgettracker.core.uicomposers.impl.dashboard

import app.cash.turbine.test
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.data.api.repository.ExchangeRateRepository
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.CurrencyMode
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.dashboard.DashboardUiModel
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListScope
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListSourceRow
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapter
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapterFactory
import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionRow
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardUiAdapterTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val repository = mockk<TransactionRepository> {
        every { observeAll() } returns transactionsFlow
    }
    private val rates = mockk<ExchangeRateRepository>(relaxed = true)
    private val factory = passthruFactory()

    private lateinit var adapter: DashboardUiAdapter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Default: same-currency passes through, foreign returns null (no rate), refresh fails
        coEvery { rates.convert(any(), any(), any()) } returns null
        coEvery { rates.convert(any<Double>(), "RON", "RON") } answers { firstArg() }
        coEvery { rates.refresh(any()) } returns Result.failure(RuntimeException("no network"))
        adapter = DashboardUiAdapter(repository, rates, factory)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── totals with SELECTED_ONLY ──────────────────────────────────────────

    @Test
    fun `SELECTED_ONLY sums income and expense for matching currency`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, kind = TransactionKind.Income, amount = 1000.0, currency = Currency.RON, occurredAtEpochMs = now),
            makeTransaction(id = 2, kind = TransactionKind.Expense, amount = 300.0, currency = Currency.RON, occurredAtEpochMs = now),
            makeTransaction(id = 3, kind = TransactionKind.Expense, amount = 500.0, currency = Currency.EUR, occurredAtEpochMs = now),
        )

        adapter.uiModel.test {
            val state = firstComputedState()
            // Default display currency is RON; EUR transaction excluded
            assertEquals(1000.0, state.totalIncome, 0.001)
            assertEquals(300.0, state.totalCosts, 0.001)
            assertEquals(700.0, state.totalBalance, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SELECTED_ONLY shows zero when no transactions match display currency`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, kind = TransactionKind.Expense, amount = 100.0, currency = Currency.USD, occurredAtEpochMs = now),
        )

        adapter.uiModel.test {
            val state = firstComputedState()
            assertEquals(0.0, state.totalCosts, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── ALL_CONVERTED mode ─────────────────────────────────────────────────

    @Test
    fun `ALL_CONVERTED sums transactions after conversion`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, kind = TransactionKind.Income, amount = 100.0, currency = Currency.RON, occurredAtEpochMs = now),
            makeTransaction(id = 2, kind = TransactionKind.Income, amount = 50.0, currency = Currency.EUR, occurredAtEpochMs = now),
        )
        // EUR → RON conversion rate: 5x
        coEvery { rates.convert(50.0, "EUR", "RON") } returns 250.0
        coEvery { rates.convert(any(), "EUR", "RON") } returns 5.0 * 50.0 // for ensureRates check

        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_CURRENCY_MODE, CurrencyMode.ALL_CONVERTED.name))
            val state = awaitItem()
            // 100 (RON) + 250 (50 EUR converted) = 350
            assertEquals(350.0, state.totalIncome, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ALL_CONVERTED emits conversionError when refresh fails`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, kind = TransactionKind.Expense, amount = 50.0, currency = Currency.USD, occurredAtEpochMs = now),
        )
        coEvery { rates.convert(any(), any(), any()) } returns null
        coEvery { rates.refresh(any()) } returns Result.failure(RuntimeException("offline"))

        // Activate the compute pipeline; conversionError is only emitted from within compute()
        val pipelineJob = launch { adapter.uiModel.collect { } }

        adapter.conversionError.test {
            adapter.onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_CURRENCY_MODE, CurrencyMode.ALL_CONVERTED.name))
            awaitItem()
        }

        pipelineJob.cancel()
    }

    // ── period filtering ───────────────────────────────────────────────────

    @Test
    fun `period filter excludes transactions outside the window`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, kind = TransactionKind.Expense, amount = 100.0, currency = Currency.RON, occurredAtEpochMs = now),
            makeTransaction(id = 2, kind = TransactionKind.Expense, amount = 200.0, currency = Currency.RON, occurredAtEpochMs = now - TimeUnit.DAYS.toMillis(8)),
        )

        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_PERIOD, TransactionPeriod.PAST_7_DAYS.name))
            val state = awaitItem()
            assertEquals(100.0, state.totalCosts, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ALL_TIME period includes all transactions`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, kind = TransactionKind.Expense, amount = 100.0, currency = Currency.RON, occurredAtEpochMs = now),
            makeTransaction(id = 2, kind = TransactionKind.Expense, amount = 200.0, currency = Currency.RON, occurredAtEpochMs = now - TimeUnit.DAYS.toMillis(400)),
        )

        adapter.uiModel.test {
            skipItems(1)
            adapter.onEvent(DashboardEvent.SelectOption(DashboardUiModel.PICKER_PERIOD, TransactionPeriod.ALL_TIME.name))
            val state = awaitItem()
            assertEquals(300.0, state.totalCosts, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── category breakdown ─────────────────────────────────────────────────

    @Test
    fun `costsBreakdown groups by category and sorts by total descending`() = runTest {
        val now = System.currentTimeMillis()
        transactionsFlow.value = listOf(
            makeTransaction(id = 1, kind = TransactionKind.Expense, amount = 100.0, currency = Currency.RON, category = "RENT", occurredAtEpochMs = now),
            makeTransaction(id = 2, kind = TransactionKind.Expense, amount = 50.0, currency = Currency.RON, category = "FOOD", occurredAtEpochMs = now),
            makeTransaction(id = 3, kind = TransactionKind.Expense, amount = 200.0, currency = Currency.RON, category = "RENT", occurredAtEpochMs = now),
        )

        adapter.uiModel.test {
            val state = firstComputedState()
            assertEquals(2, state.costsBreakdown.size)
            assertEquals("RENT", state.costsBreakdown[0].category)
            assertEquals(300.0, state.costsBreakdown[0].totalAmount, 0.001)
            assertEquals("FOOD", state.costsBreakdown[1].category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /**
     * Skips the Initial state and returns the first computed state after the
     * upstream has processed the current transactionsFlow value.
     */
    private suspend fun app.cash.turbine.TurbineTestContext<DashboardUiModel>.firstComputedState(): DashboardUiModel {
        val first = awaitItem()
        return if (first.isLoading) awaitItem() else first
    }

    private fun makeTransaction(
        id: Long,
        kind: TransactionKind = TransactionKind.Expense,
        amount: Double = 100.0,
        currency: Currency = Currency.RON,
        category: String = "OTHER",
        occurredAtEpochMs: Long = System.currentTimeMillis(),
    ) = Transaction(
        id = id,
        kind = kind,
        amount = amount,
        currency = currency,
        category = category,
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
