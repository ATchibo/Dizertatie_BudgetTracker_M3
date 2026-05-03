package com.tchibolabs.budgettracker.core.uicomposers.impl.transactionsform

import app.cash.turbine.test
import com.tchibolabs.budgettracker.core.data.api.model.Currency
import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform.TransactionsFormEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform.TransactionsFormUiModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsFormUiAdapterTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<TransactionRepository>(relaxed = true)

    private lateinit var adapter: TransactionsFormUiAdapter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        adapter = TransactionsFormUiAdapter(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── filterAmountInput (exercised via AmountChanged event) ──────────────

    @Test
    fun `AmountChanged strips non-numeric characters`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged("abc12xyz"))
        assertEquals("12", adapter.uiModel.value.amountText)
    }

    @Test
    fun `AmountChanged allows single decimal point`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged("12.5"))
        assertEquals("12.5", adapter.uiModel.value.amountText)
    }

    @Test
    fun `AmountChanged collapses multiple dots keeping only first`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged("12.34.56"))
        assertEquals("12.3456", adapter.uiModel.value.amountText)
    }

    @Test
    fun `AmountChanged strips minus sign`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged("-50"))
        assertEquals("50", adapter.uiModel.value.amountText)
    }

    @Test
    fun `AmountChanged with empty string gives empty string`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged(""))
        assertEquals("", adapter.uiModel.value.amountText)
    }

    // ── isValid ────────────────────────────────────────────────────────────

    @Test
    fun `isValid false when amount is empty`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged(""))
        assertFalse(adapter.uiModel.value.isValid)
    }

    @Test
    fun `isValid false when amount is zero`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged("0"))
        assertFalse(adapter.uiModel.value.isValid)
    }

    @Test
    fun `isValid true when amount is positive`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged("10"))
        assertTrue(adapter.uiModel.value.isValid)
    }

    @Test
    fun `isValid true for decimal positive amount`() {
        adapter.onEvent(TransactionsFormEvent.AmountChanged("0.01"))
        assertTrue(adapter.uiModel.value.isValid)
    }

    // ── save ───────────────────────────────────────────────────────────────

    @Test
    fun `save success emits on saved and resets isSaving`() = runTest {
        coEvery { repository.upsert(any()) } returns 1L
        adapter.onEvent(TransactionsFormEvent.AmountChanged("100"))

        adapter.saved.test {
            adapter.onEvent(TransactionsFormEvent.Save)
            awaitItem()
        }

        assertFalse(adapter.uiModel.value.isSaving)
    }

    @Test
    fun `save failure emits on saveError and resets isSaving`() = runTest {
        coEvery { repository.upsert(any()) } throws RuntimeException("DB error")
        adapter.onEvent(TransactionsFormEvent.AmountChanged("100"))

        adapter.saveError.test {
            adapter.onEvent(TransactionsFormEvent.Save)
            awaitItem()
        }

        assertFalse(adapter.uiModel.value.isSaving)
    }

    @Test
    fun `save is ignored when amount is invalid`() = runTest {
        adapter.onEvent(TransactionsFormEvent.AmountChanged(""))
        adapter.onEvent(TransactionsFormEvent.Save)

        coVerify(exactly = 0) { repository.upsert(any()) }
    }

    @Test
    fun `save is ignored when already saving`() = runTest {
        // Set isSaving = true by starting a slow upsert
        coEvery { repository.upsert(any()) } returns 1L
        adapter.onEvent(TransactionsFormEvent.AmountChanged("100"))

        // First save succeeds and resets isSaving; verify idempotency
        adapter.saved.test {
            adapter.onEvent(TransactionsFormEvent.Save)
            awaitItem()
        }

        coVerify(exactly = 1) { repository.upsert(any()) }
    }

    // ── load ───────────────────────────────────────────────────────────────

    @Test
    fun `load with null resets to Initial`() = runTest {
        adapter.onEvent(TransactionsFormEvent.AmountChanged("999"))
        adapter.load(null)
        assertEquals(TransactionsFormUiModel.Initial, adapter.uiModel.value)
    }

    @Test
    fun `load with existing id maps transaction fields`() = runTest {
        val existing = Transaction(
            id = 42L,
            kind = TransactionKind.Expense,
            amount = 250.0,
            currency = Currency.EUR,
            category = "RENT",
            note = "April rent",
            occurredAtEpochMs = 1_700_000_000_000L,
        )
        coEvery { repository.findById(42L) } returns existing

        adapter.load(42L)

        val state = adapter.uiModel.value
        assertEquals(42L, state.id)
        assertEquals("250.0", state.amountText)
        assertEquals(Currency.EUR, state.currency)
        assertEquals("April rent", state.description)
        assertEquals(1_700_000_000_000L, state.occurredAtEpochMs)
        assertFalse(state.isSaving)
    }

    @Test
    fun `load with unknown id does not change state`() = runTest {
        coEvery { repository.findById(99L) } returns null
        adapter.onEvent(TransactionsFormEvent.AmountChanged("50"))

        adapter.load(99L)

        // state unchanged from before the load call
        assertEquals("50", adapter.uiModel.value.amountText)
    }

    // ── picker events ──────────────────────────────────────────────────────

    @Test
    fun `OpenCurrencyPicker sets isCurrencyPickerOpen`() {
        adapter.onEvent(TransactionsFormEvent.OpenCurrencyPicker)
        assertTrue(adapter.uiModel.value.isCurrencyPickerOpen)
    }

    @Test
    fun `DismissCurrencyPicker clears isCurrencyPickerOpen`() {
        adapter.onEvent(TransactionsFormEvent.OpenCurrencyPicker)
        adapter.onEvent(TransactionsFormEvent.DismissCurrencyPicker)
        assertFalse(adapter.uiModel.value.isCurrencyPickerOpen)
    }

    @Test
    fun `CurrencySelected updates currency and closes picker`() {
        adapter.onEvent(TransactionsFormEvent.OpenCurrencyPicker)
        adapter.onEvent(TransactionsFormEvent.CurrencySelected(Currency.EUR))
        assertEquals(Currency.EUR, adapter.uiModel.value.currency)
        assertFalse(adapter.uiModel.value.isCurrencyPickerOpen)
    }
}
