# Minimal Bitcoin Widget

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A minimal, open-source Bitcoin price widget for your Android home screen. Track the price of Bitcoin in your preferred currency with a clean and modern widget .

<a href='https://play.google.com/store/apps/details?id=com.jcoronado.minimalbitcoinwidget&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="250"/></a>

_Google Play and the Google Play logo are trademarks of Google LLC._

## Features

- **Real-time Price Tracking**: High-speed, CDN-cached Bitcoin price updates powered by CoinGecko.
- **Interactive Sparkline Chart**: Clean trend visualization on the dashboard with smooth curves, dynamic scaling, and toggleable display.
- **Widget Customization**: Dedicated customization screen to personalize widget typography (Google Sans Flex Rounded vs. System Default) and a live in-app widget preview matching your theme.
- **Homescreen Widgets**: Modern widgets built with Jetpack Compose Glance, plus a legacy widget option for broader device compatibility.
- **Quick Widget Pinning**: In-app shortcut to pin the widget directly to your home screen with a single tap.
- **Animated Price Updates**: Engaging per-digit slide animations indicating directional price movements.
- **Multiple Currencies**: Track prices in USD, GBP, JPY, EUR, CAD, MXN, AUD, and BRL with locale-aware currency symbol formatting.
- **Customizable Timeframes**: Choose to display price change percentages for 24 hours, 7 days, or 30 days.
- **Configurable Refresh Rates**: Tailor background update frequency with 1, 4, or 8-hour refresh intervals.
- **Material Design 3 Expressive**: Clean and modern UI using the latest Material 3 Expressive components and Material You dynamic theming (Android 12+).
- **Multi-Language Localization**: Full translation support across 9 languages (English, Spanish, German, French, Italian, Portuguese, Turkish, and more).
- **Developer Tools**: Integrated developer options with a Mock UI simulator to preview custom prices, currencies, and widget layouts without hitting network limits.
- **Privacy-Focused**: 100% open-source, no ads, no trackers, and no unnecessary permissions.

## Screenshots

<p align="center">
  <img src="images/listing1-en.webp" width="20%" alt="listing 1 image">
  <img src="images/listing2-en.webp" width="20%" alt="listing 2 image">
  <img src="images/listing3-en.webp" width="20%" alt="listing 3 image">
  <img src="images/listing4-en.webp" width="20%" alt="listing 4 image">
</p>

## Getting Started

To build and run the project locally, you can clone the repository and open it in Android Studio.

1.  **Clone the repository.**
2.  **Open the project in Android Studio.**
3.  **Build & Run:** Let Gradle sync, then build and run the app on an emulator or a physical device.

## Built With

- [Kotlin](https://kotlinlang.org/): Primary programming language.
- [Jetpack Compose](https://developer.android.com/compose): Modern toolkit for building native UI.
- [Material 3 Expressive](https://developer.android.com/jetpack/compose/designsystems/material3): Latest Material Design features and components.
- [Glance](https://developer.android.com/jetpack/compose/glance): Build app widgets with a Jetpack Compose-style API.
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager): For reliable, periodic background price updates.
- [Navigation 3](https://developer.android.com/jetpack/compose/navigation): Modern, type-safe navigation for Compose.
- [Room](https://developer.android.com/training/data-storage/room): For local database persistence and logging.
- [OkHttp](https://square.github.io/okhttp/): For efficient HTTP requests to the CDN-cached backend proxy.
- [Gson](https://github.com/google/gson): For robust JSON serialization and parsing.

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions made are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/my-new-feature`)
3.  Commit your Changes (`git commit -m '''Add some my-new-feature'''`)
4.  Push to the Branch (`git push origin feature/my-new-feature`)
5.  Open a Pull Request

## License

Distributed under the GNU General Public License v3.0. See the `LICENSE` file for more information.

## Acknowledgments

- Bitcoin price data powered by the [CoinGecko API](https://www.coingecko.com/api).

## Contact & Support

Julian Coronado - [jcoronado.dev](https://jcoronado.dev)

If you'd like to support the development of this project, feel free to donate!

**Donate via Strike (Bitcoin & Lightning):** [strike.me/jcoronado](https://strike.me/jcoronado)
