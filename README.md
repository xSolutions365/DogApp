# DogApp

A Kotlin Multiplatform + Compose Multiplatform app for Android and iOS that brings together dog breed data from up to three public APIs. Dog CEO needs no key and is enough to run the app; TheDogAPI and API Ninjas add richer data when their keys are set. You can search breeds, read everything known about each one, keep favourites, and reveal a breed of the day.

UI, logic and storage are shared in one `shared` module. The platform apps are thin entry points.

## About this project

DogApp was built as a learning exercise for Kotlin Multiplatform and Compose Multiplatform, and for how each platform handles things like on-device ML, the camera, biometrics and payments. It's meant to be used as a playground, or as interview material where that's suitable. **It is never hooked to anything real**: there is no backend, no payment provider account and no real money.

## Features

- **Home:** a daily "breed of the day" card, and breed identification from a photo (taken with the camera or chosen from the library), classified on the device.
- **Explore:** breed search with filters for size, life span, group, energy, traits and favourites.
- **Breed detail:** a photo gallery, measurements, temperament, history and 1–5 trait meters, depending on which sources have data.
- **Profile:** favourite breeds, previous breeds of the day, a manual **Sync data**, and a **Feed me** section with mock dog charities, donations and a biometric-locked wallet.
- **Platform-native UI:** Material 3 on Android, Cupertino-style navigation and native UIKit sheets and menus on iOS.
- **Light and dark themes** from a custom design system (the font licences ship in `composeResources/files/licenses/`).

## Mock payments and security

These features use the real platform APIs around demo data, to show how each platform does them:
- **Charities are mock data**, hardcoded in `feature/donate/data/MockCharityRepository.kt`.
- **Wallet:** demo cards (brand, last 4 and expiry only) encrypted with an Android Keystore key or kept in an iOS Keychain item, unlocked with a biometric.
- **Donations** are approved locally by `DemoPaymentProcessor`. Google Pay (Android only) runs in Google's TEST environment, so **nothing is ever charged**.
- **Certificate pinning is report-only** and **on-device integrity checks** are only shown under Profile → About → **Device state**. Neither ever blocks the app.

**Playground shortcuts.** Some decisions only make sense because nothing here is real, and they're temporary stand-ins rather than patterns to copy. A Google Pay donation counts as successful whatever Google Pay returns (even an error or a closed sheet), so the flow can be tried on any device without a Google account or a merchant. The charities look real but are made up, so the donate flow can be shown end to end. The certificate pins are hardcoded root CA keys that only log a mismatch, so a CA rotation can't break the app; a real app would enforce them and plan for rotating pins. The integrity check compares the app's signature with the debug keystore's certificate, which the Android build reads at build time, so it works on any machine without a release key; a real app would verify integrity on a server (Play Integrity, App Attest). The free-tier API keys are built into the apps, which is fine for local builds but not for a published one.

## Data sources

