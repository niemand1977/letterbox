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

**AnimalsOnTop** (`AnimalsOnTop.kt`) — Displays an animal image at the top and three letter buttons below. The child taps the letter that matches the animal's name. A random letter A–Z is chosen and the corresponding animal image is shown (e.g. **A** → apple, **B** → bear); two wrong letters are randomly selected. Tapping correct plays a "yes" sound + green flash; wrong plays a "no" sound + red flash. A new round loads in-place (no screen flash).

The letter-to-animal mapping is `letter_resource_map` in `Utils.kt`.

### Sounds

- `yes.ogg` — played on correct answer
- `no.ogg` — played on wrong answer

Both are loaded via `SoundPool` with game audio attributes.

## Tech Stack

- **Language:** Kotlin
- **Build:** Gradle (via the checked-in wrapper) + Android Gradle Plugin
- **UI:** AndroidX AppCompat, ConstraintLayout
- **Images:** PNG, generated from SVG sources by the `Makefile` (see below)
- **License:** GPL-3.0

Versions are not repeated here — they live in `gradle/libs.versions.toml`
(Kotlin, AGP, AndroidX), `gradle/wrapper/gradle-wrapper.properties` (Gradle)
and `app/build.gradle` (SDK levels, Java/Kotlin target).

## Project Structure

```
app/src/main/
├── java/ch/cryptobit/letterbox/
│   ├── AnimalsOnTop.kt   # Main (and only) game activity
│   └── Utils.kt          # SoundPool, letter-animal map, bitmap loading
└── res/
    ├── drawable/         # Generated animal PNGs (A–Z)
    ├── drawable/src/     # SVG sources (Pixabay) + ic_launcher.svg
    ├── layout/           # Activity layout
    ├── mipmap-*/         # Launcher icons at each density
    └── raw/              # Sound effects (yes/no)
```

Build and packaging live in `build.gradle`, `app/build.gradle`,
`gradle/libs.versions.toml` and `app/proguard-rules.pro`; the SVG → PNG
pipeline in `Makefile`; store metadata in `fastlane/`; CI in `.gitlab-ci.yml`.

## Building

### Prerequisites

- Android SDK, including the platform matching `compileSdk` in `app/build.gradle`
- A JDK new enough for the Android Gradle Plugin (the build targets Java 17 bytecode)
- To regenerate images/icons from the SVGs: `rsvg-convert` (`librsvg2-bin`) and
  `convert` (ImageMagick, or `graphicsmagick-imagemagick-compat`)

Gradle itself does not need to be installed — use the wrapper.

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
- **`assets`** — rasterizes the animal SVGs in `app/src/main/res/drawable/src/`
  with `rsvg-convert`, then pipes them through `convert -trim +repage -resize`
  to strip transparent padding and downsize into `app/src/main/res/drawable/`
- **`icons`** — converts `ic_launcher.svg` to every required mipmap density and
  to the store icon under `fastlane/metadata/android/en-US/images/`

The exact pixel sizes are in the `Makefile`.

## CI / Releases

GitLab CI builds debug and release APKs on every push. On version tags
(`MAJOR.MINOR.PATCH`) it additionally:

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
