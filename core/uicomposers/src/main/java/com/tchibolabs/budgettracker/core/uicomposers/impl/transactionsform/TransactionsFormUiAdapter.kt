package com.tchibolabs.budgettracker.core.uicomposers.impl.transactionsform

import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform.TransactionCategory
import com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform.TransactionsFormEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform.TransactionsFormUiModel
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionsFormUiAdapter @Inject constructor(
    private val repository: TransactionRepository,
) : UiAdapter<TransactionsFormUiModel, TransactionsFormEvent>() {

    private val _uiModel = MutableStateFlow(TransactionsFormUiModel.Initial)
    override val uiModel: StateFlow<TransactionsFormUiModel> = _uiModel.asStateFlow()

    private val _saveError = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saveError: SharedFlow<Unit> = _saveError

    fun load(id: Long?) {
        if (id == null) {
            _uiModel.value = TransactionsFormUiModel.Initial
            return
        }
        scope.launch {
            val existing = repository.findById(id) ?: return@launch
            _uiModel.value = TransactionsFormUiModel(
                id = existing.id,
                amountText = existing.amount.toString(),
                currency = existing.currency,
                category = existing.category.toCategory(),
                occurredAtEpochMs = existing.occurredAtEpochMs,
                description = existing.note.orEmpty(),
                isCurrencyPickerOpen = false,
                isCategoryPickerOpen = false,
                isDatePickerOpen = false,
                isSaving = false,
                saved = false,
            )
        }
    }

    override fun onEvent(event: TransactionsFormEvent) {
        when (event) {
            is TransactionsFormEvent.AmountChanged ->
                _uiModel.update { it.copy(amountText = event.text.filterAmountInput()) }
            is TransactionsFormEvent.CurrencySelected ->
                _uiModel.update {
                    it.copy(currency = event.currency, isCurrencyPickerOpen = false)
                }
            is TransactionsFormEvent.CategorySelected ->
                _uiModel.update {
                    it.copy(category = event.category, isCategoryPickerOpen = false)
                }
            is TransactionsFormEvent.DateSelected ->
                _uiModel.update {
                    it.copy(occurredAtEpochMs = event.epochMs, isDatePickerOpen = false)
                }
            is TransactionsFormEvent.DescriptionChanged ->
                _uiModel.update { it.copy(description = event.text) }
            TransactionsFormEvent.OpenCurrencyPicker ->
                _uiModel.update { it.copy(isCurrencyPickerOpen = true) }
            TransactionsFormEvent.DismissCurrencyPicker ->
                _uiModel.update { it.copy(isCurrencyPickerOpen = false) }
            TransactionsFormEvent.OpenCategoryPicker ->
                _uiModel.update { it.copy(isCategoryPickerOpen = true) }
            TransactionsFormEvent.DismissCategoryPicker ->
                _uiModel.update { it.copy(isCategoryPickerOpen = false) }
            TransactionsFormEvent.OpenDatePicker ->
                _uiModel.update { it.copy(isDatePickerOpen = true) }
            TransactionsFormEvent.DismissDatePicker ->
                _uiModel.update { it.copy(isDatePickerOpen = false) }
            TransactionsFormEvent.Save -> save()
        }
    }

    private fun save() {
        val current = _uiModel.value
        if (!current.isValid || current.isSaving) return
        _uiModel.update { it.copy(isSaving = true) }
        scope.launch {
            val savedSuccessfully = runCatching {
                val transaction = Transaction(
                    id = current.id ?: 0L,
                    kind = if (current.category.isIncome) TransactionKind.Income else TransactionKind.Expense,
                    amount = current.amountText.toDouble(),
                    currency = current.currency,
                    category = current.category.name,
                    note = current.description.takeIf { it.isNotBlank() },
                    occurredAtEpochMs = current.resolveOccurredAtEpochMs(),
                )
                repository.upsert(transaction)
            }.isSuccess
            if (!savedSuccessfully) _saveError.tryEmit(Unit)
            _uiModel.update { it.copy(isSaving = false, saved = savedSuccessfully) }
        }
    }

    private fun TransactionsFormUiModel.resolveOccurredAtEpochMs(): Long {
        if (id != null) return occurredAtEpochMs
        val zone = ZoneId.systemDefault()
        val pickedDate = Instant.ofEpochMilli(occurredAtEpochMs).atZone(zone).toLocalDate()
        val now = LocalDate.now(zone)
        val timeOfDay = if (pickedDate == now) LocalTime.now(zone) else LocalTime.MIDNIGHT
        return pickedDate.atTime(timeOfDay).atZone(zone).toInstant().toEpochMilli()
    }

    private fun String.toCategory(): TransactionCategory =
        runCatching { TransactionCategory.valueOf(this.uppercase()) }
            .getOrDefault(TransactionCategory.OTHER)

    private fun String.filterAmountInput(): String {
        val cleaned = filter { it.isDigit() || it == '.' }
        val firstDot = cleaned.indexOf('.')
        if (firstDot == -1) return cleaned
        return cleaned.substring(0, firstDot + 1) +
            cleaned.substring(firstDot + 1).replace(".", "")
    }
}
