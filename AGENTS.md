# Minimal Bitcoin Widget - Context for AI Agents

This file provides context, architectural guidelines, and conventions for AI assistants and agents interacting with this repository.

## Overview

**Minimal Bitcoin Widget** is an Android application that provides homescreen widgets to display the current price of Bitcoin. It includes a companion app for configuration and relies heavily on Modern Android Development (MAD) practices.

## Tech Stack

- **Language:** Kotlin (JVM 11 target)
- **SDK Versions:** Min SDK 28, Target/Compile SDK 37
- **UI Framework:** Jetpack Compose (Material 3 / Expressive)
- **Widgets:** Jetpack Glance for modern widgets, with support for legacy AppWidget.
- **Background Work:** WorkManager (for periodic API polling)
- **Networking:** OkHttp, Gson (Fetches data from CoinGecko API)
- **Local Storage:** SharedPreferences (for user settings and caching), Room Database (primarily for debugging/logs)
- **Architecture:** MVVM (Model-View-ViewModel) with Repository Pattern

## Project Structure

The project consists of a single `:app` module. The main source code is under `app/src/main/java/com/jcoronado/minimalbitcoinwidget/`.

Key directories and files:

- **`MainActivity.kt`**: The main entry point for the companion Compose app.
- **`data/`**: Data layer containing `PriceRepository.kt` for centralizing API requests (OkHttp/Gson), caching, and mock UI data logic, and `Resource.kt` for standardized API result state management (`Success`, `Error`, `Loading`).
- **`screens/` & `ui/`**: Jetpack Compose UI screens, theming, and reusable components.
- **`viewmodels/`**: ViewModels (e.g., `PriceViewModel`) that handle business logic, UI state, and bridge the data to the widgets.
- **`widgets/`**: Contains widget implementations. Divided into Glance widgets (`PriceWidget`) and legacy implementations (`LegacyPriceWidget`).
- **`workers/`**: Contains `PriceUpdateWorker.kt` which uses WorkManager to periodically fetch Bitcoin prices in the background via `PriceRepository`.
- **`utils/`**: Utility functions such as `FormatUtils.kt` for centralized price, currency symbol disambiguation, and percentage change formatting.
- **`classes/`**: Data models, constants (`AppConstants`), preference keys (`Prefs`), and API configuration (`Api`).
- **`AppDatabase.kt`**: Room database setup, mainly used for storing internal `DebugLog` entries.

## Development Conventions & Guidelines

### 1. UI & Theming

- **Strictly use Jetpack Compose** for all new app UI development. Avoid introducing new XML layouts.
- Follow **Material 3 & Material 3 Expressive** design guidelines.
- **Formatting:** Use `FormatUtils` (`formatPriceSeparated`, `formatChange`) for all price and percentage displays to guarantee proper symbol placement (prefix vs. suffix), comma/dot decimal rules, and native symbol disambiguation across locales.

### 2. Widget Development

- Prefer **Jetpack Glance** for building and updating widget UI. Maintain existing legacy XML for backwards compatibility.
- Ensure state updates (like new price data or errors) are propagated to both Glance and legacy widgets where necessary. Widget states are often managed through `PriceViewModel` helper functions.

### 3. Background Processing & Networking

- All periodic data fetching must go through **WorkManager** (`PriceUpdateWorker`).
- **Respect API limits:** The app fetches from CoinGecko. Maintain the existing caching logic (e.g., the 30-minute cache buffer via `AppConstants.CACHE_DURATION_MILLIS`) to avoid `429 Too Many Requests` errors.
- Handle network failures gracefully. If a fetch fails, the widget should be updated to an error state (`PriceWidgetState.Error`) rather than crashing, and WorkManager should handle the retry backoff.

### 4. Data Storage

- **SharedPreferences:** Used for lightweight data, user preferences (e.g., selected currency, refresh interval), and caching the most recent JSON response.
- **Room Database:** Currently used for internal debugging/logging (`DebugLog`). Avoid using it for heavy relational data unless the app's scope expands.

### 5. State Management & Data Architecture

- Use `ViewModel` combined with Kotlin Coroutines for managing app state.
- All network operations, cache management, and data access MUST go through `PriceRepository`. ViewModels and Workers should not perform direct HTTP network calls or manual `SharedPreferences` cache serialization.
- **API Results:** Use `Resource<T>` (`Resource.Success`, `Resource.Error`, `Resource.Loading`) for modeling API results across Repository, ViewModels, and Workers.
- **ViewModel Constructor Overloads:** When creating or updating a ViewModel constructor with default parameter values (e.g. for default repository instances), annotate the constructor with `@JvmOverloads constructor(...)` so Android framework factories (`AndroidViewModelFactory`) can instantiate it via Java Reflection.
- Keep the worker logic separate from the UI. Workers should request data updates via `PriceRepository` and trigger a widget refresh.

### 6. Debugging & Mock UI

- **Mock UI State:** A Mock UI mode is available in the Developer Options screen. When enabled, it allows simulating static, user-defined custom data (Price, Change %, and Currency) for screenshots and testing.
- **Interception Logic:** When active (`Prefs.DEBUG_MOCK_UI_ENABLED`), standard loading, live API fetches, widget redrawing, and background worker fetches (`PriceUpdateWorker`) are bypassed to persistently display the custom mock configurations without calling the CoinGecko API.

### 8. Testing, Tooling and Running the App

- For ADB emulator commands, ADB locale switching scripts, build helper functions, see [COMMANDS.md](COMMANDS.md).

## General AI Instructions

- When making changes to the UI, verify compatibility with Jetpack Compose Material 3.
- When modifying data fetching logic, ensure background constraints (like network connectivity) and caching rules are preserved.
- Prioritize Kotlin idioms, Coroutines, and Flow where appropriate.
- When providing stringResource translations, ensure translations exist for all strings.xml files (currently 9 total).
- When library / structure / tech stack changes are introduced, update AGENTS.md file as well (example: bumping minimum Android SDK)
- **Dependency Overrides:** `androidx.navigationevent` and `androidx.navigationevent-compose` are force-resolved to version `1.2.0-alpha01` (or newer) to bypass the framework bug `IllegalStateException: This input is not added to any dispatcher` during predictive back gestures when popups/dropdowns are active. This override should remain until the fix is released in a stable channel version, at which point it should be reverted to the stable channel.
