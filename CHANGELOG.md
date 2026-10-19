# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Date Format: YYYY-MM-DD

## [Unreleased]

### Added

### Changed

---

## [3.6.0] - PENDING

### Added

- Added new Glance-based **Chart Widget** (2x2) featuring current Bitcoin price, price change percentage pill, and sparkline price trend chart with reference dashed baseline.
- Added "Add to Home Screen" bottom sheet on the Dashboard to easily choose between pinning the Price Widget or Chart Widget.
- Added swipeable pager with Pixel-style indicator dots to the Customize Widget screen to preview both Price and Chart widgets in real time.
- Added `PackageUpdateReceiver` to immediately refresh and repaint homescreen widgets from local cache on app update without making network requests.
- Added interactive tap animation to replay sparkline chart entrance on the Dashboard.

### Changed

- Standardized chart terminology and verbiage across dashboard, settings, and widget picker.
- Added informative notice in Settings when 30-day interval is selected regarding chart availability.
- Updated widget picker descriptions and marked Legacy Price Widget as retiring soon.
- Declared productivity `appCategory` in app manifest for improved launcher widget discovery.
- Updated dependencies including Kotlin 2.4.21, Material 3 Expressive 1.5.0-beta01, and NavigationEvent 1.2.0.

### Fixed

- Fixed "Rate on Google Play" shortcut to direct exclusively to Google Play store reviews section.
- Fixed currency symbol and decimal formatting alignment across all locales in widget layouts.

## [3.5.0] - 2026-10-12

### Added

- Added widget customization options: Price Size (Small, Default, Large), Colors (Dynamic, Solid), and Theme (System, Light, Dark).

### Changed

- Harmonized dynamic widget theme colors across Light, Dark, and System modes.
- Centralized Glance widget color palettes and background providers into `GlanceColorScheme`, eliminating inline allocations on widget composition passes.

## [3.4.1] - 2026-10-05

### Changed

- Reduced font weight on bolded text for better readability
- Small wording change on the Customize Widget screen

## [3.4.0] - 2026-09-29

### Added

- Added new Google Sans Flex Rounded variable font to the app and Glance homescreen widget with a dedicated "Customize Widget" settings screen.
- Added widget font customization options (App Default vs. System Default).

### Changed

- Migrated price fetching to a fast, CDN-cached backend proxy to improve widget refresh speed and reliability worldwide.

## [3.3.1] - 2026-08-25

### Fixed

- Fixed sparkline price trend chart flattening on non-USD currencies (such as JPY, MXN, and BRL) by using proportional relative currency scaling.

## [3.3.0] - 2026-08-17

### Added

- Added interactive sparkline price trend chart with smooth curves, entrance animations, and a configurable setting toggle.
- Added per-digit slide animation for price updates with directional up/down scrolling.

## [3.2.0] - 2026-07-21

### Added

- Added configurable Mock UI State in Developer options to simulate static custom data (Price, Change %, and Currency) in the app and homescreen widgets for testing and screenshots.

## [3.1.1] - 2026-06-04

### Fixed

- Fixed currency mismatch bug where the app dashboard fetched USD prices but formatted them with the user's local currency symbol (such as EUR) on launch/refresh.

## [3.1.0] - 2026-04-30

### Added

- Added haptic feedback when navigating the UI
- Added "Shortcuts" section to quickly pin widget to homescreen

### Changed

- Changed default font from Google Sans to Google Sans Flex

## [3.0.0] - 2026-04-21

### Added

- Upgrade app UI to use Jetpack Compose and Material 3 Expressive.
- Add new homescreen widget to use Jetpack Glance (Legacy app widget still exists).
- Support for 24-hour, 7-day and 30-day price change percentages.
- Support for Japanese Yen (JPY) currency.
- Configurable widget refresh intervals (1, 4, or 8 hours).
- Modern, type-safe navigation using Navigation 3.
- Support for Material You dynamic colors on Android 12+.
- Hidden "Developer Mode" unlocked via tapping Build Number.

### Changed

- Improved background update reliability using WorkManager.
- Enhanced theme switching with support for Light, Dark, and System modes.

## [2.6.1] - 2025-12-21

### Fixed

- Handle network failures when the widget refreshes data

## [2.6.0] - 2025-12-19

### Added

- Local cache to store the price data locally
- Loading indicator on homescreen widget
- Displaying the price based on the devices locale format

### Changed

- Updated Android dependencies
- Updated Coingecko endpoint to use simple/price/

## [2.5.0] - 2025-12-13

### Added

- "What's New" dialog to show changes after an update.
- Support for German, Spanish, French, Italian, Portuguese, and Turkish languages.
- Monochrome icon for Android 13+ themed icons.
- Material Design loading indicator.

### Changed

- Refreshed the UI to be "Edge-to-Edge" for a more modern look.
- Improved homescreen widget sizing and text properties.
- API requests now time out after 10 seconds to prevent indefinite loading.

### Fixed

- Corrected text colors in Dark Mode to improve readability.
- Fixed errors when setting local currency to BRL.
- Implemented Toast messages to properly notify users of API errors.

## [2.4] - 2023-10-03

- Previous changes not included in this file. Check in commit history on the master branch.
