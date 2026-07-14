# Virtual GamePad Mobile

> Turn your Android phone into a wireless gamepad controller for your PC.

Virtual GamePad Mobile connects to a compatible PC server over your local Wi-Fi network and streams gamepad input in real time — dual analog sticks, D-Pad, face buttons, shoulder buttons, triggers, and more. The app runs locked to landscape, supports QR code pairing, and is designed to feel like a proper controller with a sci-fi HUD aesthetic.

**Version:** 0.4.2 · **Min SDK:** Android 8.0 (API 26) · **Target SDK:** API 34

---

## Features

- **Wireless gamepad over TCP/IP** — streams input to a PC server on your local network
- **QR code pairing** — scan a QR code displayed by the PC server to connect instantly; no manual IP/port entry required
- **Full button layout** — dual analog sticks, D-Pad, A/B/X/Y face buttons, LT/RT triggers, LB/RB shoulder buttons, Select, Start, Home, Capture
- **Gamepad customization** — adjust per-button visibility, scale, position, and anchor point; save and load layouts as JSON profiles
- **Haptic feedback** — configurable vibration on button press with three intensity levels (Soft / Medium / Strong)
- **Full-screen mode** — hides system bars for a clean controller UI
- **Persistent connection** — connection survives navigation (Settings, Main Menu) without dropping the session
- **Connection diagnostics** — checks Wi-Fi state, local IP, subnet reachability, ping, and port availability before connecting
- **Customizable theme** — choice of accent color and light/dark/system color scheme
- **Save connection credentials** — optionally remembers last-used IP and port
- **Configurable polling rate** — adjustable from 10 ms to 500 ms (default 80 ms)

---

## Screenshots

> _Screenshots coming soon._

---

## How It Works

