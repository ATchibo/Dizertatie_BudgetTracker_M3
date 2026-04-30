package com.tchibolabs.budgettracker.core.uicomposers.impl.home

import com.tchibolabs.budgettracker.core.data.api.model.TransactionKind
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.uicomposers.api.home.HomeEvent
import com.tchibolabs.budgettracker.core.uicomposers.api.home.HomeUiModel
import com.tchibolabs.budgettracker.core.uisystem.api.UiAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            HomeEvent.Refresh -> {
                // Repository is already observed continuously from init.
            }
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
