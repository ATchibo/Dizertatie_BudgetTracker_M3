package com.tchibolabs.budgettracker.core.uisystem.api

/**
 * Marker contract for stateless Composables that render a [UiModel] and emit
 * actions only through callbacks. A [UiComposer] MUST NOT receive a
 * [UiAdapter] / ViewModel directly — pass the [UiModel] and lambdas instead.
 *
 * Kotlin can't constrain top-level Composables, so this interface exists as
 * documentation: every screen/component should follow the shape
 * `fun XComposer(uiModel: M, onEvent: (Event) -> Unit)`.
 */
interface UiComposer
