# Development Guide

## Prerequisites

- **JDK 21+** (Temurin recommended)
- **Android SDK** with compileSdk 36
- **Amper 0.10.0** (via wrapper — `./amper`)

## Project Structure

```
kmp-app-updater/
├── core/                  # Headless KMP library (Android + Desktop JVM)
├── compose-ui/            # Compose Multiplatform UI components
├── sample-android/        # Android sample app
├── sample-desktop/        # Desktop sample app
├── project.yaml           # Amper project definition
└── .github/workflows/
    ├── ci.yml             # Build + test on push/PR
    └── release.yml        # Publish to Maven Central + build sample binaries
```

## Build & Test

```bash
# Run all tests
./amper test

# Run tests for a specific platform only
./amper test -p jvm
./amper test -p android

# Build a specific module
./amper build -m core
./amper build -m compose-ui

# Build sample APK (release variant)
./amper package -m sample-android -v release

# Build desktop distribution for current OS
./amper package -m sample-desktop

# Run desktop sample
./amper run -m sample-desktop
```

## Testing Strategy

### Unit Tests (`core/test/`)

Tests run on both Desktop JVM and Android host. Key test files:

- **AppUpdaterTest** — full state-machine tests: check → download → install → reset, including mock v99 release end-to-end tests
- **ConfigValidationTest** — validates `AppUpdater` and `GitHubUpdateSource` constructor guards
- **ReleaseParsingTest** — JSON parsing of GitHub release payloads
- **VersionComparatorTest** — semantic version comparison logic

All tests use Ktor's `MockEngine` to simulate GitHub API responses without network access.

### Mock v99 Release

A pre-release `v99.0.0` exists on GitHub for manual integration testing. The sample apps use `includePreReleases = true` so they detect it. This lets you test the full update flow (check → download → install) without affecting real library releases.

**Important:** Both the installed app and the v99 APK must be signed with the same keystore. The shared `sample-android/release.keystore` ensures this.

To rebuild the v99 test APK locally:

```bash
# 1. Temporarily set versionCode=99, versionName="99.0.0" in sample-android/module.yaml
# 2. Build
./amper package -m sample-android -v release
# 3. Upload to GitHub release
gh release upload v99.0.0 build/tasks/_android_packageRelease/android-release.apk --clobber
# 4. Revert the version changes
```

## Signing

### Sample App Keystore

The sample Android app uses a shared release keystore (`sample-android/release.keystore`) for consistent signing across local and CI builds.

- **Alias:** `sample`
- **Passwords:** `samplepass`
- **Gitignored** — not committed to the repo
- **CI:** Stored as base64 in the `SAMPLE_KEYSTORE_BASE64` GitHub secret, decoded at build time

To regenerate the keystore locally:

```bash
./amper tool generate-keystore
```

Or manually with `keytool`:

```bash
keytool -genkeypair -v \
  -keystore sample-android/release.keystore \
  -alias sample -keyalg RSA -keysize 2048 -validity 36500 \
  -storepass samplepass -keypass samplepass \
  -dname "CN=KMP App Updater Sample, O=pavi2410"
```

Then update the CI secret:

```bash
base64 sample-android/release.keystore | gh secret set SAMPLE_KEYSTORE_BASE64
```

### Library Signing (Maven Central)

Library artifacts are signed with a GPG key for Maven Central publication.

- **Key ID:** last 8 chars stored in `SIGNING_KEY_ID` secret
- **Private key:** ASCII-armored, stored in `GPG_KEY_CONTENTS` secret
- **Passphrase:** stored in `SIGNING_PASSWORD` secret

## CI/CD

### CI (`ci.yml`)

Runs on every push/PR to `main`:
1. Sets up Java 21 + Amper
2. Runs `./amper test`
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

1. Update `versionName` in `sample-android/module.yaml` and project version metadata (remove `-SNAPSHOT`)
2. Commit and push to `main`
3. Create a GitHub release with tag `vX.Y.Z`
4. The workflow automatically:
   - Runs tests
   - Publishes `core` and `compose-ui` to Maven Central
   - Builds and attaches sample APK + desktop installers
5. Go to [Maven Central Deployments](https://central.sonatype.com/publishing/deployments) and click **Publish**
6. After release, bump the version to next `-SNAPSHOT`
