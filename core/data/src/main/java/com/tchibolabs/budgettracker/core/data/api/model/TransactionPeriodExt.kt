package com.tchibolabs.budgettracker.core.data.api.model

import java.time.LocalDate
import java.time.ZoneId

fun TransactionPeriod.cutoffMs(): Long? {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    return when (this) {
        TransactionPeriod.TODAY -> today.atStartOfDay(zone).toInstant().toEpochMilli()
        TransactionPeriod.PAST_7_DAYS -> today.minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
        TransactionPeriod.PAST_31_DAYS -> today.minusDays(31).atStartOfDay(zone).toInstant().toEpochMilli()
        TransactionPeriod.PAST_YEAR -> today.minusYears(1).atStartOfDay(zone).toInstant().toEpochMilli()
        TransactionPeriod.CURRENT_MONTH -> today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        TransactionPeriod.ALL_TIME -> null
    }
}
