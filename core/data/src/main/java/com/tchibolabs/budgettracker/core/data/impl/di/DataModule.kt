package com.tchibolabs.budgettracker.core.data.impl.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tchibolabs.budgettracker.core.data.api.repository.ExchangeRateRepository
import com.tchibolabs.budgettracker.core.data.api.repository.TransactionRepository
import com.tchibolabs.budgettracker.core.data.impl.db.BudgetDatabase
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

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS exchange_rates")
    }
}

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
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideTransactionDao(db: BudgetDatabase): TransactionDao = db.transactionDao()
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
