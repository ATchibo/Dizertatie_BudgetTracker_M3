package com.tchibolabs.budgettracker.feature.home.impl

import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HomeEvent {
    data object Refresh : HomeEvent
}

@HiltViewModel
class HomeUiAdapter @Inject constructor(
    private val transactions: TransactionRepository,
) : UiAdapter<HomeUiModel, HomeEvent>() {

    private val _uiModel = MutableStateFlow(HomeUiModel.Initial)
    override val uiModel: StateFlow<HomeUiModel> = _uiModel.asStateFlow()

    init {
        observeTransactions()
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> observeTransactions()
        }
    }

    private fun observeTransactions() {
        scope.launch {
            transactions.observeAll().collect { list ->
                val balance = list.sumOf {
                    if (it.kind == TransactionKind.Income) it.amount else -it.amount
                }
                _uiModel.update {
                    it.copy(
                        totalBalance = balance,
                        recentTransactionCount = list.size,
                        isLoading = false,
                    )
                }
            }
        }
    }
}
