<div align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="120" alt="AIRstix icon" />

  # AIRstix

  **Turn your Android phone into a wireless gamepad for PC.**

  AIRstix connects to the [AIRstix server](https://github.com/Amarthya-sg/AIRstix-server) over your local Wi-Fi and streams real-time gamepad input using a low-latency TCP connection with binary serialization.

  ![Android](https://img.shields.io/badge/Android-API%2026%2B-brightgreen?logo=android)
  ![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blueviolet?logo=kotlin)
  ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue?logo=jetpackcompose)
  ![Version](https://img.shields.io/badge/Version-0.4.2-cyan)
  ![License](https://img.shields.io/badge/License-GPL--3.0-red)
</div>

---

## Screenshots

<div align="center">

| Main Menu | Gamepad |
|:-:|:-:|
| <img src="Images/01_main_menu.jpg" width="420" alt="Main Menu"/> | <img src="Images/04_gamepad.jpg" width="420" alt="Live Gamepad"/> |

| Connect Screen | Layout Profiles |
|:-:|:-:|
| <img src="Images/05_connect.jpg" width="420" alt="Connect Screen"/> | <img src="Images/03_layout_profiles.jpg" width="420" alt="Layout Profiles"/> |

| Settings |
|:-:|
| <img src="Images/02_settings.jpg" width="420" alt="Settings"/> |

</div>

---

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Getting Started](#getting-started)
- [Connecting to a Server](#connecting-to-a-server)
- [Gamepad Layout](#gamepad-layout)
- [Customization](#customization)
- [Settings](#settings)
- [Building for Release](#building-for-release)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Permissions](#permissions)
- [License](#license)

---

## Features

### Controller
- **Full Xbox-style layout** — dual analog sticks, D-Pad, A/B/X/Y face buttons, LT/RT triggers, LB/RB shoulder buttons, Select (−), Start (+), Home, Capture
- **Grouped or ungrouped controls** — toggle face buttons and D-Pad between grouped and individual button modes
- **Persistent connection** — the session stays alive while navigating between screens (Settings, Main Menu) without dropping

### Connection
- **Wireless over TCP/IP** — streams input to the AIRstix server on your local Wi-Fi network
- **QR code quick pair** — scan the QR code shown by the server to connect instantly, no typing required
- **Manual entry fallback** — enter IP address and port directly if QR is unavailable
- **Connection diagnostics** — step-by-step checks: Wi-Fi state → local IP → subnet reachability → ping → port availability

### Customization
- **Visual layout editor** — drag every control to any position on a live dotted-grid canvas
- **Per-button properties** — visibility, scale, opacity, offset, and anchor point (TL / TR / BL / BR) per control
- **Layout profiles** — save, switch, create, import, and export named layouts as JSON files
- **Default Layout** — always available as a factory-reset baseline

### Display & Feel
- **HUD / sci-fi aesthetic** — corner bracket decorations, monospace typography, neon accent palette
- **Seven accent colors** — Red, Green, Blue, Yellow, Purple, Orange, Pink — each with neon (dark) and glossy (light) rendering
- **Full-screen mode** — hides system bars for a clean controller view, enabled by default
- **Haptic feedback** — configurable vibration on every button press with Soft / Medium / Strong intensity
- **Landscape-locked** — activity stays in landscape orientation at all times

### Performance
- **Configurable polling rate** — 10 ms to 500 ms (default 80 ms); lower = faster input, higher CPU cost
- **Low-latency socket tuning** — `TCP_NODELAY` + `IPTOS_LOWDELAY` on every connection
- **Save connection credentials** — optionally remembers last-used IP and port across sessions

---

## How It Works

The app opens a TCP socket to the AIRstix server. Gamepad state (button presses, analog stick positions, trigger values) is serialized using [Colfer](https://github.com/pascaldekloe/colfer) binary encoding and sent at the configured polling interval.

### Wire Format

Defined in [`VGP_Data_Exchange/GamePadReading.colf`](VGP_Data_Exchange/GamePadReading.colf), modeled after the [Windows GamepadReading API](https://learn.microsoft.com/en-us/uwp/api/windows.gaming.input.gamepadreading):

```
package VGP_Data_Exchange

type GamepadReading struct {
    ButtonsUp         uint32   // bitmask — buttons released this frame
    ButtonsDown       uint32   // bitmask — buttons held this frame
    LeftTrigger       float32  // [0.0, 1.0]
    RightTrigger      float32  // [0.0, 1.0]
    LeftThumbstickX   float32  // circular [-1.0, 1.0]
    LeftThumbstickY   float32  // circular [-1.0, 1.0]
    RightThumbstickX  float32  // circular [-1.0, 1.0]
    RightThumbstickY  float32  // circular [-1.0, 1.0]
}
```

Thumbstick values are circular — `(0.7, 0.7)` is a valid corner; `(1.0, 1.0)` is out of range. Button bits follow the [Windows GamepadButtons enum](https://learn.microsoft.com/en-us/uwp/api/windows.gaming.input.gamepadbuttons).

The `VGP_Data_Exchange` directory is a git submodule providing generated Java and C sources from the schema.

---

## Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| JDK | 21 (Temurin recommended) |
| Android Studio | Latest stable |
| Android device / emulator | API 26+ (Android 8.0+) |
| AIRstix server | Running on the same local network |

### Clone

```bash
git clone --recurse-submodules https://github.com/Amarthya-sg/AIRstix.git
```

If you already cloned without `--recurse-submodules`:

```bash
git submodule update --init --recursive
```

### Build

```bash
./gradlew assemble
```

### Install on a connected device

```bash
./gradlew installDebug
```

Or open the project in Android Studio and run it directly.

---

## Connecting to a Server

1. Start the [AIRstix server](https://github.com/Amarthya-sg/AIRstix-server) on your PC.
2. Make sure both devices are on the same Wi-Fi network.
3. Open the app — the Main Menu shows **NOT CONNECTED** in the status bar at the top.
4. Tap **▶ Start** in the center hub.
5. Either:
   - **01 · Quick Pair** — tap **Scan QR Code** and scan the QR shown by the server, or
   - **02 · Manual Entry** — type the IP address and port, then tap **Connect →**
6. On success the app returns to the Main Menu showing **CONNECTED**. Tap **▶ Start** again to enter the live gamepad screen.

> If the connection fails, a **Run Diagnostics** option walks through each network check step by step.

---

## Gamepad Layout

| Zone | Controls |
|---|---|
| Top center | LB (LSHLDR), RB (RSHLDR) shoulder buttons |
| Center | Select (−), Home (○), Home (⌂), Start (+), Settings (⚙) |
| Left | Left Analog Stick, LT trigger |
| Bottom left | D-Pad (Up / Down / Left / Right) |
| Right | A / B / X / Y face buttons, RT trigger |
| Bottom right | Right Analog Stick |

---

## Customization

Open **CUSTOMIZE** from the Main Menu to enter the visual layout editor.

- **Drag** any control freely across the dotted-grid canvas.
- **Double-tap** a control to open its property panel — toggle visibility, adjust scale and opacity, set anchor point.
- **Group / Ungroup** face buttons or D-Pad to move them as a unit or individually.
- **Preview** the layout with the eye (👁) button.

### Options menu

| Action | Description |
|---|---|
| Save | Persist the current layout |
| Import Config | Load a layout from a `.json` file |
| Export Config | Write the current layout to a `.json` file |
| Reset Defaults | Restore the factory layout |

### Layout Profiles

Tap **PROFILE** from the Main Menu to open the profile dialog.

- **Default Layout** is always present and cannot be deleted.
- Saved or imported configs appear as named profiles (e.g. `1b`, `2b`).
- Tap a profile to load it instantly — a snackbar confirms "Default Layout loaded".
- Tap the 🗑 icon to delete a custom profile.
- Tap **Create Profile** to save the current layout under a new name.

---

## Settings

Open **SETTINGS** from the Main Menu.

| # | Section | Setting | Default | Notes |
|---|---|---|---|---|
| 01 | Display | Theme Color | Blue | Red / Green / Blue / Yellow / Purple / Orange / Pink |
| 01 | Display | Polling Interval (ms) | 80 | Range: 10–500 ms |
| 02 | Behavior | Remember IP Address and Port | Off | Saves last-used credentials |
| 02 | Behavior | Haptic Feedback (Vibrations) | Off | Vibrate on button press |

> When Haptic Feedback is enabled a Haptic Intensity selector appears: **Soft / Medium / Strong**.

Use **Save** to apply changes, **Reset** to restore defaults, or **Cancel** to discard.

---

## Building for Release

Create `signing.properties` in the project root — this file is gitignored and must never be committed:

```properties
STORE_FILE=/absolute/path/to/your.keystore
STORE_PASSWORD=your_store_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

A template is provided at [`signing.properties.template`](signing.properties.template).

Build the signed release APK:

```bash
./gradlew assembleRelease
```

If `signing.properties` is absent, the build falls back to the debug signing config automatically.

---

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device or emulator)
./gradlew connectedCheck

# Lint
./gradlew lint

# Everything together (mirrors CI)
./gradlew build lint test
```

### Test Coverage

| Suite | Location | What it covers |
|---|---|---|
| Unit | `app/src/test/` | `NetworkDiagnostics` subnet logic |
| Instrumented E2E | `app/src/androidTest/` | Connection lifecycle, gamepad input, navigation, settings |

E2E tests use `TestGamepadServer` — a loopback TCP server that deserializes incoming `GamepadReading` frames for assertion.

### CI

GitHub Actions runs on every push and pull request:

- **Job 1** — Build, lint, unit tests
- **Job 2** — Instrumented tests on API **26**, **34**, and **36** in parallel via [Android Emulator Runner](https://github.com/ReactiveCircus/android-emulator-runner)

---

## Project Structure

```
.
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/io/github/amarthyasg/airstix/
│       │       ├── MainActivity.kt                    # Entry point & navigation graph
│       │       ├── data/
│       │       │   ├── BaseColor.kt                   # Accent color enum + color resolution
│       │       │   ├── ButtonComponent.kt             # ButtonComponent, ButtonConfig, ButtonAnchor
│       │       │   ├── ColorScheme.kt
│       │       │   ├── Defaults.kt                    # Default settings & button layout configs
│       │       │   ├── HapticIntensity.kt
│       │       │   ├── Preview.kt                     # Compose preview helpers
│       │       │   └── SettingsRepository.kt          # DataStore-backed settings persistence
│       │       ├── network/
│       │       │   ├── ConnectionState.kt             # UI state model
│       │       │   ├── ConnectionViewModel.kt         # TCP socket, command queue
│       │       │   ├── ConnectionViewModelFactory.kt
│       │       │   ├── NetworkCommand.kt              # Sealed command types
│       │       │   └── NetworkDiagnostics.kt          # Wi-Fi / IP / subnet / ping / port checks
│       │       └── ui/
│       │           ├── components/
│       │           │   └── QRCodeScanner.kt           # ZXing QR scanner wrapper
│       │           ├── composables/
│       │           │   ├── AnalogStick.kt
│       │           │   ├── BoundedNumericInput.kt
│       │           │   ├── ButtonConfigEditor.kt
│       │           │   ├── CentralButtons.kt          # LB/RB, −, ○, ⌂, +, ⚙
│       │           │   ├── Circle.kt
│       │           │   ├── ColorSchemePicker.kt
│       │           │   ├── Dpad.kt
│       │           │   ├── FaceButtons.kt             # A / B / X / Y
│       │           │   ├── Gamepad.kt                 # Full gamepad layout & routing
│       │           │   ├── GamepadCustomizationEditor.kt
│       │           │   ├── HUDViewfinder.kt           # Corner bracket decoration
│       │           │   ├── ListItemPicker.kt
│       │           │   ├── ResponsiveGrid.kt
│       │           │   ├── SpinBox.kt
│       │           │   └── Trigger.kt                 # LT / RT
│       │           ├── screens/
│       │           │   ├── AboutScreen.kt
│       │           │   ├── ConnectScreen.kt           # QR scan + manual IP/port entry
│       │           │   ├── ConnectingScreen.kt        # Progress, success, error + diagnostics
│       │           │   ├── ConnectionLostScreen.kt
│       │           │   ├── Gamepad.kt                 # Live gamepad screen & polling loop
│       │           │   ├── GamepadCustomization.kt    # Visual drag editor + options menu
│       │           │   ├── MainMenu.kt                # HUD hub, profile dialog, Resume button
│       │           │   └── SettingsScreen.kt
│       │           ├── theme/
│       │           │   ├── Color.kt                   # Neon/Glossy palette
│       │           │   ├── Shape.kt
│       │           │   ├── Theme.kt                   # Dynamic dark/light color schemes
│       │           │   ├── ThemePreview.kt
│       │           │   └── Type.kt
│       │           └── utils/
│       │               ├── FindActivity.kt
│       │               ├── HapticUtils.kt
│       │               └── LockScreenOrientation.kt
│       └── androidTest/
│           ├── TestGamepadServer.kt                   # Loopback TCP server for E2E tests
│           └── e2e/
│               ├── ConnectionE2ETest.kt
│               ├── GamepadInputE2ETest.kt
│               ├── NavigationE2ETest.kt
│               └── SettingsE2ETest.kt
├── Images/                                            # App screenshots
│   ├── 01_main_menu.jpg
│   ├── 02_settings.jpg
│   ├── 03_layout_profiles.jpg
│   ├── 04_gamepad.jpg
│   └── 05_connect.jpg
├── VGP_Data_Exchange/                                 # Git submodule — Colfer schema + generated code
│   ├── GamePadReading.colf                            # Wire format schema
│   ├── C/                                             # Generated C sources
│   └── io/github/kitswas/VGP_Data_Exchange/           # Generated Java sources
├── fastlane/metadata/android/                        # Store listing metadata & screenshots
├── gradle/
│   ├── libs.versions.toml                             # Centralized dependency versions
│   └── wrapper/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   └── workflows/
│       ├── build_and_test.yaml
│       └── validate_fastlane.yaml
├── build.gradle.kts
├── settings.gradle.kts
├── mise.toml                                          # Tool version management (Java 21)
├── signing.properties.template
└── gradlew / gradlew.bat
```

---

## Tech Stack

| Component | Library / Tool | Version |
|---|---|---|
| Language | Kotlin | 2.3.20 |
| UI | Jetpack Compose + Material3 | BOM 2026.03.01 |
| Navigation | Navigation Compose | 2.9.7 |
| State | ViewModel + StateFlow | lifecycle 2.10.0 |
| Persistence | DataStore Preferences | 1.2.1 |
| Serialization | kotlinx.serialization-json | 1.10.0 |
| QR scanning | ZXing Android Embedded | 4.3.0 |
| Wire encoding | Colfer (VGP_Data_Exchange submodule) | — |
| Documentation | Dokka | 2.2.0 |
| Build | Android Gradle Plugin | 9.1.0 |
| Min SDK | Android 8.0 | API 26 |
| Compile SDK | — | API 36 |
| Java | — | 21 |

---

## Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | TCP connection to the AIRstix server |
| `ACCESS_NETWORK_STATE` | Network diagnostics before connecting |
| `VIBRATE` | Haptic feedback on button press |

---

## Related

- **PC Server:** [AIRstix-server](https://github.com/Amarthya-sg/AIRstix-server) — the Linux-focused server that receives input from this app
- **Data Exchange Spec:** [VGP_Data_Exchange](https://github.com/kitswas/VGP_Data_Exchange) — Colfer schema and generated sources for the wire format

---

## License

See [LICENCE.TXT](LICENCE.TXT) — GNU General Public License v3.0.
