# DogApp

A Kotlin Multiplatform + Compose Multiplatform app for Android and iOS that brings together dog breed data from up to three public APIs. Dog CEO needs no key and is enough to run the app; TheDogAPI and API Ninjas add richer data when their keys are set. You can search breeds, read everything known about each one, keep favourites, and reveal a breed of the day.

UI, logic and storage are shared in one `shared` module. The platform apps are thin entry points.

## Features

- **Home: breed of the day**
  - Once a day, tap the face-down **Reveal today's breed** card: it flips over in 3D, with a haptic, to show the breed. Tapping the breed card opens the breed detail.
  - Draws are random with no repeats and prefer breeds that have photos. Once every breed has been drawn, a new cycle starts.
  - Each draw is stored per local calendar day.
- **Home: identify a breed from a photo**
  - Home offers it the way each platform does, with a choice of **taking** or **choosing** a photo:
    - **Android:** an identify FAB that opens a Material 3 FAB menu with **Take photo** and **Choose photo** (Back closes it). The chosen item grows into the identify screen with a container transform.
    - **iOS:** an **Identify** button in the top-right toolbar (like **Edit** in Mail). It's a real UIKit button with a native pull-down menu, **Take Photo** and **Choose from Library**. Identify opens as a native page sheet (grabber, swipe down or **Done** to close). Tapping a match closes the sheet and opens the breed on the Home tab.
  - Identify opens straight into the camera or the picker, and offers both again: **Take a photo** / **Choose a photo**, then **Take another photo** / **Choose another photo**.
  - **Choosing** uses the system photo picker (Android Photo Picker, iOS `PHPickerViewController`; neither needs a permission).
  - **Taking** uses DogApp's own camera screen: a full-screen viewfinder with a shutter and a close button, built on CameraX on Android and AVFoundation on iOS. The photo is shown and classified just like a chosen one. On Android it's turned upright and scaled to at most 2048 px; iOS keeps the JPEG with its orientation.
  - **Camera permission:** the system asks the first time. If it's refused, the camera says "Camera access is off" with a way forward: Android offers **Allow camera** after the first refusal and **Open settings** (DogApp's App info) once it's permanently refused; iOS offers **Open settings** (DogApp's own Settings page). **Choose a photo** is always there. A device without a camera (the iOS simulator) says "This device has no camera".
  - The photo is classified on the device: LiteRT on Android, Core ML + Vision on iOS. Nothing is uploaded.
  - A clear winner (60% or more) is shown on its own; otherwise the top 3 candidates are listed with their match percentages. A photo with little dog in it shows "No dog found".
  - Tapping a match opens its breed detail. A recognised breed that isn't in the merged breed list (for example "Eskimo dog", or most breeds when only Dog CEO is used) is shown by name only, with no photo or details.
  - The model knows 118 dog breeds (the ImageNet dog classes), so other breeds can't be recognised.
- **Explore: breed search and filters**
  - Search matches the start of any word in a breed name across all merged breeds.
  - Filter chips under the search field: **Size** (Toy under 5 kg, Small 5–11, Medium 11–25, Large 25–45, Giant over 45, from the middle of the weight range), **Life span** (under 10, 10–12, 13+ years, from the middle of the range), **Group**, **Energy** (low, medium, high), **Traits** (good with children, low shedding, easy to train) and **Favourites**. Options within a filter can be combined; search and every active filter must all match. **Clear all** resets them.
  - The filter chips appear once the breeds have loaded. A filter's chip only shows when at least one breed has the info it needs, so filters with no data never appear. **Favourites** is always shown.
  - A breed sync with breeds already stored (Profile → About → **Sync data**) keeps the list and the filters on screen and updates them in place.
  - Breeds missing the info an active filter needs are hidden, and the list says how many and why (for example "22 breeds hidden: no size listed"). Energy and trait filters use the API Ninjas ratings, so they only cover breeds with ratings.
  - Each row shows a small favourite mark.
  - On first launch, a loading screen is shown and search stays disabled (and the filters hidden) until the breed database is filled.
