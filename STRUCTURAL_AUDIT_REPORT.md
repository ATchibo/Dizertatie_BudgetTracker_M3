# Structural Audit Report

_Last updated: 2026-05-03. Items marked ✅ were resolved in a previous session._

---

## 1. MVVM & State Management

### ✅ FIXED — DashboardUiAdapter: `isRefreshing` not exception-safe
`isRefreshing` toggle now wrapped in `try/finally` guaranteeing reset on failure or cancellation.

### ✅ FIXED — TransactionsFormUiAdapter: Incomplete failure path for save
`save()` now uses `runCatching`, always resets `isSaving`, and emits on `saveError: SharedFlow<Unit>` so the UI can react.

### ✅ FIXED — HomeUiAdapter: Refresh launched duplicate collectors
`HomeEvent.Refresh` is now a documented no-op; the single `observeAll()` collector started in `init` handles live updates.

---

### 🔴 DashboardUiAdapter: Impure reducer still mutates upstream state

**Location:** `core/uicomposers/src/.../impl/dashboard/DashboardUiAdapter.kt:127`

```kotlin
// Inside compute(), which runs inside mapLatest — a "pure" transformation:
if (effectiveMode != inputs.mode) {
    scope.launch { currencyMode.value = effectiveMode }  // ← side effect in reducer
}
```

`compute()` is wired as the body of a `mapLatest` transformer and must be a pure function (inputs → output). The `scope.launch` inside it fires a state mutation that will immediately re-trigger the pipeline, creating an implicit feedback loop that is hard to reason about and test.

**Fix:** Return the effective mode as part of the computed output and apply the state correction in a dedicated `onEach` or `flatMapLatest` stage after the pure compute call. `compute()` should return a pair `(UiModel, CurrencyMode?)` or a sealed result type.

---

### 🟡 DashboardUiAdapter: `ensureRates` mutates `currencyMode` from `onEvent`

**Location:** `core/uicomposers/src/.../impl/dashboard/DashboardUiAdapter.kt:104`

```kotlin
is DashboardEvent.SelectOption -> ...
    ?.let { currencyMode.value = it }
```

Not a pure-reducer violation on its own (events are the right place for mutations), but the interaction with the impure `compute()` above doubles the re-entry risk. Resolve together with the point above.

---

## 2. Compose Pattern Violations

### 🔴 Side effect (LaunchedEffect) inside a UiComposer

**Location:** `core/uicomposers/src/.../impl/transactionsform/TransactionsFormUiComposer.kt:59–61`

```kotlin
LaunchedEffect(uiModel.saved) {
    if (uiModel.saved) onSaved()
}
```

By the project's pattern, `UiComposer` is a **stateless renderer** — it takes a `UiModel` and emits callbacks. Side effects driven by state transitions belong in the **EntryPoint** layer (watching `SharedFlow`/`StateFlow` via `LaunchedEffect`). Putting navigation-triggering logic here couples the rendering layer to flow control.

**Fix:** Expose `saved` as a `SharedFlow<Unit>` on the adapter (mirror the `saveError` pattern already in place), collect it in `TransactionsFormEntryPoint`, and call `onNavigate` from there. Remove `onSaved` parameter and `LaunchedEffect` from the composer.

---

### 🟡 Hardcoded strings remaining in UI layer

The previous session moved most strings to `strings.xml`. The following are still hardcoded:

| File | Line(s) | Strings |
|------|---------|---------|
| `app/.../navigation/TabbedShell.kt` | 29, 34, 41 | `"Transactions"`, `"Dashboard"`, `"BudgetTracker"` |
| `core/design/.../BudgetTopAppBar.kt` | 40 | `"Back"` (contentDescription) |
| `core/uicomposers/api/.../TransactionsUiModel.kt` | 9–22 | `"Today"`, `"All time"`, `"Date (Oldest first)"`, etc. — period/order labels in extension properties |
| `core/uicomposers/api/.../DashboardUiModel.kt` | 20–25 | Same period labels for dashboard filter |
| `core/uicomposers/api/.../TransactionListUiComposer.kt` | 22 | `"No transactions found"` default parameter |

