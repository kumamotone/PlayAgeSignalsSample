# Play Age Signals API Sample

A sample Android application demonstrating the [Play Age Signals API](https://developer.android.com/google/play/age-signals/use-age-signals-api).

## Overview

The Play Age Signals API provides information about the user's age verification status. This helps apps provide age-appropriate content and experiences based on user verification status.

## Features

- Request age signals using `AgeSignalsManager`
- Display user verification status
- Display install ID for tracking
- Handle various verification statuses
- Error handling with retry capability
- **Built-in testing UI** for simulating API responses

## Requirements

- Android Studio
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36
- Google Play Services

## Setup

### 1. Add Dependency

The Play Age Signals dependency is already configured in `gradle/libs.versions.toml`:

```toml
[versions]
playAgeSignals = "0.0.1-beta02"

[libraries]
play-age-signals = { group = "com.google.android.play", name = "age-signals", version.ref = "playAgeSignals" }
```

And in `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.play.age.signals)
    implementation(libs.kotlinx.coroutines.play.services)
}
```

### 2. Sync Gradle

Sync your project with Gradle files to download the dependencies.

## Usage

### Basic API Usage

```kotlin
// Create an instance of a manager
val ageSignalsManager = AgeSignalsManagerFactory.create(context)

// Request an age signals check
ageSignalsManager
    .checkAgeSignals(AgeSignalsRequest.builder().build())
    .addOnSuccessListener { ageSignalsResult ->
        // Store the install ID for later...
        val installId = ageSignalsResult.installId()

        when (ageSignalsResult.userStatus()) {
            UserStatus.VERIFIED -> {
                // User is verified as 18+
            }
            UserStatus.SUPERVISED -> {
                // User has supervised account
            }
            UserStatus.SUPERVISED_APPROVAL_DENIED -> {
                // Disallow access...
            }
            else -> {
                // Handle other cases
            }
        }
    }
    .addOnFailureListener { exception ->
        // Handle error
    }
```

## Testing

> **Note:** The Age Signals API requires the app to be distributed through Google Play to work properly. On local builds or emulators, you may receive an error or `UNKNOWN` status.

### Built-in Testing UI

This sample app includes a **Testing Settings** panel that allows you to simulate API responses without requiring a Play Store distribution. This is useful for:

- Testing different user statuses (VERIFIED, SUPERVISED, etc.)
- Testing error handling scenarios
- Developing and debugging your age verification flow

#### How to Use

1. Expand the **"🧪 Testing Settings"** panel in the app
2. Toggle **"Use Fake Manager"** to enable testing mode
3. Configure the simulated response:
   - **User Status**: Select the verification status to simulate
   - **Install ID**: Set a custom install ID for testing
   - **Simulate Error**: Select an error code to test error handling

When testing mode is active:
- A "🧪 Testing Mode Active" indicator appears at the top
- The "Check Age Signals" button shows "(Fake)" 
- Results show "(Fake)" or "(Simulated)" labels

#### Available User Statuses

| Status | Description |
|--------|-------------|
| `UNKNOWN` | User's verification status is unknown |
| `VERIFIED` | User is verified as 18 years or older |
| `SUPERVISED` | User has a supervised Google account with age set by a parent |
| `SUPERVISED_APPROVAL_PENDING` | User has a supervised account, and a significant change is pending parental approval |
| `SUPERVISED_APPROVAL_DENIED` | User has a supervised account, and a significant change was denied by the parent |

#### Simulatable Errors

| Code | Error | Description |
|------|-------|-------------|
| -1 | `API_NOT_AVAILABLE` | Age signals feature is not available |
| -2 | `NETWORK_ERROR` | Network error occurred |
| -6 | `TOO_MANY_REQUESTS` | Rate limited, retry later |
| -7 | `PLAY_SERVICES_OUTDATED` | Google Play Services needs update |
| -8 | `CLIENT_TRANSIENT_ERROR` | Temporary error, retry |
| -9 | `APP_NOT_OWNED` | App not installed from Google Play |
| -100 | `INTERNAL_ERROR` | Unknown internal error |

### Using FakeAgeSignalsManager (Official)

For unit testing, you can also use the official `FakeAgeSignalsManager` from the library:

```kotlin
val fakeResult = AgeSignalsResult.builder()
    .setUserStatus(AgeSignalsVerificationStatus.VERIFIED)
    .setInstallId("test-install-id")
    .build()

val manager = FakeAgeSignalsManager()
manager.setNextAgeSignalsResult(fakeResult)

manager.checkAgeSignals(AgeSignalsRequest.builder().build())
    .addOnSuccessListener { /* handle success */ }
    .addOnFailureListener { /* handle failure */ }
```

For more details, see the official documentation on [testing age signals](https://developer.android.com/google/play/age-signals/test-age-signals-api).

## Project Structure

```
app/src/main/java/dev/kuma/playagesignalssample/
├── MainActivity.kt          # Main UI with Compose + Testing Settings
├── AgeSignalsViewModel.kt   # ViewModel with fake mode support
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## References

- [Play Age Signals API Overview](https://developer.android.com/google/play/age-signals)
- [Use Age Signals API](https://developer.android.com/google/play/age-signals/use-age-signals-api)
- [Test Age Signals](https://developer.android.com/google/play/age-signals/test-age-signals-api)
- [Release Notes](https://developer.android.com/google/play/age-signals/release-notes)

## License

MIT
