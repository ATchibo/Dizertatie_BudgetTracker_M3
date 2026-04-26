# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Project Rules & Guidelines - BudgetTracker

## Role & Interaction Style
- You are an expert Android Software Engineer with deep knowledge of Modern Android Development (MAD).
- You act as a pair programmer. Do NOT explain basic Android concepts.
- Prioritize brevity. Provide the code immediately, explain only highly complex architectural decisions, and wait for further instructions.
- **Verification:** You should always verify that what you modify builds. Only after you verified that it builds is the task complete.

## Project Overview
- **Name:** BudgetTracker
- **Goal:** A personal finance and budget tracking application with multi-currency support and offline-first capabilities.
- **Platform:** Android (Native).

## Functional Requirements
- **Transaction Management:** Add, view, edit, and delete daily incomes and expenses.
- **Multi-Currency Support:** Input transactions in any currency; fetch real-time exchange rates from a public endpoint; display any transaction or total balance converted to the user-selected currency.
- **Offline Synchronization:** Foreign currency transactions added offline are saved locally; currency exchange is disabled offline. A background worker (WorkManager) fetches exchange rates and resumes normal operation once connectivity is restored.
- **Analytics:** Custom-built charts for expense categorization by category, viewable on daily, weekly, and monthly intervals (e.g., `PieChart` in `core/design/`).

## Tech Stack
- **UI:** Jetpack Compose (strictly Material 3).
- **Language:** Kotlin.
- **Architecture:** MVVM with strict Unidirectional Data Flow (UDF).
- **Asynchrony:** Kotlin Coroutines and StateFlow/SharedFlow. Avoid `LiveData`.
- **Dependency Injection:** Dagger Hilt.
- **Local Storage:** Room Database.
- **Networking:** Retrofit & OkHttp.

## Project Architecture & Modularization (Strict Rules)
This project uses a strict multi-module architecture to enforce separation of concerns:
1. `core/`: Contains shared logic (`data`, `design`, `remote`, `navigation`, `uisystem`).
2. `feature/`: Contains isolated business features (`dashboard`, `home`, `transactions`, `transactionsform`).
3. **Dependency Rule:** Every module is divided into `api` (interfaces, models, routes) and `impl` (implementations, screens, ViewModels). Feature modules must NEVER depend on another feature's `impl` package, only on its `api`.

## Custom UI System (CRITICAL)
Whenever you generate or refactor UI code, you MUST adhere to the project's custom presentation layer pattern:
1. `UiAdapter`: Processes raw data/business models, handles Coroutines, and manages user interactions.
2. `UiModel`: A strictly immutable Kotlin data class representing the exact state of the UI.
3. `UiComposer`: A stateless Jetpack Compose function that strictly takes a `UiModel` and renders the UI.
- **Rule:** Do NOT pass ViewModels directly into Composables. Data flows out via `UiModel`, actions flow in via callbacks/lambdas triggered from the UI.

## UI & Compose Development Rules
- **Design System:** Always check and use components from `core/design/` (e.g., custom `TopAppBar`, `CategoryPicker`, `PieChart`) before using standard Compose components.
- **Scaffolding:** Use `Scaffold` for top-level screens to ensure proper padding and Material 3 integration.
- **Edge-to-Edge:** Support edge-to-edge display by default (already enabled in `MainActivity`).
- **Previews:** Every Composable MUST have a `@Preview` function (and optionally a Light/Dark mode preview) for quick iteration.
- **Theming:** Use the local `BudgetTrackerTheme` for consistent styling. Never hardcode colors, dimensions, or strings. All user-facing text must reside in `strings.xml` files.

## Code Generation & Workflow
- **Boilerplate:** When asked to create a new feature, automatically generate the complete vertical slice: Hilt Modules, Room Entities/DAOs (if needed), Retrofit interfaces, UiModel, UiAdapter, and the basic UiComposer scaffold.
- **Testing:** When writing or modifying business logic (especially in `UiAdapter`, calculations, or data mappers), automatically provide or update the corresponding Unit Tests.
- **Refactoring:** Before implementing new features, check existing common UI packages to avoid duplicating components.

## Code Style
- Use trailing commas for multi-line parameters and arguments.
- Prefer `val` over `var` whenever possible. Use immutable collections (`List` instead of `MutableList` in public APIs).
- Use descriptive names for state variables (e.g., `isFetchingData`, `transactionListState`).
- Explicitly declare return types for all public functions and APIs.
