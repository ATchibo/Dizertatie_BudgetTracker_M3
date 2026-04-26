package com.tchibolabs.budgettracker.core.data.impl.di

import android.content.Context
import androidx.room.Room
import com.tchibolabs.budgettracker.core.data.api.repository.ExchangeRateRepository
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.data.impl.db.BudgetDatabase
import com.tchibolabs.budgettracker.core.data.impl.db.ExchangeRateDao
import com.tchibolabs.budgettracker.core.data.impl.db.TransactionDao
import com.tchibolabs.budgettracker.core.data.impl.repository.ExchangeRateRepositoryImpl
import com.tchibolabs.budgettracker.core.data.impl.repository.TransactionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
internal object DataProvidersModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BudgetDatabase =
        Room.databaseBuilder(
            context,
            BudgetDatabase::class.java,
            "budget_tracker.db",
        ).build()

    @Provides
    fun provideTransactionDao(db: BudgetDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideExchangeRateDao(db: BudgetDatabase): ExchangeRateDao = db.exchangeRateDao()

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl,
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindExchangeRateRepository(
        impl: ExchangeRateRepositoryImpl,
    ): ExchangeRateRepository
}
