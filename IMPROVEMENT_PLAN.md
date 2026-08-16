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

### Task 5: Custom Widget Font Selection & Dynamic Typography
- **Goal:** Allow users to choose their preferred typography for the Glance home screen widget (e.g. Google Sans Rounded, Manrope, Google Sans Code, System Default).
- **Context & Constraints:**
  - Android `RemoteViews` and Jetpack Glance cannot load custom `.ttf`/`.otf` font files across process boundaries via standard `Text(style = TextStyle(fontFamily = ...))` composables.
  - Custom font rendering in widgets is achieved by drawing text onto a crisp `Bitmap` using Android `Canvas` + `Paint` (`ANTI_ALIAS_FLAG`), then displaying it via Glance `Image(ImageProvider(bitmap), colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface))` to ensure automatic Material You dynamic theming and dark mode compatibility.
- **Target Files:**
  - Font Assets: `app/src/main/res/font/google_sans_flex_rounded.ttf`, `app/src/main/res/font/manrope_bold.ttf`, `app/src/main/res/font/google_sans_code.ttf`
  - Model & Constants: `classes/WidgetFont.kt`, `classes/Prefs.kt`
  - Settings UI: `screens/SettingsScreen.kt`, `viewmodels/SettingsViewModel.kt`
  - Widget UI: `widgets/glance/PriceWidget.kt`, `widgets/glance/PriceWidgetState.kt`
  - Translations: All 9 `values*/strings.xml` files for font labels.
- **Instructions:**
  1. **Define `WidgetFont` Model:**
     - Create an enum/class representing available font choices (`SYSTEM_DEFAULT`, `GOOGLE_SANS_ROUNDED`, `MANROPE`, `GOOGLE_SANS_CODE`) with `@StringRes` name and `@FontRes` font resource ID.
  2. **Add Preference Storage:**
     - Add `Prefs.SELECTED_WIDGET_FONT` (or DataStore equivalent) with `GOOGLE_SANS_ROUNDED` or `SYSTEM_DEFAULT` as default.
  3. **Settings Screen Integration:**
     - Add a settings row/dialog in `SettingsScreen.kt` with a live visual preview of `$62,884.21` rendered in each available font.
     - On selection, update preference and trigger immediate widget refresh via `PriceViewModel.updateGlanceWidgets()`.
  4. **Glance Widget Bitmap Renderer:**
     - In `PriceWidget.kt` (or a dedicated `WidgetBitmapUtils.kt`), implement text bitmap creation:
       ```kotlin
       fun createTextBitmap(
           context: Context,
           text: String,
           fontSizeSp: Float,
           typeface: Typeface?
       ): Bitmap {
           val density = context.resources.displayMetrics.density
           val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
               this.typeface = typeface
               this.textSize = fontSizeSp * density
               this.color = Color.WHITE
           }
           val width = ceil(paint.measureText(text)).toInt().coerceAtLeast(1)
           val fontMetrics = paint.fontMetrics
           val height = ceil(fontMetrics.descent - fontMetrics.ascent).toInt().coerceAtLeast(1)
           val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
           val canvas = Canvas(bitmap)
           canvas.drawText(text, 0f, -fontMetrics.ascent, paint)
           return bitmap
       }
       ```
     - Resolve the user's selected `Typeface` via `ResourcesCompat.getFont(context, fontResId)` (or `Typeface.DEFAULT`).
     - Render the price and symbol strings into Bitmaps and display them using Glance's `Image(provider = ImageProvider(bitmap), colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface))`.
  5. **Localization:**
     - Provide translations across all 9 `strings.xml` files for the font picker title and font names.



