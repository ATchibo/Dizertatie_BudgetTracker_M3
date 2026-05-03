package com.tchibolabs.budgettracker.core.uicomposers.api

import java.util.Locale

fun Double.formatAmount(): String =
    if (this == this.toLong().toDouble()) "${this.toLong()}.0"
    else String.format(Locale.getDefault(), "%.2f", this)
