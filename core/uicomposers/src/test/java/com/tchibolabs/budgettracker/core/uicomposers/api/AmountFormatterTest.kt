package com.tchibolabs.budgettracker.core.uicomposers.api

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountFormatterTest {

    @Test
    fun `whole number formats with single decimal place`() {
        assertEquals("100.0", 100.0.formatAmount())
        assertEquals("0.0", 0.0.formatAmount())
        assertEquals("1.0", 1.0.formatAmount())
        assertEquals("10000.0", 10_000.0.formatAmount())
    }

    @Test
    fun `decimal value formats to two places`() {
        assertEquals("1.50", 1.5.formatAmount())
        assertEquals("3.14", 3.14.formatAmount())
        assertEquals("9.99", 9.99.formatAmount())
    }

    @Test
    fun `negative whole number formats with single decimal place`() {
        assertEquals("-50.0", (-50.0).formatAmount())
    }

    @Test
    fun `negative decimal formats to two places`() {
        assertEquals("-1.50", (-1.5).formatAmount())
    }
}
