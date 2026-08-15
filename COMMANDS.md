# Minimal Bitcoin Widget - Helpful Commands & Scripts

This file contains useful developer scripts, ADB commands, and release helpers for the Minimal Bitcoin Widget project.

---

## 1. Locale & Region Switching (ADB Helper)

To quickly test translations and regional number/currency formatting variations (e.g. prefix vs. suffix symbols, comma vs. dot decimals, space grouping) across all supported languages in the Android emulator:

```bash
# For testing MBW locale switching via ADB
set-mbw-locale() {
  local LOCALE="$1"
  case "$LOCALE" in
    # English
    en|en-us|us)      LOCALE_TAG="en-US" ;; # Dot decimal, Comma grouping, $ in front
    en-gb|uk)         LOCALE_TAG="en-GB" ;; # Dot decimal, Comma grouping, £ in front

    # Spanish
    es|es-es)         LOCALE_TAG="es-ES" ;; # Comma decimal, Dot grouping, € at end
    es-mx|mx)         LOCALE_TAG="es-MX" ;; # Dot decimal, Comma grouping, $ in front

    # German
    de|de-de)         LOCALE_TAG="de-DE" ;; # Comma decimal, Dot grouping, € at end
    de-ch|ch)         LOCALE_TAG="de-CH" ;; # Dot decimal, Apostrophe grouping (CHF 95'000.00)

    # French
    fr|fr-fr)         LOCALE_TAG="fr-FR" ;; # Comma decimal, Space grouping, € at end
    fr-ca|ca)         LOCALE_TAG="fr-CA" ;; # Comma decimal, Space grouping, $ at end

    # Portuguese
    pt|pt-br|br)      LOCALE_TAG="pt-BR" ;; # Comma decimal, Dot grouping, R$ in front
    pt-pt)            LOCALE_TAG="pt-PT" ;; # Comma decimal, Space grouping, € at end

    # Italian & Turkish
    it|it-it)         LOCALE_TAG="it-IT" ;; # Comma decimal, Dot grouping, € at end
    tr|tr-tr)         LOCALE_TAG="tr-TR" ;; # Comma decimal, Dot grouping, ₺ at end

    # Reset
    reset|"")         LOCALE_TAG="" ;;

    # Custom / Passthrough (e.g. ja-JP)
    *)                LOCALE_TAG="$LOCALE" ;;
  esac

  echo "Switching MBW locale to: ${LOCALE_TAG:-[System Default]}..."
  adb shell cmd locale set-app-locales com.jcoronado.minimalbitcoinwidget --locales "$LOCALE_TAG"
}
```

### Usage Examples:
* `set-mbw-locale mx` &rarr; Spanish (Mexico) &ndash; `$ 95,000.00`
* `set-mbw-locale es` &rarr; Spanish (Spain) &ndash; `95.000,00 €` / `95.000,00 US$`
* `set-mbw-locale de` &rarr; German &ndash; `95.000,00 €`
* `set-mbw-locale fr` &rarr; French &ndash; `95 000,00 €`
* `set-mbw-locale br` &rarr; Portuguese (Brazil) &ndash; `R$ 550.000,00`
* `set-mbw-locale tr` &rarr; Turkish &ndash; `%2,03`
* `set-mbw-locale reset` &rarr; Reset back to device system default

---

## 2. Release: Package Native Debug Symbols

When preparing a release bundle for Google Play Console submission, package the unstripped native debug symbols into a `.zip` archive containing the current app version (`versionName`) and build number (`versionCode`) in the filename:

```bash
# Example for v3.3.0 (build 19)
cd app/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib && zip -r ../../../../../../../native-debug-symbols-v3.3.0-19.zip arm64-v8a armeabi-v7a x86 x86_64 && cd -
```