**Note on period/order labels:** These live in `api/` model files as `val TransactionPeriod.label: String` extension properties. Moving them to `strings.xml` requires access to a `Context`/`Resources`, which does not belong in a plain Kotlin model. The right fix is to move these display-name mappings into the `UiComposer` layer (e.g., a `@Composable fun TransactionPeriod.label() = stringResource(...)` extension), keeping the model itself context-free.

---

### 🟡 Missing `@Preview` annotations

| File | Composable |
|------|-----------|
| `app/.../navigation/TabbedShell.kt` | `TabbedShell` |
| `core/design/.../OptionsBottomSheet.kt` | `OptionsBottomSheet` |

Project rule: every public `@Composable` must have a `@Preview`.

---

## 3. Code Duplication

### 🟡 `cutoffMs()` implemented twice

**Locations:**
- `core/uicomposers/src/.../impl/dashboard/DashboardUiAdapter.kt:204`
- `core/uicomposers/src/.../impl/transactions/TransactionsUiAdapter.kt:130`

Identical private extension function `TransactionPeriod.cutoffMs(): Long?` exists in both adapters. Any future change to period semantics (e.g., adding `PAST_90_DAYS`) must be applied in two places.

**Fix:** Promote to an internal or public extension in `core/uicomposers/api/` (e.g., `TransactionPeriodExt.kt`).

---

## 4. Dead Code

### 🟡 `feature:home` module is completely unused

The module is declared in `settings.gradle.kts` but never imported by `:app` or any other module. `HomeEntryPoint`, `HomeUiAdapter`, and `HomeUiComposer` are never reachable.

**Impact:** Unnecessary compilation, Hilt component generation, and classpath bloat on every build.

**Fix:** Either wire the module into navigation (add `BudgetRoute.Home` entry to `BudgetTrackerNavHost`, add a Home tab) or delete the module and remove it from `settings.gradle.kts`.

---

### 🟡 `BudgetRoute.Home` is an orphan route

**Location:** `core/navigation/src/.../api/BudgetRoute.kt:10`

`BudgetRoute.Home` is declared in the sealed interface but has no matching `entry(...)` in `BudgetTrackerNavHost` and is never pushed onto the back stack. The fallback in `entryProvider` will throw `IllegalStateException` if it is ever reached.

**Fix:** Delete the variant together with the home feature, or wire it up.

---

## 5. Architecture & Package Topology

### ✅ FIXED — `TransactionCategory` embedded Compose `Color` in UiModel
`Color` removed; color is now derived from the design layer only.

### ✅ FIXED — `formatAmount` duplicated across layers
Centralized in `core/uicomposers/api/AmountFormatter.kt`.

### ✅ FIXED — `DashboardEntryPoint.onNavigate` unused parameter
Parameter removed; navigation handled entirely by the NavHost.

---

### 🟡 EntryPoints in `api/` import `impl/` types directly

**Location:** All `feature/*/api/*EntryPoint.kt` files

```kotlin
// e.g. DashboardEntryPoint.kt
adapter: DashboardUiAdapter = hiltViewModel()  // DashboardUiAdapter is in impl/
```

By project rules the `api/` package should not depend on `impl/`. The constraint is real: `hiltViewModel<T>()` requires the concrete ViewModel type. The pragmatic resolution is to accept this for `EntryPoint` files specifically (they are the DI wiring boundary) but document the exception explicitly, or introduce an abstract `UiAdapter` type per feature that the EntryPoint references.

---

### 🟡 Feature-specific presentation logic centralised in `core/uicomposers`

All four feature adapters and composers live in `core/uicomposers/{api,impl}` rather than inside their respective feature modules. As the project grows this creates cross-feature coupling risk: a change to dashboard logic requires touching a `core` module that all features depend on, triggering full recompilation.

**Consideration:** Keep only genuinely shared components (e.g., `TransactionListUiComposer`, `formatAmount`) in `core/uicomposers`. Move `DashboardUiAdapter`/`DashboardUiModel`/`DashboardUiComposer` into `feature/dashboard/impl`, and similarly for the others.

---

## 6. Dependency Injection

