# LetterBox

A simple Android game for kids to learn letters. An animal image is shown and the child picks the correct starting letter from three options. Correct and incorrect answers are reinforced with sound and color feedback.

```
 ┌───┬───┐
 │ A │ B │   Tap the letter that matches the animal!
 ├───┼───┤
 │ C │ ? │
 └───┴───┘
```

## How It Works

**AnimalsOnTop** (`AnimalsOnTop.kt`) — Displays an animal image at the top and three letter buttons below. The child taps the letter that matches the animal's name. A random letter A–Z is chosen, the corresponding animal image is shown (e.g. **A** → apple, **B** → bear). Two wrong letters are randomly selected. Tapping correct plays a "yes" sound + green flash; wrong plays a "no" sound + red flash. A new round loads in-place (no screen flash).

### Letter-to-Animal Mapping

Defined in `Utils.kt` via `letter_resource_map`:

| Letter | Animal | Letter | Animal | Letter | Animal |
|--------|--------|--------|--------|--------|--------|
| A | Apple | J | Jaguar | S | Snake |
| B | Bear | K | Kangaroo | T | Table |
| C | Chameleon | L | Lion | U | Unicorn |
| D | Duck | M | Mouse | V | Vulture |
| E | Elephant | N | Nest | W | Whale |
| F | Fox | O | Octopus | X | Xylophone |
| G | Giraffe | P | Penguin | Y | Yak |
| H | Bunny | Q | Jellyfish | Z | Zebra |
| I | Hedgehog | R | Rainbow | | |

### Sounds

- `yes.ogg` — played on correct answer
- `no.ogg` — played on wrong answer

Both are loaded via `SoundPool` with game audio attributes.

## Tech Stack

- **Language:** Kotlin 1.9.24
- **Build:** Gradle 8.9, AGP 8.5.2
- **Min SDK:** 21 (Android 5.0)
- **Target SDK:** 34 (Android 14)
- **UI:** AndroidX AppCompat, ConstraintLayout
- **Images:** PNG (400×400), converted from SVG sources via `rsvg-convert`
- **License:** GPL-3.0

## Project Structure

```
letterbox/
├── app/
│   ├── build.gradle                    # App module build config
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/ch/cryptobit/letterbox/
│           │   ├── AnimalsOnTop.kt     # Main game activity
│           │   └── Utils.kt            # SoundPool, letter-animal map
│           └── res/
│               ├── drawable/           # Animal PNGs (A–Z) + launcher icon
│               ├── drawable/src/       # Original SVG sources (Pixabay + logo)
│               ├── mipmap-*/           # Launcher icon at each density
│               ├── layout/             # Activity layouts
│               ├── raw/                # Sound effects (yes/no)
│               └── values/             # Strings, colors, styles
├── build.gradle                        # Root build config
├── settings.gradle                     # Project settings
├── gradle.properties                   # Gradle/AndroidX config
├── Makefile                            # SVG → PNG conversion (assets + icons)
├── fastlane/                           # F-Droid / store metadata + icon
└── LICENSE                             # GPL-3.0
```

## Building

### Prerequisites

- Android SDK with platform 34 and build-tools 34.0.0
- JDK 21
- `rsvg-convert` (`librsvg2-bin`) — needed to regenerate images/icons from SVGs

### Build the APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Regenerate Images and Icons from SVGs

```bash
make
```

This runs two targets:
- **`assets`** — converts all animal SVGs in `app/src/main/res/drawable/src/` to 400×400 PNGs
- **`icons`** — converts `ic_launcher.svg` to all required mipmap densities (mdpi → xxxhdpi) and to `fastlane/metadata/android/en-US/images/icon.png` (512×512)

## CI / Releases

GitLab CI builds debug and release APKs on every push. On version tags (e.g. `0.1.1`) it additionally:

- Uploads both APKs and their SHA-256 checksums to the GitLab Package Registry (permanent URLs)
- Creates a GitLab Release with the hashes embedded — enabling **binary transparency**

Verify a downloaded APK:
```bash
sha256sum -c SHA256SUMS.release
```

## Deployment

Fastlane metadata lives in `fastlane/metadata/android/en-US/`. The app is available via [F-Droid](https://f-droid.org/packages/ch.cryptobit.letterbox/).

## License

GPL-3.0 — see [LICENSE](LICENSE).

Animal images are from [Pixabay](https://pixabay.com) (see `app/src/main/res/drawable/src/LICENSE` for details).
