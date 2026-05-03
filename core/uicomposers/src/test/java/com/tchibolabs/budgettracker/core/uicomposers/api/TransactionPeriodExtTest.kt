package com.tchibolabs.budgettracker.core.uicomposers.api

import com.tchibolabs.budgettracker.core.data.api.model.TransactionPeriod
import com.tchibolabs.budgettracker.core.data.api.model.cutoffMs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TransactionPeriodExtTest {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val today: LocalDate = LocalDate.now(zone)

    @Test
    fun `ALL_TIME returns null`() {
        assertNull(TransactionPeriod.ALL_TIME.cutoffMs())
    }

    @Test
    fun `TODAY cutoff is start of today`() {
        val expected = today.atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, TransactionPeriod.TODAY.cutoffMs())
    }

    @Test
    fun `PAST_7_DAYS cutoff is 7 days ago start of day`() {
        val expected = today.minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, TransactionPeriod.PAST_7_DAYS.cutoffMs())
    }

    @Test
    fun `PAST_31_DAYS cutoff is 31 days ago start of day`() {
        val expected = today.minusDays(31).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, TransactionPeriod.PAST_31_DAYS.cutoffMs())
    }

    @Test
    fun `PAST_YEAR cutoff is one year ago start of day`() {
        val expected = today.minusYears(1).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, TransactionPeriod.PAST_YEAR.cutoffMs())
    }

    @Test
    fun `CURRENT_MONTH cutoff is first of month start of day`() {
        val expected = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, TransactionPeriod.CURRENT_MONTH.cutoffMs())
    }

    @Test
    fun `all non-ALL_TIME cutoffs are in the past`() {
        val now = System.currentTimeMillis()
        val nonAllTime = TransactionPeriod.entries.filter { it != TransactionPeriod.ALL_TIME }
        nonAllTime.forEach { period ->
            val cutoff = period.cutoffMs()
            assertNotNull(cutoff)
            assertTrue("${period.name} cutoff should be <= now", cutoff!! <= now)
        }
    }

    @Test
    fun `PAST_7_DAYS cutoff is strictly earlier than TODAY cutoff`() {
        val todayCutoff = TransactionPeriod.TODAY.cutoffMs()!!
        val past7Cutoff = TransactionPeriod.PAST_7_DAYS.cutoffMs()!!
        assertTrue(past7Cutoff < todayCutoff)
    }
}
