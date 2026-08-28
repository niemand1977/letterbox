# LetterBox

A simple Android game for kids to learn letters. An animal image is shown and the child picks the correct starting letter from three options. Correct and incorrect answers are reinforced with sound feedback.

## How It Works

The app has two activity modes:

- **AnimalsOnTop** (`AnimalsOnTop.kt`) — The active mode. Displays an animal image at the top and three letter buttons below. The child taps the letter that matches the animal's name. A random letter A–Z is chosen, and the corresponding animal image is shown (e.g. **A** → apple, **B** → bear, **C** → chameleon). Two wrong letters are randomly selected from the remaining alphabet. Tapping the correct letter plays a "yes" sound; a wrong letter plays a "no" sound. The activity then recreates itself with a new round.

- **MainActivity** (`MainActivity.kt`) — The legacy mode. Shows a large letter at the top and three animal image buttons below. The child picks the animal whose name starts with the displayed letter. This mode is currently disabled (`ANIMALS_ON_TOP = true` in `MainActivity.kt` redirects to AnimalsOnTop).

- **LettersOnTop** (`LettersOnTop.kt`) — A fullscreen activity template with system UI auto-hide. Not actively used in the current game flow.

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

- **Language:** Kotlin
- **Build:** Gradle 8.7, AGP 8.2.0
- **Min SDK:** 19 (Android 4.4)
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
│           │   ├── MainActivity.kt     # Legacy mode (letter → pick animal)
│           │   ├── AnimalsOnTop.kt     # Active mode (animal → pick letter)
│           │   ├── LettersOnTop.kt     # Fullscreen template (unused)
│           │   └── Utils.kt            # SoundPool, letter-animal map, helpers
│           └── res/
│               ├── drawable/           # Animal PNGs (A–Z)
│               ├── drawable/src/       # Original SVG sources (Pixabay)
│               ├── layout/             # Activity layouts
│               ├── raw/                # Sound effects (yes/no)
│               └── values/             # Strings, colors, styles, attrs
├── build.gradle                        # Root build config
├── settings.gradle                     # Project settings
├── gradle.properties                   # Gradle/AndroidX config
├── Makefile                            # SVG → PNG conversion
├── fastlane/                           # F-Droid deployment metadata
└── LICENSE                             # GPL-3.0
```

## Building

### Prerequisites

- Android SDK with platform 34 and build-tools 34.0.0
- JDK 17+
- `rsvg-convert` (only needed to regenerate images from SVGs)

### Build the APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Regenerate Images from SVGs

```bash
make
```

This converts all SVG files in `app/src/main/res/drawable/src/` to 400×400 PNGs in `app/src/main/res/drawable/`.

## Deployment

Fastlane is configured for F-Droid. Metadata lives in `fastlane/metadata/android/en-US/`.

## License

GPL-3.0 — see [LICENSE](LICENSE).

Animal images are from [Pixabay](https://pixabay.com) (see `app/src/main/res/drawable/src/LICENSE` for details).
