# AI-Ready Development Roadmap: Minimal Bitcoin Widget

This document serves as a structured technical specification for an AI agent to perform structural and UI improvements on the Minimal Bitcoin Widget project only when requested by the user.

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

- **Goal:** Deprecate Legacy widget logic at a future date (2027) alongside the `v4.0.0` release (see Task 7).
- **Instructions:**
  1. Evaluate if Glance covers all necessary features.
  2. If so, remove `widgets.legacy` package and `PriceWidget.kt` (Legacy wrapper).
  3. Ensure `PriceUpdateWorker` only needs to trigger Glance updates.

---

## Phase 4: Android OS Lifecycle & Versioning Roadmap (2027)

### Task 5: Early 2027 (January?) — Drop Android 9 & 10 (Minor Release: `v3.7.0`)

- **Goal:** Modernize baseline OS support by dropping Android 9 (API 28) and Android 10 (API 29).
- **Impact Analysis (Oct 2026 data):**
  - Android 9: ~0.2% of app users
  - Android 10: ~1.2% of app users
  - Total audience retained: **~98.6%** (Android 11+).
- **Target Files:**
  - `app/build.gradle.kts` (`minSdk = 30`)
  - `AGENTS.md` (Update Tech Stack: `Min SDK 30`)
- **Instructions:**
  1. Bump `minSdk` from 28 to 30 in `app/build.gradle.kts`.
  2. Treat as a **Minor** version bump (e.g., `3.7.0` or `3.8.0`) since this is routine maintenance with minimal audience impact.
  3. Existing installations on Android 9/10 continue running without breaking; they simply remain on the last compatible APK.

### Task 6: Later in 2027 — Android 12 Baseline & Major Milestone (`v4.0.0`)

- **Goal:** Advance `minSdk` to 31 (Android 12), remove all legacy XML widget code, and bundle with new UI features for a major `v4.0.0` release.
- **Context & Synergy with Task 4 (Widget Consolidation):**
  - Android 12 (API 31) is the architectural dividing line for modern widgets (`system_app_widget_background_radius`, Material You dynamic colors).
  - Bumping `minSdk` to 31 allows full retirement of `LegacyPriceWidget` and backwards-compatible `RemoteViews` layouts.
- **Target Files:**
  - `app/build.gradle.kts` (`minSdk = 31`, `versionName = "4.0.0"`)
  - `AGENTS.md` (Update Tech Stack: `Min SDK 31`)
  - Remove: `com.jcoronado.minimalbitcoinwidget.widgets.legacy` package and XML widget layouts.
  - Modify: `PriceWidget.kt`, `TrendlinePriceWidget.kt`, `GlanceColorScheme.kt`, `SettingsScreen.kt`, `PriceViewModel.kt`.
- **Instructions:**
  1. Bump `minSdk` to 31 in `app/build.gradle.kts`.
  2. Execute **Task 4 (Widget Consolidation)**: Delete `widgets.legacy` package, XML layout resources (`legacy_price_widget.xml`), and dual-update logic in `PriceViewModel`.
  3. Strip out `Build.VERSION.SDK_INT >= 31` and `Build.VERSION_CODES.S` branching across the app.
  4. Pair with new UI changes and feature additions to deliver a comprehensive, user-facing `v4.0.0` release.