| API | Used for | Auth |
|---|---|---|
| [TheDogAPI](https://www.thedogapi.com/en) | Breed facts, photos and galleries | API key |
| [API Ninjas Dogs](https://api-ninjas.com/api/dogs) | 1–5 trait ratings, size, life expectancy | API key |
| [Dog CEO](https://dog.ceo/dog-api) | Breed and sub-breed names, photo galleries | None |

Breeds are matched by normalised name, each source's rows are stored separately, and they're merged when read.

## Setup

### Requirements

- **JDK 17 or newer.**
- **Android Studio** with the Android SDK (compile and target SDK 37, min SDK 24).
- **Xcode** with the iOS simulator (the app needs iOS 18.2 or newer), on a Mac with Apple silicon.
- Gradle comes with the wrapper, so there's nothing to install.

### API keys (optional)

The app builds and runs without any keys, using Dog CEO alone. Get free keys from [TheDogAPI](https://www.thedogapi.com/en) and [API Ninjas](https://api-ninjas.com/api/dogs), then add them to `local.properties` in the project root. That file is gitignored.

```properties
dogapp.theDogApiKey=YOUR_THE_DOG_API_KEY
dogapp.apiNinjasKey=YOUR_API_NINJAS_KEY
```

The environment variables `DOGAPP_THE_DOG_API_KEY` and `DOGAPP_API_NINJAS_KEY` take precedence over `local.properties`. Never commit the keys.

### iOS signing

`iosApp/Configuration/Config.xcconfig` has an empty `TEAM_ID`. The simulator doesn't need one. To run on a real device, set your Apple team ID there.

## Running

- **Android:** use the `androidApp` run configuration, or `./gradlew :androidApp:assembleDebug` and install the APK.
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme. The `Shared` framework is built automatically.

## Checks

| Command | What it runs |
|---|---|
| `./gradlew staticAnalysis` | ktlint 1.8.0 with Compose rules on all modules, plus Android lint (warnings are errors) |
| `./gradlew :shared:testAndroidHostTest` | Shared tests on the JVM, Koin `verify()`, and the architecture rules test |
| `./gradlew :shared:iosSimulatorArm64Test` | Shared tests on iOS, plus Compose UI tests, Room integration tests and the full Koin graph |
| `./gradlew ktlintFormat` | Auto-fixes formatting |

## Architecture

Clean Architecture laid out as packages in the single `shared` module, under `com.example.dogapp`:

```
app/                 composition root: may depend on everything
  di/                Koin modules, initKoin, platform modules
  navigation/        destinations, per-tab back stacks, Navigator, platform transitions
  shell/             AppShell, Material navigation bar, Cupertino tab bar
core/
  domain/            pure Kotlin: models, repository interfaces, use cases, Outcome, errors
  data/              Ktor client, DataStore, Room database, report-only certificate pinning
  presentation/      theme, shared components, shared-element helpers, error text
feature/<name>/      home, explore, breeddetail, profile, identify, donate, wallet
  domain/  data/     a feature's own use cases and data, when it has any (explore, identify, donate, wallet)
  presentation/      Route, Entries, UiState, Action, ViewModel, Root + Screen
  di/                the feature's Koin module
```

**Layer rules** are enforced by `ArchitectureTest`, which checks the imports of every main source file:
- domain imports only `kotlin.*`, `kotlinx.coroutines.*` and other domain code
- data doesn't import presentation or `app`
- presentation doesn't import data or `app`
- core doesn't import features or `app`
- no feature imports another feature

**Conventions:**
- **Use cases** are plain classes with `operator fun invoke`. Results are `Outcome<Data, DomainError>`.
- **Screens** follow one pattern:
  - an immutable `UiState`
  - a sealed `Action` interface
  - a ViewModel exposing `state: StateFlow` and `onAction`
  - `XRoot` (gets the ViewModel from Koin) plus a stateless `XScreen` (previewable and tested)
- **Platform variants** come from `NavigationStyle` (Material on Android, Cupertino on iOS), passed down from `AppShell`. Truly native pieces sit behind `expect`/`actual`.
- **Features talk through ports.** The donate flow needs the wallet's cards, but no feature may import another. So `feature/donate` declares the `PaymentCards` port (list the saved cards, unlock, confirm one with a biometric), and `app/di/WalletPaymentCards` implements it over the wallet's `WalletCoordinator`.
- **Breed sources are plug and play.** Each API is a `BreedSource` (optionally also a `BreedImageSource`) bound with one line in `app/di/BreedModule.kt`. Removing that line removes the source and its stored rows. Sources that need a key are bound only when their key is set.

**Storage:** Room (`DogAppDatabase`, with auto-migrations; schemas in `shared/schemas/`) for breeds, favourites, daily draws and donations, and DataStore Preferences for small key-value data.

## Tools and libraries

| Area | Library | Version |
|---|---|---|
| Language | Kotlin Multiplatform | 2.4.21 |
| UI | Compose Multiplatform / Material 3 | 1.12.2 / 1.12.0-alpha03 |
| Navigation | JetBrains Navigation 3 (+ lifecycle-viewmodel-navigation3) | 1.1.2 (lifecycle 2.11.0) |
| DI | Koin (classic DSL, compose, compose-viewmodel) | 4.2.2 |
| Networking | Ktor (OkHttp on Android, Darwin on iOS) + kotlinx-serialization | 3.6.0 |
| Database | Room KMP + bundled SQLite (KSP) | 2.8.5 / 2.7.1 (KSP 2.3.12) |
| Key-value | DataStore Preferences | 1.2.1 |
| Images | Coil 3 (compose, network-ktor3) | 3.6.3 |
| Logging | Kermit (also routes Koin and Ktor logs) | 2.2.0 |
| Dates | kotlinx-datetime | 0.8.0 |
| On-device ML | LiteRT `CompiledModel` (Android) / Core ML + Vision (iOS) | 2.3.0 / system |
| Camera | CameraX (`camera-camera2`, `camera-lifecycle`, `camera-compose` viewfinder) (Android) / AVFoundation (iOS) | 1.6.2 / system |
| Google Pay (Android) | play-services-wallet + Compose pay button | 20.0.0 / 1.2.0 |
| Android core | androidx.core (`PackageInfoCompat` for the signing certificate) | 1.19.1 |
| Biometrics | androidx.biometric + androidx.fragment (Android) / LocalAuthentication + Keychain (iOS) | 1.1.0 / 1.9.1 / system |
| Coroutines | kotlinx-coroutines (+ test) | 1.11.0 |
| Build secrets | BuildKonfig | 0.23.0 |
| Static analysis | ktlint Gradle plugin + compose-rules | 14.2.0 (ktlint 1.8.0) / 0.6.8 |
| Build | AGP / Gradle | 9.4.1 / 9.8.1 |

All versions are in `gradle/libs.versions.toml`.

Material 3 is the only pre-release library, by choice. No stable `material3` exists for Compose Multiplatform after 1.9, so it stays on the version JetBrains pairs with our Compose Multiplatform release (listed in that release's changelog).

**Breed recognition model:** MobileNetV2 trained on ImageNet (Apache 2.0, from the TensorFlow models), shipped in `androidApp/src/main/assets/` and as `iosApp/iosApp/MobileNetV2.mlmodel`.

## Licence

DogApp is released under the [MIT License](LICENSE). The fonts are under the SIL Open Font License and the MobileNetV2 model under Apache 2.0, as noted above.
