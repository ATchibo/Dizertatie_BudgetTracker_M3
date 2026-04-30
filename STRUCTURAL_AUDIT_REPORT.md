# Structural Audit Report

## MVVM & State Management

- **Location:** `core/uicomposers/impl/home/HomeUiAdapter.kt`  
  **The Violation:** Violates single-source reactive collection (refresh triggers additional collectors).  
  **The Why:** Calling `observeTransactions()` on `Refresh` launches another long-lived `collect`, causing duplicate work and possible race updates.  
  **Refactor Suggestion:** Keep one collector for repository flow and model refresh as a state/input in that same pipeline.

- **Location:** `core/uicomposers/impl/dashboard/DashboardUiAdapter.kt`  
  **The Violation:** Reducer impurity (state derivation mutates source state).  
  **The Why:** `compute()`/`ensureRates()` writes to `currencyMode` during model computation, which can create re-entrant updates and hard-to-debug loops.  
  **Refactor Suggestion:** Make compute path pure and move `currencyMode` fallback to explicit event/reducer transitions.

- **Location:** `core/uicomposers/impl/dashboard/DashboardUiAdapter.kt`  
  **The Violation:** Refresh lifecycle handling not exception-safe.  
  **The Why:** `isRefreshing` is toggled manually without a guaranteed `finally`, so failure/cancellation can leave UI stuck refreshing.  
  **Refactor Suggestion:** Wrap refresh body with `try/finally` and always reset `isRefreshing`.

- **Location:** `core/uicomposers/impl/transactionsform/TransactionsFormUiAdapter.kt`  
  **The Violation:** Incomplete failure path for save flow.  
  **The Why:** `upsert()` errors are not surfaced and `isSaving` is reset only on success, risking stuck loading state.  
  **Refactor Suggestion:** Use `runCatching`/`try-finally`, emit error effect/state, always reset `isSaving`.

- **Location:** `core/uicomposers/impl/home`, `impl/transactions`, `impl/transactionsform`  
  **The Violation:** Inconsistent one-off effect handling.  
  **The Why:** Dashboard uses `SharedFlow` for transient errors, while other adapters mostly rely on persistent state flags or nothing.  
  **Refactor Suggestion:** Standardize transient `UiEffect` channels (`SharedFlow`) where one-shot UI effects are needed.

## Clean Architecture & SOLID

- **Location:** `feature/*/api/*EntryPoint.kt` (`home`, `dashboard`, `transactions`, `transactionsform`)  
  **The Violation:** API layer depends on implementation classes.  
  **The Why:** `feature ... api` imports `core.uicomposers.impl.*`, coupling high-level feature contracts to low-level details.  
  **Refactor Suggestion:** Expose only `core.uicomposers.api` contracts/factories and resolve impl via DI bindings.

- **Location:** `core/uicomposers/api/transactionsform/TransactionsFormUiModel.kt`  
  **The Violation:** Model/UI concern mixing (semantic model embeds Compose `Color`).  
  **The Why:** Category semantics become tied to rendering tech/theme, reducing portability and theming flexibility.  
  **Refactor Suggestion:** Keep category model semantic-only and map to color in UI/design mapping layer.

## Compose Best Practices

- **Location:** `core/uicomposers/impl/*`, `feature/dashboard/api/DashboardEntryPoint.kt`, `core/design/api/components/TransactionCard.kt`  
  **The Violation:** Hardcoded user-facing strings.  
  **The Why:** Breaks localization, consistency, and product copy governance.  
  **Refactor Suggestion:** Move all text to `strings.xml` and consume via `stringResource`.

- **Location:** `core/uicomposers/impl/transactions/TransactionListUiAdapterImpl.kt`, `feature/dashboard/api/DashboardEntryPoint.kt`  
  **The Violation:** Ad-hoc amount formatting in UI layer.  
  **The Why:** Locale/currency formatting inconsistencies are likely in finance UI.  
  **Refactor Suggestion:** Centralize money formatting in a shared formatter utility.

- **Location:** `feature/dashboard/api/DashboardEntryPoint.kt`  
  **The Violation:** Oversized screen file + unused input (`onNavigate`).  
  **The Why:** Large composable files are harder to maintain and unused API parameters drift contract clarity.  
  **Refactor Suggestion:** Split dashboard sections into dedicated files and remove or wire `onNavigate`.

## Package Topology

- **Location:** `core/uicomposers/**`  
  **The Violation:** Potential "gravity well" centralization of feature-specific presentation logic.  
  **The Why:** As logic grows, cross-feature coupling risk increases and ownership boundaries blur.  
  **Refactor Suggestion:** Keep only shared presentation primitives in `core`, and move feature-specific orchestration back behind stable feature boundaries or explicit core API modules per feature.

- **Location:** `feature/dashboard/api/DashboardEntryPoint.kt`  
  **The Violation:** API package holding heavy UI implementation details.  
  **The Why:** API surface gets polluted and package intent becomes unclear.  
  **Refactor Suggestion:** Keep API package thin (entry contracts), place implementation UI under impl-oriented package.

## Dependency Injection

- **Location:** `feature/*/api/*EntryPoint.kt` + `core/uicomposers/impl/*`  
  **The Violation:** DI composition leaks through package boundaries.  
  **The Why:** Hilt wiring works, but consumer modules import impl types directly, weakening encapsulation/scoping intent.  
  **Refactor Suggestion:** Introduce API-facing adapter/composer providers (interfaces) and bind impl in DI modules.

- **Location:** `core/uicomposers/impl/di/TransactionListAdapterModule.kt`  
  **The Violation:** Scope granularity likely too broad (`SingletonComponent`) for UI composition strategy.  
  **The Why:** Long-lived singletons for UI composition factories can be acceptable, but tighter scope improves future flexibility/test isolation.  
  **Refactor Suggestion:** Re-evaluate whether factory/provider should be singleton or viewmodel/component-scoped.

## Test Coverage Debt (Critical)

- **Location:** Repo-wide (`core/uicomposers/impl/*` especially)  
  **The Violation:** Missing unit tests for key reducer/adapter logic.  
  **The Why:** Finance-critical behavior (sorting/filtering/conversion/save flows) can regress silently.  
  **Refactor Suggestion:** Add adapter tests first for `DashboardUiAdapter`, `TransactionsUiAdapter`, `TransactionsFormUiAdapter`, and `HomeUiAdapter` event/state transitions.

## Positive Observations

- Strong UDF direction: adapters expose `StateFlow<UiModel>` + sealed events, and screens mostly stay stateless renderers.
- Good lifecycle handling in entry points with `collectAsStateWithLifecycle`.
- Reusable composition introduced: `TransactionListUiAdapter` + `TransactionListUiComposer` reduces duplicated row rendering logic.
- `LazyColumn` keys are provided for transaction rows (`key = { it.id }`), which helps list stability.
- Use of Hilt with constructor injection is clean and consistent across modules.
- Previews are present for major composables, helping UI iteration speed.

## Test Coverage Gaps

- No adapter reducer tests for event-to-state transitions (all four adapters).
- No tests for filter/order/cutoff correctness in `TransactionsUiAdapter`.
- No tests for conversion fallback behavior and refresh/error scenarios in `DashboardUiAdapter`.
- No tests for form input sanitization (`filterAmountInput`), save idempotency, and edit/new load behavior in `TransactionsFormUiAdapter`.
- No UI tests validating critical dialogs/bottom sheets and destructive action flow (delete confirm).
