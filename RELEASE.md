# Minimal Bitcoin Widget - Release & Internal Testing Guide

This guide provides the step-by-step procedure for preparing, compiling, signing, packaging symbols, and deploying an Android App Bundle (`.aab`) to the Google Play Console **Internal Testing** track.

---

## 1. Pre-Release Checklist

Before building the release bundle, ensure the following checklist is completed:

- [ ] **Working Tree & Git Status**:
  - All feature and fix commits are merged into the target release branch (e.g. `release/3.6.0`).
  - Working tree is clean: `git status`
- [ ] **Run Tests**:
  - Run local unit tests to ensure no regressions:
    ```bash
    ./gradlew test
    ```
- [ ] **Version Code & Version Name Bump** (`app/build.gradle.kts`):
  - `versionCode`: Increment to a strictly higher integer than any previously uploaded build on Google Play Console (e.g., `33` &rarr; `34`).
  - `versionName`: Update semantic version string if releasing a new version (e.g., `"3.6.0"`).
- [ ] **Localized Release Notes** (`release-notes.txt`):
  - Verify localized release notes are ready for all supported languages (`<en-US>`, `<es-ES>`, `<es-419>`, `<de-DE>`, `<fr-FR>`, `<it-IT>`, `<pt-PT>`, `<pt-BR>`, `<tr-TR>`).

---

## 2. Keystore & Signing Configuration

- **Keystore File**: `mbw-upload-keystore.jks`
  - Located in the project root or in `MBW - Drive/Secrets/mbw-upload-keystore.jks`.
  - Ignored by Git via `.gitignore`.
- **Key Alias**: `upload`
- **Keystore & Key Passwords**:
  - Stored securely in `MBW - Drive/Secrets/notes.txt` and the local untracked `notes.txt`.
  - **Never commit passwords to version control.**

---

## 3. Building & Signing the App Bundle (`.aab`)

1. **Compile the Release Bundle**:

   ```bash
   ./gradlew :app:bundleRelease
   ```

   The unsigned/raw bundle is created at `app/build/outputs/bundle/release/app-release.aab`.

2. **Sign the Bundle using `jarsigner`**:
   _(Note: Android App Bundles are signed using JDK `jarsigner`, unlike APK files which use `apksigner`)_

   ```bash
   jarsigner -keystore mbw-upload-keystore.jks \
     -storepass <KEYSTORE_PASSWORD> \
     -keypass <KEYSTORE_PASSWORD> \
     app/build/outputs/bundle/release/app-release.aab \
     upload
   ```

3. **Verify the Signature**:
   ```bash
   jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
   ```

---

### Or the Automated Helper Function (`export-mbw-bundle`)

For one-command compilation, signing, and archival to Google Drive, use the automated helper:

```bash
export-mbw-bundle
```

_(See [COMMANDS.md](COMMANDS.md#4-release-automated-bundle-export-helper) for the full function definition)_

---

## 4. Package Native Debug Symbols (Required for Play Console)

Because the project includes pre-compiled native libraries (`libandroidx.graphics.path.so` from Jetpack `androidx.graphics:graphics-path`), Google Play Console will flag a missing debug symbols warning unless you upload a companion `.zip`.

1. Run the symbol packaging helper from the repository root:

   ```bash
   package-mbw-symbols
   ```

   _(Or run manually)_:

   ```bash
   ./gradlew mergeReleaseNativeLibs
   VERSION_NAME=$(grep 'versionName =' app/build.gradle.kts | head -1 | sed -E 's/.*"([^"]+)".*/\1/')
   VERSION_CODE=$(grep 'versionCode =' app/build.gradle.kts | head -1 | tr -dc '0-9')
   (cd app/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib && zip -r "$OLDPWD/native-debug-symbols-v${VERSION_NAME}-${VERSION_CODE}.zip" arm64-v8a armeabi-v7a x86 x86_64 -x "*.DS_Store*")
   ```

2. This produces:
   ```text
   native-debug-symbols-v<versionName>-<versionCode>.zip
   ```
