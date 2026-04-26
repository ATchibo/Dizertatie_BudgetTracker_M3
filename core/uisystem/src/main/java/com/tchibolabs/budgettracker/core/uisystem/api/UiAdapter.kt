package com.tchibolabs.budgettracker.core.uisystem.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Base type for the data → UI translation layer. Owns Coroutines, holds raw
 * domain/business state, and exposes a [UiModel] [StateFlow] that a stateless
 * [UiComposer] renders. User actions arrive through [Event]s.
 */
abstract class UiAdapter<M : UiModel, Event> : ViewModel() {

    abstract val uiModel: StateFlow<M>

    protected val scope: CoroutineScope get() = viewModelScope

    abstract fun onEvent(event: Event)
}
