package com.tchibolabs.budgettracker.core.uicomposers.impl.di

import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapterFactory
import com.tchibolabs.budgettracker.core.uicomposers.impl.transactions.TransactionListUiAdapterProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class TransactionListAdapterModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindTransactionListUiAdapterFactory(
        provider: TransactionListUiAdapterProvider,
    ): TransactionListUiAdapterFactory
}
