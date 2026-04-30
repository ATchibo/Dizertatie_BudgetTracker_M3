package com.tchibolabs.budgettracker.core.uicomposers.impl.di

import com.tchibolabs.budgettracker.core.uicomposers.api.transactions.TransactionListUiAdapterFactory
import com.tchibolabs.budgettracker.core.uicomposers.impl.transactions.TransactionListUiAdapterProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionListAdapterModule {
    @Binds
    @Singleton
    abstract fun bindTransactionListUiAdapterFactory(
        provider: TransactionListUiAdapterProvider,
    ): TransactionListUiAdapterFactory
}