The app establishes a TCP socket connection to an AIRstix server running on the same network. Gamepad state (button presses, analog stick positions, trigger values) is serialized using [Colfer](https://github.com/pascaldekloe/colfer) binary encoding and streamed at the configured polling interval. The socket is tuned with `TCP_NODELAY` and `IPTOS_LOWDELAY` for minimal input latency.

### Data Exchange Format

The wire format is defined in [`VGP_Data_Exchange/GamePadReading.colf`](VGP_Data_Exchange/GamePadReading.colf), modeled after the [Windows GamepadReading API](https://learn.microsoft.com/en-us/uwp/api/windows.gaming.input.gamepadreading):

```colf
package VGP_Data_Exchange

type GamepadReading struct {
    ButtonsUp         uint32
    ButtonsDown       uint32
    LeftTrigger       float32
    RightTrigger      float32
    LeftThumbstickX   float32
    LeftThumbstickY   float32
    RightThumbstickX  float32
    RightThumbstickY  float32
}
```

Thumbstick values are circular positions in the range `[-1.0, 1.0]` — `(0.7, 0.7)` is a corner, `(1.0, 1.0)` is out of range. Trigger values are in `[0.0, 1.0]`.

The `VGP_Data_Exchange` module is included as a git submodule and provides generated Java and C sources from the schema file.

---

## Getting Started

### Prerequisites

- Android Studio (latest stable recommended)
- JDK 21
- Android device or emulator running API 26+
- A compatible [AIRstix server](https://github.com/Amarthya-sg/AIRstix-server) running on the same network

### Clone the Repository

```bash
git clone --recurse-submodules https://github.com/kitswas/VirtualGamePad-Mobile.git
```

If you already cloned without `--recurse-submodules`:

```bash
git submodule update --init --recursive
```

### Build

```bash
./gradlew assemble
```

### Install on Device

```bash
./gradlew installDebug
```

Or open the project in Android Studio and run it directly.

---

## Connecting to a PC Server

1. Start the [AIRstix server](https://github.com/Amarthya-sg/AIRstix-server) on your PC.
2. Make sure your phone and PC are on the same Wi-Fi network.
3. Open the app and tap the connect button on the Main Menu.
4. Either:
   - **Scan the QR code** displayed by the PC server, or
   - Enter the PC's **IP address** and **port** manually.
5. Once connected, the app navigates to the gamepad screen and starts streaming input.

---

## Settings

| Setting | Default | Description |
|---|---|---|
| Color Scheme | System | Light / Dark / System default |
| Accent Color | Blue | Theme accent color |
| Full Screen | Enabled | Hides system bars |
| Haptic Feedback | Disabled | Vibrate on button press |
| Haptic Intensity | Medium | Soft / Medium / Strong |
| Polling Delay | 80 ms | Input send interval (10–500 ms) |
| Save Credentials | Disabled | Remember last IP and port |

---

## Gamepad Customization

Every button component can be individually configured:

- **Visible** — show or hide the component
- **Scale** — resize the component relative to its default size
- **Offset** — shift the component from its anchor point
- **Anchor** — pin to Top-Left, Top-Right, Bottom-Left, or Bottom-Right of the screen

Configurations can be exported and imported as JSON files, making it easy to share or back up layouts.

---

## Project Structure

```
.
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/io/github/kitswas/virtualgamepadmobile/
│       │   │   ├── MainActivity.kt              # Entry point, navigation graph
│       │   │   ├── data/
│       │   │   │   ├── BaseColor.kt             # Accent color enum
│       │   │   │   ├── ButtonComponent.kt       # Button component + config models
│       │   │   │   ├── ColorScheme.kt           # Color scheme enum
│       │   │   │   ├── Defaults.kt              # Default settings & button configs
│       │   │   │   ├── HapticIntensity.kt       # Haptic intensity enum
│       │   │   │   ├── Preview.kt               # Compose preview helpers
│       │   │   │   └── SettingsRepository.kt    # DataStore-backed settings
│       │   │   ├── network/
│       │   │   │   ├── ConnectionState.kt       # UI state model
│       │   │   │   ├── ConnectionViewModel.kt   # TCP socket, command queue
│       │   │   │   ├── ConnectionViewModelFactory.kt
│       │   │   │   ├── NetworkCommand.kt        # Sealed command types
│       │   │   │   └── NetworkDiagnostics.kt    # Wi-Fi / ping / port checks
│       │   │   └── ui/
│       │   │       ├── components/
│       │   │       │   └── QRCodeScanner.kt     # ZXing QR scanner wrapper
│       │   │       ├── composables/
│       │   │       │   ├── AnalogStick.kt
│       │   │       │   ├── BoundedNumericInput.kt
│       │   │       │   ├── ButtonConfigEditor.kt
│       │   │       │   ├── CentralButtons.kt
│       │   │       │   ├── Circle.kt
│       │   │       │   ├── ColorSchemePicker.kt
│       │   │       │   ├── Dpad.kt
│       │   │       │   ├── FaceButtons.kt
│       │   │       │   ├── Gamepad.kt           # Full gamepad drawing logic
│       │   │       │   ├── GamepadCustomizationEditor.kt
│       │   │       │   ├── HUDViewfinder.kt     # Sci-fi HUD brackets
│       │   │       │   ├── ListItemPicker.kt
│       │   │       │   ├── ResponsiveGrid.kt
│       │   │       │   ├── SpinBox.kt
│       │   │       │   └── Trigger.kt
│       │   │       ├── screens/
│       │   │       │   ├── AboutScreen.kt
│       │   │       │   ├── ConnectScreen.kt     # Manual IP/port + QR scan
│       │   │       │   ├── ConnectingScreen.kt
│       │   │       │   ├── ConnectionLostScreen.kt
│       │   │       │   ├── Gamepad.kt           # Live gamepad screen
│       │   │       │   ├── GamepadCustomization.kt
│       │   │       │   ├── MainMenu.kt
│       │   │       │   └── SettingsScreen.kt
│       │   │       ├── theme/                   # Material3 theming
│       │   │       └── utils/                   # Haptic, window utilities
│       │   └── AndroidManifest.xml
│       └── androidTest/
│           ├── TestGamepadServer.kt             # Loopback test server
│           └── e2e/
│               ├── ConnectionE2ETest.kt
│               ├── GamepadInputE2ETest.kt
│               ├── NavigationE2ETest.kt
│               └── SettingsE2ETest.kt
├── VGP_Data_Exchange/                           # Git submodule — Colfer schema + generated sources
│   ├── GamePadReading.colf                      # Wire format schema
│   ├── C/                                       # Generated C sources
│   └── io/github/kitswas/VGP_Data_Exchange/     # Generated Java sources
├── fastlane/                                    # Fastlane metadata
├── gradle/
│   ├── libs.versions.toml                       # Centralized dependency versions
│   └── wrapper/
├── build.gradle.kts                             # Root build config
├── settings.gradle.kts
├── gradlew / gradlew.bat
├── mise.toml                                    # Tool version management
└── signing.properties.template                 # Signing config template
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3.20 |
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose 2.9.7 |
| State management | ViewModel + StateFlow |
| Persistence | DataStore Preferences |
| QR scanning | ZXing Android Embedded 4.3.0 |
| Serialization | kotlinx.serialization-json |
| Wire encoding | Colfer (via VGP_Data_Exchange submodule) |
| Docs | Dokka |
| Build | AGP 9.1.0 + Gradle |
| Min SDK | API 26 (Android 8.0) |
| Compile SDK | API 36 |
| Java | 21 |

---

## Building for Release

Create a `signing.properties` file in the project root:

```properties
STORE_FILE=/path/to/your.keystore
STORE_PASSWORD=your_store_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

Then build the release APK:

```bash
./gradlew assembleRelease
```

If `signing.properties` is absent, the build falls back to the debug signing config.

---

## Testing

Run unit tests:

```bash
./gradlew test
```

Run instrumented tests (requires a connected device or emulator):

```bash
./gradlew connectedCheck
```

Run lint:

```bash
./gradlew lint
```

Run everything in parallel (as CI does):

```bash
./gradlew assemble lint test
```

---

## Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | TCP connection to PC server |
| `ACCESS_NETWORK_STATE` | Wi-Fi diagnostics before connecting |
| `VIBRATE` | Haptic feedback on button press |

---

## Related

- **PC Server:** [AIRstix-server](https://github.com/Amarthya-sg/AIRstix-server) — the Linux-focused server that receives input from this app
- **Data Exchange Spec:** [VGP_Data_Exchange](https://github.com/kitswas/VGP_Data_Exchange) — Colfer schema and generated sources for the wire format

---

## License

See [LICENCE.TXT](LICENCE.TXT).
