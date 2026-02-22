# Development Guide

## Prerequisites

- **JDK 25** (Temurin recommended)
- **Android SDK** with compileSdk 36
- **Gradle 9.2.1** (via wrapper)

## Project Structure

```
kmp-app-updater/
├── core/                  # Headless KMP library (Android + Desktop JVM)
├── compose-ui/            # Compose Multiplatform UI components
├── sample/
│   ├── android/           # Android sample app
│   └── desktop/           # Desktop sample app
└── .github/workflows/
    ├── ci.yml             # Build + test on push/PR
    └── release.yml        # Publish to Maven Central + build sample binaries
```

## Build & Test

```bash
# Run all core tests (common + desktop + android host tests)
./gradlew :core:allTests

# Compile compose-ui
./gradlew :compose-ui:compileKotlinDesktop

# Build sample APK (release, signed with shared keystore)
./gradlew :sample:android:assembleRelease

# Build desktop distribution for current OS
./gradlew :sample:desktop:packageDistributionForCurrentOS

# Run desktop sample
./gradlew :sample:desktop:run
```

## Testing Strategy

### Unit Tests (`core/src/commonTest/`)

Tests run on both Desktop JVM and Android host via `allTests`. Key test files:

- **AppUpdaterTest** — full state-machine tests: check → download → install → reset, including mock v99 release end-to-end tests
- **ConfigValidationTest** — validates `AppUpdater` and `GitHubUpdateSource` constructor guards
- **ReleaseParsingTest** — JSON parsing of GitHub release payloads
- **VersionComparatorTest** — semantic version comparison logic

All tests use Ktor's `MockEngine` to simulate GitHub API responses without network access.

### Mock v99 Release

A pre-release `v99.0.0` exists on GitHub for integration testing. The sample apps use `includePreReleases = true` so they detect it. This lets you test the full update flow (check → download → install) without affecting real library releases.

To rebuild the v99 test APK locally:

```bash
# 1. Temporarily set versionCode=99, versionName="99.0.0" in sample/android/build.gradle.kts
# 2. Build
./gradlew :sample:android:assembleRelease
# 3. Upload to GitHub release
gh release upload v99.0.0 sample/android/build/outputs/apk/release/android-release.apk --clobber
# 4. Revert the version changes
```

### Instrumented Tests (`core/src/androidDeviceTest/`)

Device tests for `AndroidAssetDownloader` and `AndroidAssetInstaller`. Require a connected device or emulator:

```bash
./gradlew :core:connectedAndroidDeviceTest
```

## Signing

### Sample App Keystore

The sample Android app uses a shared release keystore (`sample/android/release.keystore`) for consistent signing across local and CI builds.

- **Alias:** `sample`
- **Passwords:** `samplepass`
- **Gitignored** — not committed to the repo
- **CI:** Stored as base64 in the `SAMPLE_KEYSTORE_BASE64` GitHub secret, decoded at build time

To regenerate the keystore locally:

```bash
keytool -genkeypair -v \
  -keystore sample/android/release.keystore \
  -alias sample -keyalg RSA -keysize 2048 -validity 36500 \
  -storepass samplepass -keypass samplepass \
  -dname "CN=KMP App Updater Sample, O=pavi2410"
```

Then update the CI secret:

```bash
base64 sample/android/release.keystore | gh secret set SAMPLE_KEYSTORE_BASE64
```

### Library Signing (Maven Central)

Library artifacts are signed with a GPG key for Maven Central publication.

- **Key ID:** last 8 chars stored in `SIGNING_KEY_ID` secret
- **Private key:** ASCII-armored, stored in `GPG_KEY_CONTENTS` secret
- **Passphrase:** stored in `SIGNING_PASSWORD` secret

Keys were generated via the Kotlin Gradle plugin:

```bash
./gradlew generatePgpKeys -Psigning.password="" --name "Name <email>"
./gradlew uploadPublicPgpKey --keyring /path/to/public_KEY_ID.asc
```

## CI/CD

### CI (`ci.yml`)

Runs on every push/PR to `main`:
1. Sets up Java 25 + Gradle
2. Runs `:core:allTests`
3. Uploads test reports as artifacts

### Release (`release.yml`)

Triggered by GitHub release events (released or prereleased):

```
test (gate)
  ├── publish (Maven Central)
  ├── build-sample-apk (+ upload to release)
  └── build-desktop × 3 OS (linux/windows/macos → deb/msi/dmg)
```

All jobs depend on the `test` job — if tests fail, nothing publishes.

### Publishing a New Version

1. Update `VERSION_NAME` in `gradle.properties` (remove `-SNAPSHOT`)
2. Commit and push to `main`
3. Create a GitHub release with tag `vX.Y.Z`
4. The workflow automatically:
   - Runs tests
   - Publishes `core` and `compose-ui` to Maven Central
   - Builds and attaches sample APK + desktop installers
5. Go to [Maven Central Deployments](https://central.sonatype.com/publishing/deployments) and click **Publish**
6. After release, bump `VERSION_NAME` to next `-SNAPSHOT`

## Android Resources

Android resource processing is **disabled by default** in both `core` and `compose-ui` modules (AGP 9 `com.android.kotlin.multiplatform.library` plugin). This is intentional — both modules are pure Kotlin/Compose with no Android XML resources, which improves build performance.

## Code Style

Spotless with ktlint is configured at the root level:

```bash
./gradlew spotlessCheck   # verify
./gradlew spotlessApply   # auto-fix
```
