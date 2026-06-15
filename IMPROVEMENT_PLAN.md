# AI-Ready Development Roadmap: Minimal Bitcoin Widget

This document serves as a structured technical specification for an AI agent to perform structural and UI improvements on the Minimal Bitcoin Widget project.

---

## Project Context for AI
- **App Type:** Bitcoin Price Tracking Widget & App.
- **Tech Stack:** Kotlin, Jetpack Compose, Glance (Widgets), WorkManager, OkHttp/Gson, Navigation 3.
- **Data Flow:** Price data is fetched from CoinGecko API, cached as a JSON string in `SharedPreferences`, and displayed in a Compose UI and Home Screen Widgets (Glance + Legacy).

---

## Phase 1: Structural & Architectural Modernization

### Task 1: Implement Repository Pattern
- **Goal:** Centralize data fetching and caching logic.
- **Current State:** `PriceViewModel.kt` and `PriceUpdateWorker.kt` both implement manual OkHttp calls, Gson parsing, and SharedPreferences caching.
- **Target Files:**
  - Create: `com.jcoronado.minimalbitcoinwidget.data.PriceRepository.kt`
  - Modify: `PriceViewModel.kt`, `PriceUpdateWorker.kt`
- **Instructions:**
  1. Create a `PriceRepository` class.
  2. Move networking (OkHttp) and caching (`SharedPreferences`) logic from the ViewModel/Worker into the Repository.
  3. Expose data via a `Flow<PriceData?>` or a `Result` wrapper.
  4. Inject or instantiate this repository in both the ViewModel and Worker.

### Task 2: Migrate to Jetpack DataStore
- **Goal:** Replace `SharedPreferences` with a modern, reactive storage solution.
- **Current State:** Settings and JSON cache are stored in `com.jcoronado.minimalbitcoinwidget.classes.Prefs`.
- **Target Files:**
  - Create: `com.jcoronado.minimalbitcoinwidget.data.DataStoreManager.kt`
  - Modify: `PriceRepository.kt`, `SettingsViewModel.kt`
- **Instructions:**
  1. Implement **Preferences DataStore** for user settings (currency, theme, intervals).
  2. Implement **Proto DataStore** or a cleaner JSON-based DataStore for the `PriceData` cache.
  3. Replace all calls to `PreferenceManager.getDefaultSharedPreferences`.

### Task 3: Dependency Injection with Hilt
- **Goal:** Remove manual dependency management and `AndroidViewModel` boilerplate.
- **Target Files:** `build.gradle.kts`, `MainActivity.kt`, `PriceViewModel.kt`, `SettingsViewModel.kt`, `PriceUpdateWorker.kt`.
- **Instructions:**
  1. Add Hilt dependencies.
  2. Annotate the Application class and `MainActivity`.
  3. Inject `PriceRepository` and `DataStoreManager` into ViewModels.
  4. Use `@HiltWorker` for the `PriceUpdateWorker`.

---

## Phase 2: Code Quality & Maintenance

### Task 4: Standardized API Result Handling
- **Goal:** Handle errors consistently across the app.
- **Instructions:**
  1. Create a `sealed class Resource<T>` with `Success`, `Error`, and `Loading` states.
  2. Update the Repository to return `Flow<Resource<PriceData>>`.
  3. Update `PriceUiState` and `PriceWidgetState` to map directly from these `Resource` states.

### Task 5: Comprehensive Unit Testing
- **Goal:** Ensure reliability and prevent regressions by implementing a suite of unit tests for data and business logic.
- **Tools:** JUnit 4/5, MockK, and `kotlinx-coroutines-test`.
- **Instructions:**
  1. **Test `PriceData` logic:** Verify `getPercentageForInterval(index: Int)` handles all valid indices correctly and gracefully manages out-of-bounds or null data.
  2. **Test `PriceRepository`:** Mock the network and DataStore to verify data fetching, local caching, and the correct emission of `Resource` states (`Success`, `Error`).
  3. **Test `PriceViewModel`:** Use a `TestDispatcher` to assert that the `UIState` correctly reflects Repository emissions (e.g., verifying `isLoading` is true when the flow starts).
  4. **Test `DataStoreManager`:** Verify settings (currency, interval) are persisted and retrieved correctly using a temporary DataStore instance.

---

## Phase 3: UI/UX Enhancements

### Task 6: Widget Consolidation
- **Goal:** Deprecate Legacy widget logic at a future date (2027).
- **Instructions:**
  1. Evaluate if Glance covers all necessary features.
  2. If so, remove `widgets.legacy` package and `PriceWidget.kt` (Legacy wrapper).
  3. Ensure `PriceUpdateWorker` only needs to trigger Glance updates.

### Task 7: Canvas-Based Price Trend Sparkline
- **Goal:** Add visual historical charts to the dashboard.
- **Target Files:** `MainScreen.kt`, `PriceViewModel.kt`, `PriceData.kt`
- **Instructions:**
  1. Ensure historical coordinate points (sparkline data) are retrieved from the CoinGecko API payload.
  2. Create a custom Canvas-based `@Composable` function to draw a smooth bezier-curve sparkline graph.
  3. Animate the line drawing when the dashboard loads or interval changes.
