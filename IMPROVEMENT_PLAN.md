# AI-Ready Development Roadmap: Minimal Bitcoin Widget

This document serves as a structured technical specification for an AI agent to perform structural and UI improvements on the Minimal Bitcoin Widget project.

---

## Project Context for AI

- **App Type:** Bitcoin Price Tracking Widget & App.
- **Tech Stack:** Kotlin, Jetpack Compose, Glance (Widgets), WorkManager, OkHttp/Gson, Navigation 3.
- **Data Flow:** Price data is fetched from CoinGecko API via `PriceRepository`, cached as a JSON string in `SharedPreferences`, and displayed in a Compose UI and Home Screen Widgets (Glance + Legacy).

---

## Phase 1: Structural & Architectural Modernization

### Task 1: Migrate to Jetpack DataStore

- **Goal:** Replace `SharedPreferences` with a modern, reactive storage solution.
- **Current State:** Settings and JSON cache are stored in `com.jcoronado.minimalbitcoinwidget.classes.Prefs`.
- **Target Files:**
  - Create: `com.jcoronado.minimalbitcoinwidget.data.DataStoreManager.kt`
  - Modify: `PriceRepository.kt`, `SettingsViewModel.kt`
- **Instructions:**
  1. Implement **Preferences DataStore** for user settings (currency, theme, intervals).
  2. Implement **Proto DataStore** or a cleaner JSON-based DataStore for the `PriceData` cache.
  3. Replace all calls to `PreferenceManager.getDefaultSharedPreferences`.

### Task 2: Dependency Injection with Hilt

- **Goal:** Remove manual dependency management and `AndroidViewModel` boilerplate.
- **Target Files:** `build.gradle.kts`, `MainActivity.kt`, `PriceViewModel.kt`, `SettingsViewModel.kt`, `PriceUpdateWorker.kt`.
- **Instructions:**
  1. Add Hilt dependencies.
  2. Annotate the Application class and `MainActivity`.
  3. Inject `PriceRepository` and `DataStoreManager` into ViewModels.
  4. Use `@HiltWorker` for the `PriceUpdateWorker`.

---

## Phase 2: Code Quality & Maintenance

### Task 3: Comprehensive Unit Testing

- **Goal:** Ensure reliability and prevent regressions by implementing a suite of unit tests for data and business logic.
- **Tools:** JUnit 4/5, MockK, and `kotlinx-coroutines-test`.
- **Instructions:**
  1. **Test `PriceData` logic:** Verify `getPercentageForInterval(index: Int)` handles all valid indices correctly and gracefully manages out-of-bounds or null data.
  2. **Test `PriceRepository`:** Mock the network and DataStore to verify data fetching, local caching, and the correct emission of `Resource` states (`Success`, `Error`).
  3. **Test `PriceViewModel`:** Use a `TestDispatcher` to assert that the `UIState` correctly reflects Repository emissions (e.g., verifying `isLoading` is true when the flow starts).
  4. **Test `DataStoreManager`:** Verify settings (currency, interval) are persisted and retrieved correctly using a temporary DataStore instance.

---

## Phase 3: UI/UX Enhancements

### Task 4: Widget Consolidation

- **Goal:** Deprecate Legacy widget logic at a future date (2027).
- **Instructions:**
  1. Evaluate if Glance covers all necessary features.
  2. If so, remove `widgets.legacy` package and `PriceWidget.kt` (Legacy wrapper).
  3. Ensure `PriceUpdateWorker` only needs to trigger Glance updates.

### Task 5: Immediate Widget Synchronization on App Updates (`MY_PACKAGE_REPLACED`)

- **Goal:** Immediately refresh all homescreen widgets (Glance and Legacy) whenever an app update is installed, preventing stale cached resource IDs or icon misalignment in the system Launcher.
- **Context:** The Android Launcher caches `RemoteViews` layouts using compiled integer resource IDs (`R.drawable.*`). When an app update introduces, removes, or renames drawables, `aapt2` re-indexes resource IDs alphabetically. Without an explicit update broadcast, the Launcher continues rendering cached layouts using old IDs against the new APK's resource table, causing mismatched icons until a widget refresh occurs.
- **Target Files:**
  - Modify: `app/src/main/AndroidManifest.xml`
  - Create: `com.jcoronado.minimalbitcoinwidget.receivers.AppUpdateReceiver.kt` (or extend `PriceWidgetReceiver`)
- **Instructions:**
  1. Register a broadcast receiver in `AndroidManifest.xml` listening for `android.intent.action.MY_PACKAGE_REPLACED`.
  2. In `onReceive`, call `Prefs.checkAppUpdateAndInvalidateCache(prefs)` to sync version tracking.
  3. Immediately invoke `PriceViewModel.refreshWidgetsFromCache(context)` to redraw both Glance and Legacy widgets with fresh resource IDs.
  4. Ensure `PriceUpdateWorker.enqueue(context)` is re-verified so background polling schedules persist seamlessly after updates.


