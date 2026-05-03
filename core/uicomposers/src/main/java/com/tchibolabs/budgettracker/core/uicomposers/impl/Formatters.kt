package com.tchibolabs.budgettracker.core.uicomposers.impl

import java.time.format.DateTimeFormatter
import java.util.Locale

internal val transactionDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
