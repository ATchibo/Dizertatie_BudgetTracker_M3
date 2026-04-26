package com.tchibolabs.budgettracker.feature.transactionsform.impl

import com.tchibolabs.budgettracker.core.data.api.model.Transaction
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform.TransactionsFormEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.transactionsform.TransactionsFormUiModel
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
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

    fun load(id: Long?) {
        if (id == null) {
            _uiModel.value = TransactionsFormUiModel.Initial
            return
        }
        scope.launch {
            val existing = repository.findById(id) ?: return@launch
            _uiModel.value = TransactionsFormUiModel(
                id = existing.id,
                kind = existing.kind,
                amountText = existing.amount.toString(),
                currency = existing.currency,
                category = existing.category,
                note = existing.note.orEmpty(),
                isSaving = false,
                saved = false,
            )
        }
    }

    override fun onEvent(event: TransactionsFormEvent) {
        when (event) {
            is TransactionsFormEvent.KindChanged ->
                _uiModel.update { it.copy(kind = event.kind) }
            is TransactionsFormEvent.AmountChanged ->
                _uiModel.update { it.copy(amountText = event.text) }
            is TransactionsFormEvent.CurrencyChanged ->
                _uiModel.update { it.copy(currency = event.currency) }
            is TransactionsFormEvent.CategoryChanged ->
                _uiModel.update { it.copy(category = event.category) }
            is TransactionsFormEvent.NoteChanged ->
                _uiModel.update { it.copy(note = event.note) }
            TransactionsFormEvent.Save -> save()
        }
    }

    private fun save() {
        val current = _uiModel.value
        if (!current.isValid || current.isSaving) return
        _uiModel.update { it.copy(isSaving = true) }
        scope.launch {
            val transaction = Transaction(
                id = current.id ?: 0L,
                kind = current.kind,
                amount = current.amountText.toDouble(),
                currency = current.currency,
                category = current.category,
                note = current.note.takeIf { it.isNotBlank() },
                occurredAtEpochMs = System.currentTimeMillis(),
            )
            repository.upsert(transaction)
            _uiModel.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
