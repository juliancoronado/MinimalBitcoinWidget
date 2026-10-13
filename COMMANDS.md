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

- `set-mbw-locale mx` &rarr; Spanish (Mexico) &ndash; `$ 95,000.00`
- `set-mbw-locale es` &rarr; Spanish (Spain) &ndash; `95.000,00 €` / `95.000,00 US$`
- `set-mbw-locale de` &rarr; German &ndash; `95.000,00 €`
- `set-mbw-locale fr` &rarr; French &ndash; `95 000,00 €`
- `set-mbw-locale br` &rarr; Portuguese (Brazil) &ndash; `R$ 550.000,00`
- `set-mbw-locale tr` &rarr; Turkish &ndash; `%2,03`
- `set-mbw-locale reset` &rarr; Reset back to device system default

---

## 2. Android CLI Tool & Skill Usage (`android-cli`)

For emulator management, app execution, layout inspection, and documentation searches, use the official `android` CLI tool (configured via the `android-cli` skill at `~/.gemini/config/plugins/android-cli-plugin/skills/SKILL.md`).

### A. Emulator Management

Instead of raw `adb` / `qemu` background processes or aliases, prefer the `android emulator` commands:

```bash
# List all available Android Virtual Devices (AVDs)
android emulator list

# Start a specific emulator (automatically waits until device is fully booted and ready)
android emulator start "EMULATOR NAME"

# Stop a running emulator
android emulator stop "EMULATOR NAME"
```

### B. Building, Deploying & Running the App

```bash
# Build the debug APK and deploy/launch via android CLI
./gradlew assembleDebug && android run --apks=app/build/outputs/apk/debug/app-debug.apk --activity=com.jcoronado.minimalbitcoinwidget.MainActivity
```

### C. UI Layout Inspection & Screenshots

```bash
# Capture a screenshot from the running device
android screen capture screenshot.png

# Dump the current UI layout tree (fast JSON inspection for UI hierarchy debugging)
android layout --pretty
```

### D. Official Documentation Search

```bash
# Search authoritative Android developer documentation
android docs search "Glance AppWidget"

# Fetch article content by Knowledge Base URL
android docs fetch "kb://..."
```

---

## 3. Release: Package Native Debug Symbols

Google Play Console displays a warning when uploading an App Bundle (`.aab`):
> *"This App Bundle contains native code, and you haven't uploaded debug symbols. We recommend you upload a symbol file to make your crashes and ANRs easier to analyze and debug."*

This warning is triggered by pre-compiled native libraries in Android Jetpack dependencies (specifically `libandroidx.graphics.path.so` from `androidx.graphics:graphics-path`). Because the library is pre-compiled, the Android Gradle Plugin (AGP) does not automatically generate a standalone debug symbols `.zip` when compiling release bundles.

### Expected Google Play Format

Google Play Console requires a `.zip` archive whose root contains the ABI folders directly:
```text
native-debug-symbols-v<versionName>-<versionCode>.zip
├── arm64-v8a/libandroidx.graphics.path.so
├── armeabi-v7a/libandroidx.graphics.path.so
├── x86/libandroidx.graphics.path.so
└── x86_64/libandroidx.graphics.path.so
```

### One-Step Automated Command / Helper

Run this function from the repository root. It reads `versionName` and `versionCode` from `app/build.gradle.kts`, packages the unstripped symbols directly into the base of the repository folder (`MinimalBitcoinWidget/`), and verifies the archive layout:

```bash
package-mbw-symbols() {
  local LIB_DIR="app/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib"

  if [ ! -d "$LIB_DIR" ]; then
    echo "Native libs folder not found. Running mergeReleaseNativeLibs task..."
    ./gradlew mergeReleaseNativeLibs
  fi

  local VERSION_NAME=$(grep 'versionName =' app/build.gradle.kts | head -1 | sed -E 's/.*"([^"]+)".*/\1/')
  local VERSION_CODE=$(grep 'versionCode =' app/build.gradle.kts | head -1 | tr -dc '0-9')
  local ZIP_NAME="native-debug-symbols-v${VERSION_NAME}-${VERSION_CODE}.zip"

  echo "Packaging native debug symbols for v${VERSION_NAME} (build ${VERSION_CODE}) into base repo folder..."
  (cd "$LIB_DIR" && zip -r "$OLDPWD/$ZIP_NAME" arm64-v8a armeabi-v7a x86 x86_64 -x "*.DS_Store*")

  echo "Successfully generated: $ZIP_NAME in MinimalBitcoinWidget/"
  unzip -l "$ZIP_NAME"
}
```

### Manual Command

If running manual steps from the base of `MinimalBitcoinWidget/` (substitute the version name and build number):

```bash
# 1. Ensure release native libs are merged
./gradlew mergeReleaseNativeLibs

# 2. Package from out/lib directly into the base repo directory (MinimalBitcoinWidget/)
(cd app/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib && zip -r "$OLDPWD/native-debug-symbols-v3.5.0-25.zip" arm64-v8a armeabi-v7a x86 x86_64 -x "*.DS_Store*")

# 3. Verify archive structure
unzip -l native-debug-symbols-v3.5.0-25.zip
```

### Uploading to Google Play Console

1. Open **Google Play Console** &rarr; select **Minimal Bitcoin Widget**.
2. Go to **Release** &rarr; **App bundle explorer**.
3. Under **Releases**, select the newly uploaded version.
4. Open the **Downloads** tab.
5. In the **Native debug symbols** section, click **Upload** and select the `.zip` file.
