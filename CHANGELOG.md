# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Date Format: YYYY-MM-DD

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
