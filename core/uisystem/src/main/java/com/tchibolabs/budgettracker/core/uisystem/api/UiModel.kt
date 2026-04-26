package com.tchibolabs.budgettracker.core.uisystem.api

import androidx.compose.runtime.Immutable

/**
 * Marker for the strictly immutable UI state that flows out of a [UiAdapter]
 * into a [UiComposer]. Implementations MUST be data classes with stable fields.
 */
@Immutable
interface UiModel