### ✅ FIXED — `TransactionListAdapterModule` scope too broad
Scope narrowed from `SingletonComponent` to `ActivityRetainedComponent`.

---

### 🟡 `lifecycle-viewmodel-navigation3` version was stale

**Location:** `gradle/libs.versions.toml`

`lifecycleViewmodelNav3` was pinned to `2.9.0-alpha09` while the locally cached artifact was `2.10.0`, causing a build failure. Fixed to `2.10.0` during the Nav3 migration.

---

## 7. Navigation

### ✅ FIXED — Hand-rolled state-machine navigation replaced with Nav3
`NavDisplay`, `rememberNavBackStack`, `entryProvider { }` DSL, and `NavKey` are now wired correctly. `BudgetRoute` implements `NavKey`. `TabbedShell` accepts a `content` slot rather than owning screen logic. `rememberSaveableStateHolderNavEntryDecorator` and `rememberViewModelStoreNavEntryDecorator` provide correct Compose state scoping and ViewModel scoping per back-stack entry.

---

## 8. Input Validation

### 🟢 `filterAmountInput` + `isValid` are safe, but UX is rough

**Location:** `core/uicomposers/src/.../impl/transactionsform/TransactionsFormUiAdapter.kt:127–133`

`filterAmountInput()` permits a trailing lone `"."` in the text field (e.g., the user types `"5."`). `isValid` uses `toDoubleOrNull()` which returns `null` for `"."`, so the Save button stays disabled — no crash. However the user sees an unresolved decimal point with no feedback. A minor UX improvement would be to strip a trailing `"."` before display or show a validation message.

---

## 9. Test Coverage (Critical Gap)

**Location:** Repo-wide — zero unit test files exist.

Finance-critical logic that has no automated coverage:

| Adapter | Untested Logic |
|---------|----------------|
| `DashboardUiAdapter` | Period cutoff filtering, currency conversion fallback, `compute()` purity, refresh error propagation |
| `TransactionsUiAdapter` | Period/order filtering, sort correctness, cutoff boundary conditions |
| `TransactionsFormUiAdapter` | `filterAmountInput`, `isValid`, save idempotency, load-existing-transaction flow |
| `HomeUiAdapter` | Balance calculation, `observeAll` reactive pipeline |

Also missing: tests for data mappers, `cutoffMs()` boundary cases, `formatAmount()` edge cases (zero, negative, large values).

**Recommended starting point:** `TransactionsUiAdapter` period filtering and `DashboardUiAdapter` currency conversion — both are pure-ish functions that are straightforward to test with fakes.

---

## 10. Positive Observations

- **UDF direction is strong:** adapters expose `StateFlow<UiModel>` + sealed events; screens are stateless renderers.
- **Nav3 fully wired:** correct use of `entryProvider { }` DSL, per-entry ViewModel and saveable state scoping.
- **Lifecycle awareness:** `collectAsStateWithLifecycle` used throughout entry points.
- **`TransactionListUiComposer` reuse:** eliminates duplicated row rendering between dashboard and transactions screens.
- **Hilt wiring:** constructor injection is clean and consistent; no manual `ViewModelProvider` calls.
- **`LazyColumn` keyed:** `key = { it.id }` on transaction rows ensures correct list diffing.
- **Error effects standardised:** both `DashboardUiAdapter.conversionError` and `TransactionsFormUiAdapter.saveError` use `SharedFlow` for one-shot UI effects.

---

## Priority Order for Next Session

| # | Issue | Effort |
|---|-------|--------|
| 1 | Delete or wire `feature:home` + `BudgetRoute.Home` | Low |
| 2 | Fix impure reducer in `DashboardUiAdapter.compute()` | Medium |
| 3 | Move `LaunchedEffect(saved)` from `TransactionsFormUiComposer` to EntryPoint | Low |
| 4 | Extract shared `cutoffMs()` to `core/uicomposers/api/` | Low |
| 5 | Remaining hardcoded strings (TabbedShell, BudgetTopAppBar, period labels) | Medium |
| 6 | Add missing `@Preview` for TabbedShell and OptionsBottomSheet | Low |
| 7 | Add unit tests for UiAdapter logic | High effort, highest value |