- **Breed detail**
  - A photo gallery (Dog CEO and TheDogAPI) with a shared-element transition from the list.
  - Sections appear only when there is data for them: weight, height, life span, temperament, about, history, 1–5 trait meters (API Ninjas) and sources.
  - A heart toggles the breed as a favourite.
- **Profile**
  - **Favourite breeds.** If a favourite later disappears from the APIs, it's kept, shown by its saved name as "No longer available", and can be removed.
  - **Previous breeds of the day**, newest first. Today's draw joins the list once the day is over.
  - Both lists show at most two rows. With more, a "See all (5)" row ("See All (5)" on iOS) opens the full list.
  - **Feed me:**
    - **Support dog charities** opens a list of dog charities. Each one has a detail page with a **Donate** button: pick £3, £5, £10, £20 or another amount (£1 to £500), then pay with a saved card or, on Android, Google Pay.
    - **Donations** lists your donations, newest first, with how each was paid ("Paid with Visa •••• 4242", "Paid with Google Pay (Visa •••• 1111)"). With none yet, it links to the charities.
    - **Wallet** opens a wallet that only a biometric can unlock (fingerprint on Android, Face ID or Touch ID on iOS), where you can add and remove demo cards.
    - See [Mock payments and security setup](#mock-payments-and-security-setup).
  - **About** (at the bottom): **Device state** shows how many on-device integrity warnings there are ("1 warning", "No warnings") and opens a list of every check with its result. **Sync data** fetches every breed source again and shows the result under its title: "Last synced <date, time>" (stored, so it survives restarts), "Syncing…" with a spinner while it runs, "Synced, but <source> didn't respond" when only some sources failed, or "Couldn't sync. Tap to try again". Profile data (favourites, breed of the day history) isn't synced to an account yet; that's planned.
- **Images**
  - A dog placeholder sits under every photo while it loads.
  - Every photo is shown whole, scaled to fit its frame, over a soft, dimmed backdrop of the same photo that fills the rest of the frame. The backdrop is a tiny decode from the cache (no second download), blurred on iOS and Android 12+.
  - If a photo fails, it's retried once without the cache (this repairs broken cache entries) before moving on to the breed's next photo.
  - Breeds without a stored photo get one fetched during sync.
- **Platform-native navigation**
  - Each tab keeps its own back stack, and tapping the current tab again returns it to its root.
  - **Android:** Material 3 navigation bar, predictive back (from another tab's root, back returns to Home), and shared-axis transitions.
  - **iOS:** a Cupertino-style tab bar, an edge-swipe pop within a tab, and push/pop transitions with parallax.
- **Light and dark themes** from the custom "Park Day" design system:
  - colours: Leash Blue, Fur Tan and Tennis Ball
  - fonts: Bricolage Grotesque and Figtree (the OFL licences ship in `composeResources/files/licenses/`)

## Mock payments and security setup

DogApp is a demo playground for learning how payments and security work on each platform. **It is never hooked to anything real**: there is no backend, no payment provider account and no real money.
- **Charities are mock data.** The dog charities, their names, places and stories are made up and hardcoded in `feature/donate/data/MockCharityRepository.kt`. The app says so on the charity list.
- **The wallet is real platform security around demo contents.** The point is that a biometric unlocks a hardware-backed key, rather than just returning "succeeded":
  - **Android:** an AES-256-GCM key in the Android Keystore (StrongBox when available) that needs a Class 3 biometric on every use and is invalidated by a new enrolment. It's unlocked through `BiometricPrompt` with a `CryptoObject` (`androidx.biometric` 1.1.0, so `MainActivity` is a `FragmentActivity`). The encrypted wallet and its IV are stored in DataStore.
  - **iOS:** a Keychain item with `SecAccessControl(.biometryCurrentSet)`, readable only while a passcode is set and only on this device. It's read through `LAContext`, which shows Face ID or Touch ID. A DataStore flag records that a wallet exists, so an item wiped by a new enrolment shows as a reset, not as "never set up".
  - **States:** the prompt shows as soon as the wallet opens. A cancel leaves it locked with an Unlock button. A new biometric enrolment wipes the wallet and says so ("Wallet reset"). With no biometric enrolled, Android offers "Open settings" (the system enrolment flow) and iOS explains it in words, because iOS has no public link to the Face ID settings.
  - **Screen privacy**, only while the wallet screen is showing:
    - **Android:** `FLAG_SECURE` blanks screenshots, screen recordings and the Recents thumbnail, and `setRecentsScreenshotEnabled(false)` is set on Android 13+.
    - **iOS:** when the app goes inactive, a blur with the DogApp icon covers the window, so the app-switcher snapshot doesn't show the wallet. While the screen is recorded or mirrored (the `sceneCaptureState` trait), the wallet is hidden behind "Your wallet is hidden while the screen is being recorded."
  - **Auto-lock:** after 30 seconds or more in the background, the wallet is locked when the app comes back (`WalletAutoLock`, fed from the app lifecycle in `AppShell`). Nothing runs while the app is in the background.
  - **Cards, kept the tokenised way.** Real apps never store the card number (PCI DSS): a payment provider keeps the card, and the app keeps a reference with brand, last 4 and expiry. DogApp has no provider, so it keeps only those three:
    - **Validation** (`ValidateCardUseCase`, shared): any made-up number works as long as it has only digits and the right length for its brand (there's no Luhn checksum, on purpose); brand from the first digits (Visa, Mastercard, Amex, Discover, other) with that brand's number length and CVV length (4 for Amex); an expiry as `MM/YY` that hasn't passed (and isn't more than 20 years ahead). All errors show at once, and only on Save.
    - **The form:** the number, expiry and CVV have the Compose credit-card autofill content types, number keyboards and a masked CVV. They live only in `AddCardViewModel` state, are never logged, and are cleared on save. The form says it's a demo and suggests the test card 4242 4242 4242 4242.
    - **Adding:** Android: an "Add card" extended FAB opens a full-screen form with "Save card". iOS: an "Add Card" bar button opens a native page sheet with Cancel / Add.
    - **Removing:** Android: tap a card for its detail, then "Remove card". iOS: swipe a row left to "Remove" (also a VoiceOver action).
    - **Every change re-encrypts the wallet**, so saving or removing a card asks for the biometric ("Save card to your wallet" / "Remove card from your wallet"). A cancel changes nothing.
- **Donations are confirmed locally.** `DemoPaymentProcessor` stands in for a payment provider and approves every donation, except:
  - amounts outside £1 to £500
  - the test card ending `0002` (for example 4000 0000 0000 0002), which is declined so the error can be tried
- **Donating:**
  - **Android:** "Donate" opens a Material bottom sheet with amount chips, the saved cards and the Google Pay button, and a card donation closes it with a "Thank you" snackbar.
  - **iOS:** "Donate" opens a native page sheet with checkmark rows, which thanks you inside the sheet with Done.
  - **With a saved card:** a locked wallet first shows "Unlock wallet to pay with a card". "Donate £5" then asks for the biometric again ("Confirm your donation") as the payment confirmation, and reads the card fresh from the wallet. With no cards, "Add a card in your wallet" leads to the wallet.
  - **Google Pay (Android only)** runs in Google's TEST environment (`WalletConstants.ENVIRONMENT_TEST`) with the `example` gateway, so a test token comes back and **nothing is ever charged**. No merchant account is needed. The button shows only when Google Pay says it's ready. The Google Pay sheet needs a device or emulator with Play services that is signed in to a Google account.
  - **A Google Pay donation never fails.** Whatever Google Pay returns counts as a successful demo payment: a completed sheet, an error, a closed sheet, and a device without a Google account (Google shows "Google Pay requires a Google account" and then reports a cancel, which the app can't tell apart from closing the sheet). The donation is recorded as paid with Google Pay, and a Toast says "Demo payment: nothing was taken from your account." instead of the snackbar.
  - **Apple Pay isn't available.** It needs a Merchant ID entitlement, which needs a paid Apple developer account, so iOS offers saved cards only.
  - **Store rules**, for reference: Apple lets only approved nonprofits take donations in-app (and they must offer Apple Pay), and Google Play doesn't allow Play Billing for tax-exempt donations. Neither applies here, because nothing is real.
- **The donation history** is kept on the device in Room (the `donation` table): charity, amount, how it was paid and when. No card number is ever stored.
- **Certificate pinning is report-only, on purpose.** The API hosts are pinned to their root CA keys (SPKI SHA-256), each with a backup root from the same CA:
  - `api.thedogapi.com` and `dog.ceo`: GTS Root R4, backup GTS Root R1 (Google Trust Services)
  - `api.api-ninjas.com`: Amazon Root CA 1, backup Amazon Root CA 3
  - Image hosts aren't checked, because a CDN can change its CA at any time.
  - **How:** both platforms read the chain the system has already validated, so it ends at the trusted root. Android: an OkHttp network interceptor (`PinReportingInterceptor`) reads the handshake's certificates. iOS: the Ktor Darwin engine's `handleChallenge` (`pinReportingChallengeHandler`) evaluates the server trust and reads `SecTrustCopyCertificateChain`, then lets the system handle the challenge as usual. The pin of each certificate comes from its DER in shared code (`spkiPin`), and `CertificatePinReporter` compares them with `PinnedHosts`.
  - **A mismatch never fails a request.** It's logged once per host per app run as a warning under the `CertificatePins` tag, with the chain's pins and the expected ones. An enforcing app would cancel the request instead (OkHttp `CertificatePinner`, Android `<pin-set>` or iOS `NSPinnedDomains`), and would need a plan for rotating pins.
  - **To see a report:** change one host's pins in `core/data/security/PinnedHosts.kt` and run a fresh install, so the first sync calls every API. The warning appears in Logcat or the Xcode console, and the data still loads.
- **On-device integrity checks are weak on purpose and never block.** They show what each platform can look at locally, under About → Device state:
  - **Android:** root or Magisk files (`su` binaries, Magisk folders), a `test-keys` build, the app's signing certificate, an emulator (`goldfish`/`ranchu` hardware, SDK products and fingerprints), and an attached debugger.
  - **iOS:** jailbreak files (Cydia, Sileo, Substrate, rootless `/var/jb` and similar; generic Unix paths are left out because the simulator sees the Mac's disk), a write outside the sandbox, the simulator, and an attached debugger (`sysctl` `P_TRACED`).
  - **The expected signing certificate** is read at build time from the key that signs the build (today the debug keystore, so it works on every machine) and passed in as `BuildConfig.SIGNING_CERT_SHA256`. A re-signed APK shows "App signature: Warning". On a brand-new machine, the very first build runs before the debug keystore exists, so it shows the warning until the next build.
  - **Why it stays a warning:** anything that runs on the device can be patched or hidden (Frida, Magisk's hiding, a modified binary). Real integrity needs a server: Play Integrity tokens are decoded with Google's service credentials, and App Attest needs a paid Apple account plus a server to verify the attestation. DogApp has neither, so the results are only shown, labelled easy to bypass.
  - **Expected results:** on the Android emulator, "Emulator: Warning"; on the iOS simulator, "Simulator: Warning"; plus "Debugger: Warning" while a debugger is attached.
- **Testing on devices:** Android emulator: set a screen lock and add a fingerprint in Settings, using `adb -e emu finger touch 1` to touch the sensor (another id fails). iOS simulator: Features → Face ID → Enrolled, then Matching / Non-matching Face. Untick and tick Enrolled again to simulate a new enrolment.

## Data sources

| API | Used for | Auth |
|---|---|---|
| [TheDogAPI](https://www.thedogapi.com/en) (`https://api.thedogapi.com/v1`) | Breed facts (size, life span, temperament, origin, description, history, group), photos and galleries | API key, `x-api-key` header |
| [API Ninjas Dogs](https://api-ninjas.com/api/dogs) (`https://api.api-ninjas.com/v1`) | 1–5 trait ratings, size, life expectancy, a photo (paged 20 at a time) | API key, `X-Api-Key` header |
| [Dog CEO](https://dog.ceo/dog-api) (`https://dog.ceo/api`) | Breed and sub-breed names, photo galleries | None |

**Dog CEO is enough to run the app.** It's always used. TheDogAPI and API Ninjas are each used only when their key is set, and each one adds its data on top:
- **Dog CEO only:** breed names, photos and galleries. Detail screens show the photos and sources; Explore offers search and the Favourites filter; the other filter chips don't appear because no breed has their info.
- **With TheDogAPI:** sizes, life spans, temperament, about, history, groups, and more photos.
- **With API Ninjas:** 1–5 trait ratings (and the Energy and Traits filters), plus sizes and life spans for the breeds it covers.

**How the sources are merged:**
- Breeds are matched by normalised name: lowercase, without diacritics or punctuation, and with sub-breeds written as `"<sub-breed> <breed>"`.
- Dog CEO and API Ninjas also have explicit alias tables.
- Each source's rows are stored separately and merged when read. For each field, the first source with a value wins, in the order TheDogAPI → API Ninjas → Dog CEO. Photos are the union of all sources.
- Measurements are always shown in kg and cm.
- If a source fails during sync, it keeps the data from its last successful sync.
- If a source's key is removed, the next sync deletes that source's stored rows.

**Limits:** the keys are free-tier client keys, and they end up inside the built apps. Before any release, check TheDogAPI's free-plan limits and API Ninjas' commercial-use terms; commercial use of API Ninjas needs a premium plan.

## Setup

### Requirements

- **JDK 17 or newer.** AGP 9 and Gradle 9 need it; the code targets JVM 11 bytecode.
- **Android Studio** with the Android SDK (compile and target SDK 37, min SDK 24).
- **Xcode** with the iOS simulator (the app needs iOS 18.2 or newer), on a Mac with Apple silicon. The iOS targets are `iosArm64` and `iosSimulatorArm64`.
- Gradle comes with the wrapper (Gradle 9.8.1), so there's nothing to install.

### API keys (optional)

The app builds and runs without any keys, using Dog CEO alone. Each key you add turns on one more source (see [Data sources](#data-sources)); you can add either one or both. Get free keys from [TheDogAPI](https://www.thedogapi.com/en) and [API Ninjas](https://api-ninjas.com/api/dogs), then add them to `local.properties` in the project root. That file is gitignored.

```properties
dogapp.theDogApiKey=YOUR_THE_DOG_API_KEY
dogapp.apiNinjasKey=YOUR_API_NINJAS_KEY
```

For CI, use environment variables instead; they take precedence over `local.properties`. A missing or blank value means that source is off (so an empty environment variable also turns off a key set in `local.properties`):

```bash
export DOGAPP_THE_DOG_API_KEY=...
export DOGAPP_API_NINJAS_KEY=...
```

At build time, BuildKonfig generates them into `com.example.dogapp.core.data.config.BuildSecrets` as nullable values, and `app/di/BreedModule.kt` registers a keyed source only when its key is set. Never commit the keys.

### iOS signing

`iosApp/Configuration/Config.xcconfig` has an empty `TEAM_ID`. The simulator doesn't need one. To run on a real device, set your Apple team ID there.

## Running

- **Android:** use the `androidApp` run configuration, or `./gradlew :androidApp:assembleDebug` and install the APK.
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme. The `Shared` framework is built automatically.

On first launch the app syncs every source that's turned on; that takes a few seconds. After that, breeds come from the local database until you tap Profile → About → **Sync data**. There's no scheduled background sync yet.

## Checks

| Command | What it runs |
|---|---|
| `./gradlew staticAnalysis` | ktlint 1.8.0 with Compose rules on all modules, plus Android lint (warnings are errors) |
| `./gradlew :shared:testAndroidHostTest` | Shared tests on the JVM, Koin `verify()`, and the architecture rules test |
| `./gradlew :shared:iosSimulatorArm64Test` | Shared tests on iOS, plus Compose UI tests, Room integration tests and the full Koin graph |
| `./gradlew ktlintFormat` | Auto-fixes formatting |

Compose UI tests and Room tests run in `iosTest`. Android host tests can't create a `Context`, and the iOS simulator runs Compose UI tests without a device.

## Architecture

Clean Architecture laid out as packages in the single `shared` module, under `com.example.dogapp`:

```
app/                 composition root: may depend on everything
  di/                Koin modules, initKoin, platform modules
  navigation/        destinations, per-tab back stacks, Navigator, platform transitions
  shell/             AppShell, Material navigation bar, Cupertino tab bar
  logging/           Kermit setup
  image/             Coil ImageLoader
core/
  domain/            pure Kotlin: models, repository interfaces, use cases, Outcome, errors
    breed/  favourite/  draw/  security/
  data/              Ktor client, DataStore, Room database, report-only certificate pinning
    breed/{thedogapi,apininjas,dogceo,local}/  favourite/  draw/  donation/  security/
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
- **Navigation**
  - Features expose `EntryProviderScope<NavKey>.xEntries(...)`.
  - Navigation is passed in as lambdas from `app`.
  - Every route is `@Serializable` and registered in that feature's serializers module.
- **Platform variants** come from `NavigationStyle` (Material on Android, Cupertino on iOS), passed down from `AppShell`. Features expose their variants as plain parameters (`HomeLayout`, `IdentifyPresentation`, `WalletLayout`, `DonatePresentation`), and `app` maps the style onto them, so iOS UI tests render both. Truly native pieces sit behind `expect`/`actual` in `app`: `rememberIdentifyLauncher` pushes the identify screen on Android and presents a `UISheetPresentationController` hosting a `ComposeUIViewController` on iOS (both take a `PhotoSource`, Camera or Library, which Identify opens first), and `rememberDonateLauncher` shows a Material bottom sheet (Android, and the Material style on iOS) or a UIKit page sheet (iOS). Google Pay is `rememberGooglePayLauncher` in `feature/donate`: a real launcher on Android and `null` on iOS.
- **Features talk through ports.** The donate flow needs the wallet's cards, but no feature may import another. So `feature/donate` declares the `PaymentCards` port (list the saved cards, unlock, confirm one with a biometric), and `app/di/WalletPaymentCards` implements it over the wallet's `WalletCoordinator`.
- **Breed sources are plug and play.** Each API is a `BreedSource` (optionally also a `BreedImageSource`) bound with one line in `app/di/BreedModule.kt`. Removing that line removes the source and its stored rows. Sources that need a key are bound only when their key is set.
- **Breed syncs** are owned by the app-wide `BreedSyncCoordinator`. The first one starts at launch, Home and Explore share it, and it syncs only when the database is empty. `syncNow()` forces a sync (Retry on Home and Explore, and Profile's **Sync data**). Each successful sync stores its time through `LastBreedSyncStore`.

**Storage:**
- **Room** (`DogAppDatabase`, version 5, with auto-migrations; schemas in `shared/schemas/`). Tables:
  - `sourced_breed`: each source's breed data, which the sync replaces
  - `favourite_breed`: owned by the user, never touched by sync
  - `daily_draw`: one row per day; the first draw of a day is the one kept
  - `donation`: the donation history, owned by the user (charity, amount in pence, how it was paid, when)
- **DataStore Preferences** (`dogapp.preferences_pb`) holds small key-value data: the wallet (the encrypted wallet on Android, and the "wallet exists" flag on iOS) and the last breed sync time (`last_breed_sync`).

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

## Useful notes

- **Logging:** debug builds log to Logcat or OSLog under the `DogApp` tag (Koin under `Koin`, HTTP under `HttpClient`, certificate pin reports under `CertificatePins`). Release builds log nothing.
- **Known warnings:**
  - Kotlin's "expect/actual classes are in Beta" warning on the Room-generated `DogAppDatabaseConstructor`
  - AGP's `Configuration.setVisible` deprecation
  - Both are left visible on purpose; neither is suppressed.
- **Live network in iOS tests:** the iOS simulator *test* process gets `NoConnection` for real requests, while the simulator *app* doesn't. Check live API behaviour in the running app; the tests use Ktor's `MockEngine` and fakes.
- **Koin in Compose UI tests:** in Koin 4.2.2, the `KoinApplication` composable starts a global Koin context and doesn't stop it when disposed. Tests that use it must call `stopKoin()` between runs (see `AppShellTest`). Otherwise later runs silently reuse the first run's modules.
- **Koin `verify()`** doesn't see `get()` calls inside definition lambdas. That's why `KoinGraphTest` on iOS also resolves the whole graph against the real platform module.
- **Breed recognition model:** MobileNetV2 trained on ImageNet (Apache 2.0, from the TensorFlow models). Android ships the float TFLite model and its 1,001 labels in `androidApp/src/main/assets/` (stored uncompressed); iOS ships Apple's FP16 Core ML conversion as `iosApp/iosApp/MobileNetV2.mlmodel`, which Xcode compiles into the app. The two builds of the model give slightly different scores for the same photo. Core ML labels list synonyms after a comma (`"Maltese dog, Maltese terrier, Maltese"`), so only the first name is used. The 118 dog labels are matched to breeds by normalised name, plus an alias table (`imageNetBreedAliases`).
- **Camera:**
  - The Android emulator's back camera is a virtual 3D room (`hw.camera.back=virtualscene`); move around it with the emulator's camera controls. The first frames can take a few seconds. Until the camera opens, a spinner shows over the viewfinder and the shutter is disabled (a Material spinner on Android, the native `UIActivityIndicatorView` on iOS).
  - The iOS simulator has no camera, so it shows the no-camera state; capturing needs a real iPhone (a free Apple ID can sign a personal device build).
  - `NSCameraUsageDescription` is in `iosApp/iosApp/Info.plist`; Android declares `CAMERA` plus `android.hardware.camera.any` as not required, so devices without a camera can still install the app.
  - On iOS, the camera and the photo picker wait for any running presentation (such as the Identify sheet sliding up) before presenting, because UIKit rejects a presentation during another one.
- **Native UIKit views and tests:** Compose's iOS UI tests can't host a `UIKitView` (the interop container they need is internal), so the native pull-down button (`IdentifyMenuButton`) reaches Home through a slot that `AppShell` fills by default, and tests pass a Compose stand-in. Interop views are drawn behind the Compose canvas, so the button takes the screen's background colour to blend in.
- **LiteRT manifest entries:** LiteRT 2.3.0 brings Play services and WorkManager (with their `WAKE_LOCK`, `ACCESS_NETWORK_STATE` and `RECEIVE_BOOT_COMPLETED` permissions), and declares two foreground-service permissions for downloading models. The app bundles its model, so `AndroidManifest.xml` removes the two foreground-service permissions.
- **Google Pay button:** the Compose pay button (`com.google.pay.button:compose-pay-button`, on Maven Central) brings the Compose Material 2 library at runtime, and its Play services view logs `ThemeUtils: … is an AppCompat widget that can only be used with a Theme.AppCompat theme` because the app theme isn't AppCompat. The button still renders correctly.
- **Day boundary:** "today" is the device's local calendar date. Changing the device clock can reveal an extra breed, which is acceptable for this feature.
